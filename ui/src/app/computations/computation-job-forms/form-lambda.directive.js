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

 import lambdaComputationJobForm from './form-lambda.tpl.html';
import kinesisTriggerForm from './triggers/trigger-kinesis.tpl.html';

 /* eslint-enable import/no-unresolved, import/default */

 /*@ngInject*/
export default function ComputationJobLambdaDirective($compile, $templateCache, types) {
    var linker = function (scope, element) {
        var template = $templateCache.get(lambdaComputationJobForm);
        scope.sparkJobTemplate = $templateCache.get(kinesisTriggerForm);

         element.html(template);
        scope.types = types;

 //        if(scope.config){
//            scope.config.type = types.computationType.lambda;
//        }
        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            config: '=',
            isEdit: '=',
            isReadOnly: '=',
            //computationDescriptor: '=',
            theForm: '=',
            //computation:'='
        }
    };
}