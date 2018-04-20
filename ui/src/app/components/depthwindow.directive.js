/*
 * Copyright © 2016-2017 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//import './timewindow.scss';
import './depthwindow.scss';

import $ from 'jquery';
import thingsboardDepthinterval from './depthinterval.directive';
import thingsboardDepthPeriod from './depth-period.directive';

/* eslint-disable import/no-unresolved, import/default */

import depthwindowTemplate from './depthwindow.tpl.html';
import depthwindowButtonTemplate from './depthwindow-button.tpl.html';
import depthwindowPanelTemplate from './depthwindow-panel.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

import DepthwindowPanelController from './depthwindow-panel.controller';

export default angular.module('thingsboard.directives.depthwindow', [thingsboardDepthinterval, thingsboardDepthPeriod])
    .controller('DepthwindowPanelController', DepthwindowPanelController)
    .directive('tbDepthwindow', Depthwindow)
    .name;

/* eslint-disable angular/angularelement */
/*@ngInject*/
function Depthwindow($compile, $templateCache, $filter, $mdPanel, $document, $mdMedia, $translate, timeService, depthService, $log) {

    var linker = function (scope, element, attrs, ngModelCtrl) {

        /* tbTimewindow (ng-model)
         * {
         * 	  realtime: {
         * 	        interval: 0,
         * 			timewindowMs: 0
         * 	  },
         * 	  history: {
         * 	        interval: 0,
         * 			timewindowMs: 0,
         * 			fixedTimewindow: {
         * 				startTimeMs: 0,
         * 				endTimeMs: 0
         * 			}
         * 	  },
         * 	  aggregation: {
         * 	        type: types.aggregation.avg.value,
         * 	        limit: 200
         * 	  }
         * }
         */

        scope.historyOnly = angular.isDefined(attrs.historyOnly);

        scope.aggregation = scope.$eval(attrs.aggregation);

        scope.isToolbar = angular.isDefined(attrs.isToolbar);

        scope.hideLabel = function() {
            return scope.isToolbar && !$mdMedia('gt-sm');
        }

        var translationPending = false;

        $translate.onReady(function() {
            if (translationPending) {
                scope.updateDisplayValue();
                translationPending = false;
            }
        });

        var template;
        if (scope.asButton) {
            template = $templateCache.get(depthwindowButtonTemplate);
        } else {
            scope.direction = angular.isDefined(attrs.direction) ? attrs.direction : 'left';
            scope.tooltipDirection = angular.isDefined(attrs.tooltipDirection) ? attrs.tooltipDirection : 'top';
            template = $templateCache.get(depthwindowTemplate);
        }

        element.html(template);

        scope.openEditMode = function (event) {
            if (scope.disabled) {
                $log.log("scope disabled! ");
                return;
            }
            var position;
            var isGtSm = $mdMedia('gt-sm');
            if (isGtSm) {
                var panelHeight = 375;
                var panelWidth = 417;
                var offset = element[0].getBoundingClientRect();
                var bottomY = offset.bottom - $(window).scrollTop(); //eslint-disable-line
                var leftX = offset.left - $(window).scrollLeft(); //eslint-disable-line
                var yPosition;
                var xPosition;
                if (bottomY + panelHeight > $( window ).height()) { //eslint-disable-line
                    yPosition = $mdPanel.yPosition.ABOVE;
                } else {
                    yPosition = $mdPanel.yPosition.BELOW;
                }
                if (leftX + panelWidth > $( window ).width()) { //eslint-disable-line
                    xPosition = $mdPanel.xPosition.ALIGN_END;
                } else {
                    xPosition = $mdPanel.xPosition.ALIGN_START;
                }
                position = $mdPanel.newPanelPosition()
                    .relativeTo(element)
                    .addPanelPosition(xPosition, yPosition);
            } else {
                position = $mdPanel.newPanelPosition()
                    .absolute()
                    .top('0%')
                    .left('0%');
            }
            var config = {
                attachTo: angular.element($document[0].body),
                controller: 'DepthwindowPanelController',
                controllerAs: 'vm',
                templateUrl: depthwindowPanelTemplate,
                panelClass: 'tb-depthwindow-panel',
                position: position,
                fullscreen: !isGtSm,
                locals: {
                    'depthwindow': angular.copy(scope.model),
                    'historyOnly': scope.historyOnly,
                    'aggregation': scope.aggregation,
                    'onDepthwindowUpdate': function (depthwindow) {
                        scope.model = depthwindow;
                        scope.updateView();
                    }
                },
                openFrom: event,
                clickOutsideToClose: true,
                escapeToClose: true,
                focusOnOpen: false
            };
            $mdPanel.open(config);
        }

        scope.updateView = function () {
            var value = {};
            var model = scope.model;
            if (model.selectedTab === 0) {
                value.realtime = {
                    interval: model.realtime.interval,
                    depthwindowFt: model.realtime.depthwindowFt
                };
            } else {
                if (model.history.historyType === 0) {
                    value.history = {
                        interval: model.history.interval,
                        depthwindowFt: model.history.depthwindowFt
                    };
                } else {
                    value.history = {
                        interval: model.history.interval,
                        fixedDepthwindow: {
                            startDepthFt: model.history.fixedDepthwindow.startDepthFt,
                            endDepthFt: model.history.fixedDepthwindow.endDepthFt
                        }
                    };
                }
            }
            value.aggregation = {
                type: model.aggregation.type,
                limit: model.aggregation.limit
            };
            ngModelCtrl.$setViewValue(value);
            scope.updateDisplayValue();
        }

        scope.updateDisplayValue = function () {
            if ($translate.isReady()) {
                if (scope.model.selectedTab === 0 && !scope.historyOnly) {
                    scope.model.displayValue = $translate.instant('depthwindow.realtime') + ' - ' +
                        $translate.instant('depthwindow.last-prefix') + ' ' + scope.model.realtime.depthwindowFt + "Ft";
                } else {
                    scope.model.displayValue = !scope.historyOnly ? ($translate.instant('depthwindow.history') + ' - ') : '';
                    if (scope.model.history.historyType === 0) {
                        scope.model.displayValue += $translate.instant('depthwindow.last-prefix');
                    } else {
                        var startString = scope.model.history.fixedDepthwindow.startDepthFt + "Ft";
                        var endString = scope.model.history.fixedDepthwindow.endDepthFt + "Ft";
                        scope.model.displayValue += $translate.instant('depthwindow.period', {startDepth: startString, endDepth: endString});
                    }
                }
            } else {
                translationPending = true;
            }
        }

        ngModelCtrl.$render = function () {
            scope.model = depthService.defaultDepthwindow();
            if (ngModelCtrl.$viewValue) {
                var value = ngModelCtrl.$viewValue;
                var model = scope.model;
                if (angular.isDefined(value.realtime)) {
                    model.selectedTab = 0;
                    model.realtime.interval = value.realtime.interval;
                    model.realtime.depthwindowFt = value.realtime.depthwindowFt;
                } else {
                    model.selectedTab = 1;
                    model.history.interval = value.history.interval;
                    if (angular.isDefined(value.history.depthwindowFt)) {
                        model.history.historyType = 0;
                        model.history.depthwindowFt = value.history.depthwindowFt;
                    } else {
                        model.history.historyType = 1;
                        model.history.fixedDepthwindow.startDepthFt = value.history.fixedDepthwindow.startDepthFt;
                        model.history.fixedDepthwindow.endDepthFt = value.history.fixedDepthwindow.endDepthFt;
                    }
                }
                if (angular.isDefined(value.aggregation)) {
                    if (angular.isDefined(value.aggregation.type) && value.aggregation.type.length > 0) {
                        model.aggregation.type = value.aggregation.type;
                    }
                    model.aggregation.limit = value.aggregation.limit || depthService.avgAggregationLimit();
                }
            }
            scope.updateDisplayValue();
        };

        $compile(element.contents())(scope);
    }

    return {
        restrict: "E",
        require: "^ngModel",
        scope: {
            asButton: '=asButton',
            disabled:'=ngDisabled'
        },
        link: linker
    };
}
/* eslint-enable angular/angularelement */