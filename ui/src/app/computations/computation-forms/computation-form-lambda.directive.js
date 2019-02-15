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

 import computationFormLambdaTemplate from './computation-form-lambda.tpl.html';

 /* eslint-enable import/no-unresolved, import/default */

 /*@ngInject*/
export default function ComputationFormLambdaDirective($compile, $templateCache, $translate, types) {
    var linker = function(scope, element) {

         var template = $templateCache.get(computationFormLambdaTemplate);
        element.html(template);

         scope.types = types;
        scope.theForm = scope.$parent.theForm;

         $compile(element.contents())(scope);

         if(!scope.model.computationMetadata)
            scope.model.computationMetadata = {}

         scope.model.computationMetadata.type = types.computationType.lambda;

         scope.fileAdded = function($file) {
            scope.theForm.$setDirty();
            scope.model.importData = $file;
        };

         scope.clearFile = function() {
            scope.theForm.$setDirty();
            scope.model.importData = null;
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