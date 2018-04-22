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
import '../plugin/plugin.scss';

/* eslint-disable import/no-unresolved, import/default */

import computationFieldsetTemplate from './computation-fieldset.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ComputationDirective($compile, $templateCache) {
    var linker = function (scope, element) {
        var template = $templateCache.get(computationFieldsetTemplate);
        element.html(template);
        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            computation: '=',
            isEdit: '=',
            isReadOnly: '=',
            theForm: '=',
            onDeleteComputation: '&'
        }
    };
}
