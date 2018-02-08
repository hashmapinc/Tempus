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

import computationFieldsetTemplate from './computation-fieldset.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ComputationDirective($compile, $templateCache, $translate, types, toast, utils, userService, componentDescriptorService, $log) {
    var linker = function (scope, element) {
        var template = $templateCache.get(computationFieldsetTemplate);
        element.html(template);

        //$log.error("HMDC scope.computation " + scope.computation.id.id);

        scope.showComputationConfig = false;

        scope.computationConfiguration = {
            data: null
        };

        if (scope.computation && !scope.computation.configuration) {
            scope.computation.configuration = {};
        }

        scope.$watch("computation", function (newValue, prevValue) {
            if (newValue != prevValue) {
                scope.computationConfiguration.data = null;
                if (scope.computation) {
                    $log.error("HMDC scope.computation " + angular.toJson(scope.computation));
                    componentDescriptorService.getComponentDescriptorByClazz(scope.computation.clazz).then(
                        function success(component) {
                            scope.computationComponent = component;
                            scope.showComputationConfig = !(userService.getAuthority() === 'TENANT_ADMIN'
                                                        && scope.computation.tenantId
                                                        && scope.computation.tenantId.id === types.id.nullUid)
                                                      && utils.isDescriptorSchemaNotEmpty(scope.computationComponent.configurationDescriptor);
                            scope.computationConfiguration.data = angular.copy(scope.computation.configuration);
                        },
                        function fail() {
                        }
                    );
                }
            }
        });

        scope.onComputationIdCopied = function() {
            toast.showSuccess($translate.instant('computation.idCopiedMessage'), 750, angular.element(element).parent().parent(), 'bottom left');
        };

        /*componentDescriptorService.getComponentDescriptorsByType(types.componentType.computation).then(
            function success(components) {
                scope.computationComponents = components;
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
            computation: '=',
            isEdit: '=',
            isReadOnly: '=',
            theForm: '=',
            onDeleteComputation: '&'
        }
    };
}
