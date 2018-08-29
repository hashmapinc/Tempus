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

/* eslint-disable angular/log */

import computationFormSparkTemplate from './computation-form-kubeless.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ComputationFormKubelessDirective($compile, $templateCache, $translate, types) {
    var linker = function(scope, element) {

        var template = $templateCache.get(computationFormSparkTemplate);
        element.html(template);

        scope.types = types;
        scope.theForm = scope.$parent.theForm;

        $compile(element.contents())(scope);


        scope.fileAdded = function($file, options) {
            scope.theForm.$setDirty();
            scope.model[options.file] = $file;
            scope.model[options.fileName] = $file.name;
        };

        scope.clearFile = function(options) {
            scope.theForm.$setDirty();

            scope.model[options.file] = null;
            scope.model[options.fileName] = null;
        };

    };

    return {
        restrict: "A",
        link: linker,
        scope: {
            model: "="
        }
    }
}