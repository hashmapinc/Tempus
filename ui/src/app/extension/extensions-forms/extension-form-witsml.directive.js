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
import 'brace/ext/language_tools';
import 'brace/mode/json';
import 'brace/theme/github';
import './extension-form.scss';

/* eslint-disable angular/log */

import extensionFormWitsmlTemplate from './extension-form-witsml.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ExtensionFormWitsmlDirective($compile, $templateCache, $translate, types) {

    var linker = function (scope, element) {

        function Server() {
            this.host = "localhost";
            this.port = 49320;
            this.highFrequencyInMillis = 5000;
            this.lowFrequencyInSeconds = 60;
            this.timeoutInMillis = 5000;
            this.identity = {
                "type": "username"
            };
            this.version = "v1311";
            this.objectTypes = [];
            this.wellStatus = "drilling";
        }

        var template = $templateCache.get(extensionFormWitsmlTemplate);
        element.html(template);

        scope.types = types;
        scope.theForm = scope.$parent.theForm;

        if (!scope.configuration.servers.length) {
            scope.configuration.servers.push(new Server());
        }

        scope.addServer = function(serversList) {
            serversList.push(new Server());
            scope.theForm.$setDirty();
        };

        scope.removeItem = (item, itemList) => {
            var index = itemList.indexOf(item);
            if (index > -1) {
                itemList.splice(index, 1);
            }
            scope.theForm.$setDirty();
        };


        $compile(element.contents())(scope);

        scope.collapseValidation = function(index, id) {
            var invalidState = angular.element('#'+id+':has(.ng-invalid)');
            if(invalidState.length) {
                invalidState.addClass('inner-invalid');
            }
        };

        scope.expandValidation = function (index, id) {
            var invalidState = angular.element('#'+id);
            invalidState.removeClass('inner-invalid');
        };
    };

    return {
        restrict: "A",
        link: linker,
        scope: {
            configuration: "=",
            isAdd: "="
        }
    }

}