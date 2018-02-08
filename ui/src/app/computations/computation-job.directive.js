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
export default function ComputationJobDirective($compile, $templateCache, $log){//, $translate, types, toast, utils, userService) {
    var linker = function (scope, element) {
        var template = $templateCache.get(computationJobFieldsetTemplate);
        element.html(template);

        //$log.error("HMDC scope.computationJob " + scope.computationJob.id.id);

        //scope.computationJob = null;
        
        scope.$watch('computationJob.name', function(newValue, oldValue) {
            $log.log("newValue, oldValue" + newValue + ":" + oldValue);
        });

        scope.$watch('computationJob', function(newValue, oldValue) {
            $log.log("newValue, oldValue" + newValue + ":" + oldValue);
            scope.computationJob = newValue;
        });

        $log.log("scope computation : " + scope.computation);

        scope.showComputationJobConfig = false;

        scope.computationJobConfiguration = {
            data: null
        };


        if (scope.computationJob && !scope.computationJob.configuration) {
            scope.computationJob.configuration = {};
        }



        /*scope.$watch("computationJob.clazz", function (newValue, prevValue) {
            if (newValue != prevValue) {
                scope.computationJobConfiguration.data = null;
                if (scope.computationJob) {
                    $log.error("HMDC scope.computationJob " + angular.toJson(scope.computationJob));
                    componentDescriptorService.getComponentDescriptorByClazz(scope.computationJob.clazz).then(
                        function success(component) {
                            scope.computationJobComponent = component;
                            scope.showComputationJobConfig = !(userService.getAuthority() === 'TENANT_ADMIN'
                                                        && scope.computationJob.tenantId
                                                        && scope.computationJob.tenantId.id === types.id.nullUid)
                                                      && utils.isDescriptorSchemaNotEmpty(scope.computationJobComponent.configurationDescriptor);
                            scope.computationJobConfiguration.data = angular.copy(scope.computationJob.configuration);
                        },
                        function fail() {
                        }
                    );
                }
            }
        });

        scope.$watch("computationJobConfiguration.data", function (newValue, prevValue) {
            if (newValue && !angular.equals(newValue, prevValue)) {
                scope.computationJob.configuration = angular.copy(scope.computationJobConfiguration.data);
            }
        }, true);

        scope.onComputationJobIdCopied = function() {
            toast.showSuccess($translate.instant('computationJob.idCopiedMessage'), 750, angular.element(element).parent().parent(), 'bottom left');
        };

        /*componentDescriptorService.getComponentDescriptorsByType(types.componentType.computationJob).then(
            function success(components) {
                scope.computationJobComponents = components;
            },
            function fail() {
            }
        );*/

        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            computationJob: '=?',
            isEdit: '=',
            isReadOnly: '=',
            theForm: '=',
            onActivateComputationJob: '&',
            onSuspendComputationJob: '&',
            onExportComputationJob: '&',
            onDeleteComputationJob: '&'
        },
        bindToController: {
            computation: '=?'
        },
        controller: 'ComputationJobController',
        controllerAs: 'vm'
    };
}
