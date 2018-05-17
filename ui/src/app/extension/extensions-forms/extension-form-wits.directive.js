/*
 * Copyright © 2017-2018 Hashmap, Inc
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

import extensionFormWitsTemplate from './extension-form-wits.tpl.html'

export default function ExtensionFormWitsDirective($compile, $templateCache, $translate, types) {

    var linker = function(scope, element) {

        var template = $templateCache.get(extensionFormWitsTemplate);
        element.html(template);

        scope.types = types;
        scope.theForm = scope.$parent.theForm;


        scope.addServer = function() {
                var newServer = {
                    host: "localhost",
                    port: 13370,
                    depthBasedChannelNumbers: "1,2",
                    timeBasedChannelNumbers: "3,4",
                    recordSpecifications: "a,b,c",
                    channelSpecifications: "e,f,g"
                };
                scope.servers.push(newServer);
        };

        if(scope.isAdd) {
            scope.servers = [];
            scope.config.servers = scope.servers;
            scope.addServer();
        } else {
            scope.servers = scope.config.servers;
        }

        scope.removeServer = function(server) {
                var index = scope.servers.indexOf(server);
                if (index > -1) {
                    scope.servers.splice(index, 1);
                }
        };
    };

    return {
            restrict: "A",
            link: linker,
            scope: {
                config: "=",
                isAdd: "="
            }
        }
}