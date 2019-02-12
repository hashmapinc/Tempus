/*
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
/* eslint-disable import/no-unresolved, import/default */

import wellLogViewerTrackFieldsetTemplate from './well-log-track.tpl.html';
import wellTrackComponent from './well-log-component.tpl.html'
import WellLogViewerTrackDirective from './well-log-component.directive.js'
import WellLogComponentsDirective from './well-log-components.directive.js'

/* eslint-enable import/no-unresolved, import/default */


/*export default function WellLogViewerTrackDirective($compile, $templateCache) {
    var linker = function (scope, element) {
        var template = $templateCache.get(wellLogViewerTrackFieldsetTemplate);
        element.html(template);
        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            widgetsBundle: '=',
            isEdit: '=',
            isReadOnly: '=',
            theForm: '=',
            onExportWidgetsBundle: '&',
            onDeleteWidgetsBundle: '&'
        }
    };
}*/
export default angular.module('tempus.directives.wellLogViewerTrack', [])
    .directive('tbWellLogViewerTrack', WellLogViewerTrack)
    .directive('tbWellLogComponent', WellLogViewerTrackDirective)
    .directive('tbWellLogComponents', WellLogComponentsDirective)
    .name;

/*@ngInject*/
function WellLogViewerTrack() {
    return {
        restrict: "E",
        scope: true,
        bindToController: {
            schema: '=',
            form: '=',
            model: '=',
            formControl:'='
        },
        controller: WellLogTrackController,
        controllerAs: 'vm',
        templateUrl: wellLogViewerTrackFieldsetTemplate
    };
}

/* eslint-disable angular/angularelement */

/*@ngInject*/
function WellLogTrackController($scope, $log, $sce) {

    let vm = this;
    vm.count = 0;
    vm.addTrack = addTrack;
    vm.trackList = [];
    vm.trackWidth =[1,2,3,4,5];
    vm.trackComponent = wellTrackComponent;

    function addTrack(){
        vm.count = vm.count + 1;
        var insertDetail = {
            id: vm.count,
            details: []
        }
        vm.trackList.push(insertDetail);
        $log.log(vm.trackList);
    }
     $scope.deliberatelyTrustDangerousSnippet = function() {
     $log.log(wellTrackComponent)
                   return $sce.trustAsHtml(wellTrackComponent);
                 };

}
