/*
 * Copyright © 2016-2017 The Thingsboard Authors
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
export default function ComputationJobDirective($compile, $templateCache, $log, $translate, types, toast) {
    var linker = function (scope, element) {
        var template = $templateCache.get(computationJobFieldsetTemplate);
        element.html(template);

        scope.showComputationJobConfig = false;
        //scope.showComputationJobConfig = true;

        scope.computationJobConfiguration = {
            data: null
        };

        if (scope.computation) {
            scope.showComputationJobConfig = true;
            scope.computationDescriptor = scope.computation.jsonDescriptor;
            //scope.computationjob.computationId = scope.computation.id;
        } 

        /*computationService.getComputation(scope.computation.id.id).then(
            function success(computation) {
                scope.computation = computation;
                scope.showComputationJobConfig = true;
                scope.computationDescriptor = computation.jsonDescriptor;
                $log.log("Computation success: " + angular.toJson(computation));
            },
            function fail() {
            }
        );*/
 
        scope.$watch('computationjob.name', function(newValue, oldValue) {
            $log.log("newValue, oldValue" + newValue + ":" + oldValue);
        });


        scope.$watch('computationjob', function(newValue, oldValue) {
            if (newValue && !angular.equals(newValue, oldValue)) {
                scope.computationJobConfiguration.data = null;
                scope.computationjob = newValue;
                //scope.computationjob.computationId = scope.computation.id;
                scope.computationJobConfiguration.data = angular.copy(newValue.argParameters);
            }
        });

        scope.$watch('computation', function(newValue, oldValue) {
            if(newValue && !angular.equals(newValue, oldValue)){
                scope.showComputationJobConfig = true;
                scope.computationDescriptor = newValue.jsonDescriptor;
                //scope.computationjob.computationId = scope.computation.id;
            } 
        }, true);


        if (scope.computationjob && !scope.computationjob.argParameters) {
            scope.computationjob.argParameters = {};
        }

        scope.$watch("computationJobConfiguration.data", function (newValue, prevValue) {
            if (newValue && !angular.equals(newValue, prevValue)) {
                scope.computationjob.argParameters = angular.copy(scope.computationJobConfiguration.data);
            }
        }, true);

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
            isEdit: '=',
            isReadOnly: '=',
            theForm: '=',
            computation: '=',
            onActivateComputationJob: '&',
            onSuspendComputationJob: '&',
            onExportComputationJob: '&',
            onDeleteComputationJob: '&'
        }
    };
}
