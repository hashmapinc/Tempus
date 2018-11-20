import 'brace/ext/language_tools';
import 'brace/mode/json';
import 'brace/theme/github';

import './extension-form.scss';

/* eslint-disable angular/log */

import extensionFormModbusTemplate from './extension-form-modbus.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ExtensionFormModbusDirective($compile, $templateCache, $translate, types) {


    var linker = function(scope, element) {


        function Server() {
            this.transport = {
                "type": "tcp",
                "host": "localhost",
                "port": 502,
                "timeout": 3000
            };
            this.devices = []
        }

        function Device() {
            this.unitId = 1;
            this.deviceName = "";
            this.attributesPollPeriod = 1000;
            this.timeseriesPollPeriod = 1000;
            this.attributes = [];
            this.timeseries = [];
        }

        function Tag(globalPollPeriod) {
            this.tag = "";
            this.type = "long";
            this.pollPeriod = globalPollPeriod;
            this.functionCode = 3;
            this.address = 0;
            this.registerCount = 1;
            this.bit = 0;
            this.byteOrder = "BIG";
        }


        var template = $templateCache.get(extensionFormModbusTemplate);
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

        scope.addDevice = function(deviceList) {
            deviceList.push(new Device());
            scope.theForm.$setDirty();
        };

        scope.addNewAttribute = function(device) {
            device.attributes.push(new Tag(device.attributesPollPeriod));
            scope.theForm.$setDirty();
        };

        scope.addNewTimeseries = function(device) {
            device.timeseries.push(new Tag(device.timeseriesPollPeriod));
            scope.theForm.$setDirty();
        };

        scope.removeItem = (item, itemList) => {
            var index = itemList.indexOf(item);
            if (index > -1) {
                itemList.splice(index, 1);
            }
            scope.theForm.$setDirty();
        };

        scope.onTransportChanged = function(server) {
            var type = server.transport.type;

            server.transport = {};
            server.transport.type = type;
            server.transport.timeout = 3000;

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