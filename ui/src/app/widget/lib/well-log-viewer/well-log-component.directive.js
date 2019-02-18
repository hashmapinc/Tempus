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

import wellTrackComponent from './well-log-component.tpl.html'

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/


export default function WellLogViewerTrackDirective($compile, $templateCache, $log) {
    var linker = function (scope, element) {
        var template = $templateCache.get(wellTrackComponent);
        element.html(template);
        scope.trackWidth =[1,2,3,4,5];
        scope.trackDetail.details ={
            component :[]
        }
        var vm = this;
        vm.count = 1;

        $log.log(scope)
        $compile(element.contents())(scope);
        scope.trackDetail.component = [];
        scope.addComponent = function (){
            var insertDetail = {
                id: vm.count ++
            }
            scope.trackDetail.component.push(insertDetail);
        }
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            trackDetail: '=',
            datasources: '='
        }
    };
}