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

import wellTrackComponents from './well-log-component.tpl.html'

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function WellLogViewerComponentsDirective($compile, $templateCache,types) {
    var linker = function (scope, element) {
        var template = $templateCache.get(wellTrackComponents);
        element.html(template);
        scope.componentTypes = types.wellLogComponent.componentTypes;
        scope.fillTypes = types.wellLogComponent.fillTypes;
        scope.styleTypes = types.wellLogComponent.styleTypes;

        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            trackComponent: '=',
            datasources:'='
        }
    };
}