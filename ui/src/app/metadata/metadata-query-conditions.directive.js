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

//import metadataQueryConditionsTemplate from './metadata-query-conditions.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function MetadataQueryConditionsDirective($compile, $templateCache,$log) {
    var linker = function(scope) {
        $log.log("MetadataQueryConditionsDirective")


scope.count =0;

        scope.addHtml = function(){

        var div= angular.element(document).find('space-for-buttons');

//             var contentTr = angular.element('<md-input-container><label>name</label>'+
//                                             '<input type="text" ng-model="conditionName"></md-input-container>'+
//                                             '<md-input-container class="md-block"><label>condition</label>'+
//                                             '<md-select ng-model="conditions">'+
//                                             '<md-option ng-value="condition" ng-repeat="condition in vm.queryCondition">{{ condition}}</md-option>'+
//                                             '</md-select></md-input-container>'+
//                                             '<md-input-container><label>value</label>'+
//                                             '<input type="text" ng-model="conditionValue"></md-input-container>');
//                 $compile(contentTr)(scope);

            //angular.element(angular.element(document).find('space-for-buttons')).append($compile("<div><button class='btn btn-default' data-alert="+scope.count+">Show alert #"+scope.count+"</button></div>")(scope));
            scope.count = 1;
            $log.log(div)

            angular.element(div).append($compile("<div><button class='btn btn-default' data-alert="+scope.count+">Show alert #"+scope.count+"</button></div>")(scope));

        }

        scope.addHtml();

        //$compile(element.contents())(scope);

    };

    return {
        restrict: "E",
        link: linker,
        scope: {
        }
    }
}