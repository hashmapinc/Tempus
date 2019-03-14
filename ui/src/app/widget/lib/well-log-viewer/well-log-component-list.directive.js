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

import wellTrackComponent from './well-log-component-list.tpl.html'

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/


export default function WellLogViewerTrackDirective($compile, $templateCache,types) {
    var linker = function (scope, element) {
        var template = $templateCache.get(wellTrackComponent);
        element.html(template);
        scope.trackWidth =types.wellLogComponent.trackWidth;
        scope.isLineButtonHidden = false;
        scope.isGridButtonHidden = false;
        scope.isTimeYButtonHidden = false;

        scope.addComponent = function (componentType){
            changeButtonVisibility(componentType, scope, true);
            var insertDetail = {
                id: scope.trackDetail.component.length ? scope.trackDetail.component.length + 1 : 1,
                cType: componentType
            }
            scope.trackDetail.component.push(insertDetail);
        }

        scope.removeComponent = function ($event,id){
            var index = scope.trackDetail.component.findIndex(x => x.id==id);
            var componentType = scope.trackDetail.component.find(x => x.id==id).cType;
            changeButtonVisibility(componentType, scope, false);
            if($event){
                  $event.stopPropagation();
                  $event.preventDefault();
            }
            scope.trackDetail.component.splice(index, 1);
        }
        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            trackDetail: '=',
            datasources: '='
        }
    };

    function changeButtonVisibility(componentType, scope, isHidden) {
        if (componentType === 'Line') {
            scope.isLineButtonHidden = isHidden;
        }
        if (componentType === 'Linear Grid') {
            scope.isGridButtonHidden = isHidden;
        }
        if (componentType === 'Time Y axis') {
            scope.isTimeYButtonHidden = isHidden;
        }
    }
}