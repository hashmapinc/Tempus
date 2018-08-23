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
import timezoneList from 'timezone-list';

/* eslint-disable angular/log */

import extensionFormWitsTemplate from './extension-form-wits.tpl.html'

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ExtensionFormWitsDirective($compile, $templateCache, $translate, types) {

    var linker = function(scope, element) {

        function Server() {
            this.deviceName = "Device Name",
            this.deviceType = "Device Type",
            this.tcpHost = "localhost",
            this.tcpPort = 13370,
            this.dateChannelNumbers="5",
            this.timeChannelNumbers = "6",
            this.attributesChannelNumbers="1",
            this.depthChannelNumbers = "8",
            this.timezone,
            this.records = {},
            this.channels = {}
        }

        var template = $templateCache.get(extensionFormWitsTemplate);
        element.html(template);

        scope.types = types;
        scope.theForm = scope.$parent.theForm;
        scope.timezoneList = timezoneList.getTimezones();


        if (!scope.configuration.servers.length) {
            scope.configuration.servers.push(new Server());
        }

        scope.addServer = function(serversList) {
            serversList.push(new Server());
            scope.theForm.$setDirty();
        }

        scope.removeItem = (item, itemList) => {
            var index = itemList.indexOf(item);
            if (index > -1) {
                itemList.splice(index, 1);
            }
            scope.theForm.$setDirty();
        }

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

        scope.fileAdded = function($file, model, options) {
            let reader = new FileReader();
            reader.onload = function(event) {
                scope.$apply(function() {
                    if(event.target.result) {
                        scope.theForm.$setDirty();
                        let addedFile = event.target.result;

                        if (addedFile && addedFile.length > 0) {
                            model[options.fileName] = $file.name;
                            model[options.file] = addedFile.replace(/^data.*base64,/, "");

                        }
                    }
                });
            };
            reader.readAsDataURL($file.file);
        };

        scope.clearFile = function(model, options) {
            scope.theForm.$setDirty();

            model[options.fileName] = null;
            model[options.file] = null;

        };
    };

    return {
            restrict: "A",
            link: linker,
            scope: {
                configuration: "=",
                timezoneList: "@",
                isAdd: "="
            }
        }
}