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
import '../plugin/plugin.scss';

/* eslint-disable import/no-unresolved, import/default */

import computationJobFieldsetTemplate from './computation-job-fieldset.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ComputationJobDirective($compile, $templateCache, $translate, types, toast, $stateParams, computationService) {
    var linker = function (scope, element) {
        var template = $templateCache.get(computationJobFieldsetTemplate);
        element.html(template);
        scope.types = types;
        scope.showComputationJobConfig = false;

        if (scope.computation) {
            scope.showComputationJobConfig = true;
            if (scope.computation.type == types.computationType.spark)
                scope.computationDescriptor = scope.computation.computationMetadata.jsonDescriptor;
        }
        else{
            computationService.getComputation($stateParams.computationId).then(
                function success(computation) {
                    scope.computation = computation;
                    if (scope.computation.type == types.computationType.spark)
                        scope.computationDescriptor = computation.computationMetadata.jsonDescriptor;
                    scope.flag=true
                    $compile(element.contents())(scope);
                },
                function fail() {
                }
            );
        }

        scope.$watch('computation', function(newValue, oldValue) {
            if(newValue && !angular.equals(newValue, oldValue)){
                scope.showComputationJobConfig = true;
                if (newValue.type == types.computationType.spark)
                    scope.computationDescriptor = newValue.computationMetadata.jsonDescriptor;
            }
        }, true);

        if (scope.computationjob && !scope.computationjob.configuration) {
            scope.computationjob.configuration = {};
        }

        scope.onComputationJobIdCopied = function() {
            toast.showSuccess($translate.instant('computationJob.idCopiedMessage'), 750, angular.element(element).parent().parent(), 'bottom left');
        };

        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            computationjob: '=',
            computation: '=?',
            isEdit: '=',
            isReadOnly: '=',
            theForm: '=',
            onActivateComputationJob: '&',
            onSuspendComputationJob: '&',
            onExportComputationJob: '&',
            onDeleteComputationJob: '&'
        }
    };
}
