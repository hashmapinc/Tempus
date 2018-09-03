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

import kubelessComputationJobForm from './form-kubeless.tpl.html';
import kafkaTriggerForm from './triggers/trigger-kafka.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ComputationJobKubelessDirective($compile, $templateCache, $translate, types, $log) {
    var linker = function (scope, element) {
        var template = $templateCache.get(kubelessComputationJobForm);
        scope.sparkJobTemplate = $templateCache.get(kafkaTriggerForm);
        element.html(template);
        scope.types = types;
        $log.log('Config is ', scope.config);
        if(!scope.config || !scope.config.functionSelector){
            scope.config = {
                functionSelector: []
            };
        }

        scope.addMap = function(mapping) {
            $log.log('Mapping is ', mapping);
            var newMap = {key:"", value:""};
            mapping.push(newMap);
        };

        scope.removeMap = function(map, mapping) {
            var index = mapping.indexOf(map);
            if (index > -1) {
                mapping.splice(index, 1);
            }
        };

        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            config: '=',
            isEdit: '=',
            isReadOnly: '=',
            theForm: '='
        }
    };
}