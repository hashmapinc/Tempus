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
//import wellTrackComponent from './well-log-component.tpl.html'
import WellLogViewerTrackDirective from './well-log-component.directive.js'
import WellLogComponentsDirective from './well-log-components.directive.js';
import './well-log-track.scss';

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
function WellLogViewerTrack($compile, $templateCache) {
     var linker = function (scope, element) {
            var template = $templateCache.get(wellLogViewerTrackFieldsetTemplate);
            element.html(template);

            scope.addTrack = function (){
                if(angular.isUndefined(scope.model.Track)){
                    scope.model.Track = [];
                }
                var insert ={
                    id: scope.model.Track.length + 1,
                    width:"",
                    component:[]
                }

                scope.model.Track.push(insert);
            }
            scope.removeTrack = function ($event,id){
                var index = scope.model.Track.findIndex(x => x.id==id);
                if($event){
                      $event.stopPropagation();
                      $event.preventDefault();
                }
                scope.model.Track.splice(index, 1);
            }
             $compile(element.contents())(scope);
     }
    return {
        restrict: "E",
        scope: {
            schema: '=',
            form: '=',
            model: '=',
            formControl:'=',
            datasources:'='
        },
        link: linker,
        /*bindToController: {
            schema: '=',
            form: '=',
            model: '=',
            formControl:'=',
            datasources:'='
        },*/
       /* controller: WellLogTrackController,
        controllerAs: 'vm',
        templateUrl: wellLogViewerTrackFieldsetTemplate*/
    };
}

/* eslint-disable angular/angularelement */

/*@ngInject*/
/*function WellLogTrackController($scope, $log) {

    $scope.addTrack = function (){
    $log.log("iin add");
            $log.log($scope)
        if(!$scope.vm.model.Track){
            $scope.vm.model={
                Track:[]
            }
        }
        var insert ={
            id: $scope.vm.model.Track.length + 1,
            width:"",
            component:[]
        }

        $scope.vm.model.Track.push(insert);
    }*/
     /*$scope.deliberatelyTrustDangerousSnippet = function() {
           return $sce.trustAsHtml(wellTrackComponent);
     };*/

/*}*/
