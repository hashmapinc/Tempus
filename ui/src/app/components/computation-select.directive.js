/*
 * Copyright © 2017-2018 Hashmap, Inc
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
import './plugin-select.scss';

import TempusApiComputation from '../api/computation.service';

/* eslint-disable import/no-unresolved, import/default */

import computationSelectTemplate from './computation-select.tpl.html';

/* eslint-enable import/no-unresolved, import/default */


export default angular.module('tempus.directives.computationSelect', [TempusApiComputation])
    .directive('tbComputationSelect', ComputationSelect)
    .name;

/*@ngInject*/
function ComputationSelect($compile, $templateCache, $q, computationService, types) {

    var linker = function (scope, element, attrs, ngModelCtrl) {
        var template = $templateCache.get(computationSelectTemplate);
        element.html(template);

        scope.tbRequired = angular.isDefined(scope.tbRequired) ? scope.tbRequired : false;
        scope.computation = null;
        scope.computationSearchText = '';
        scope.searchTextChanged = false;

        scope.computationFetchFunction = computationService.getAllComputations;
        if (angular.isDefined(scope.computationsScope)) {
            if (scope.computationsScope === 'action') {
                scope.computationFetchFunction = computationService.getAllActionComputations;
            } else if (scope.computationsScope === 'system') {
                scope.computationFetchFunction = computationService.getSystemComputations;
            } else if (scope.computationsScope === 'tenant') {
                scope.computationFetchFunction = computationService.getTenantComputations;
            }
        }

        scope.fetchComputations = function(searchText) {
            var pageLink = {limit: 10, textSearch: searchText};

            var deferred = $q.defer();

            scope.computationFetchFunction(pageLink).then(function success(result) {
                deferred.resolve(result.data);
            }, function fail() {
                deferred.reject();
            });

            return deferred.promise;
        }

        scope.computationSearchTextChanged = function() {
            scope.searchTextChanged = true;
        }

        scope.isSystem = function(item) {
            return item && item.tenantId.id === types.id.nullUid;
        }

        scope.updateView = function () {
            ngModelCtrl.$setViewValue(scope.computation);
        }

        ngModelCtrl.$render = function () {
            if (ngModelCtrl.$viewValue) {
                scope.computation = ngModelCtrl.$viewValue;
            }
        }

        scope.$watch('computation', function () {
            scope.updateView();
        })

        if (scope.selectFirstComputation) {
            var pageLink = {limit: 1, textSearch: ''};
            scope.computationFetchFunction(pageLink).then(function success(result) {
                var computations = result.data;
                if (computations.length > 0) {
                    scope.computation = computations[0];
                }
            }, function fail() {
            });
        }

        $compile(element.contents())(scope);
    }

    return {
        restrict: "E",
        require: "^ngModel",
        link: linker,
        scope: {
            computationsScope: '@',
            theForm: '=?',
            tbRequired: '=?',
            selectFirstComputation: '='
        }
    };
}
