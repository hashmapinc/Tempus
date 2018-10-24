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
export default function ComputationJobKubelessDirective($compile, $templateCache, $translate, types) {
    var linker = function (scope, element) {
        var template = $templateCache.get(kubelessComputationJobForm);
        scope.sparkJobTemplate = $templateCache.get(kafkaTriggerForm);
        element.html(template);
        scope.types = types;

        //TODO: Add type check
        if(!scope.config || !scope.config.functionSelector){
            scope.config = {
                functionSelectorMap: []
            };
        }

        scope.addMap = function(mapping) {
            var newMap = {key:"", value:""};
            mapping.push(newMap);
        };

        scope.removeMap = function(map, mapping) {
            var index = mapping.indexOf(map);
            if (index > -1) {
                mapping.splice(index, 1);
            }
        };

        scope.$watch('config', function (newValue, oldValue) {
            if(newValue || !angular.equals(newValue, oldValue)){
                var selectors = newValue.functionSelectorMap;
                if(selectors) {
                    var result = selectors.reduce(function (map, obj) {
                        map[obj.key] = obj.value;
                        return map;
                    }, {});
                    newValue.functionSelector = result;
                }else {
                    var tempArr = [];
                    angular.forEach(newValue.functionSelector, function(value, key){
                        var obj ={
                         "key" :key,
                         "value": value
                        }
                        tempArr.push(obj);
                    });
                    newValue.functionSelectorMap = tempArr;
                }
            }
        }, true);

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