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
/* eslint-disable import/no-unresolved, import/default, no-unused-vars */
import './gateway.scss';

/*@ngInject*/
export function GatewayController(deviceService, $scope, gatewayConfigurationService, $translate, toast) {

    var vm = this;

    vm.getGatewayDevices = getGatewayDevices;
    vm.save = save;
    vm.replica = 1;
    vm.loadGatewayConfiguration = loadGatewayConfiguration;
    vm.deployTempusGateway = deployTempusGateway;
    vm.getTempusGatewayPodsStatus = getTempusGatewayPodsStatus;
    vm.refresh = refresh;
    vm.scopeData = '';
    vm.configId ='';
    vm.podStatus ='';
    vm.getGatewayDevices();
    vm.loadGatewayConfiguration();
    vm.getTempusGatewayPodsStatus();

    function getGatewayDevices() {

        var pageLink = {limit:100};
        deviceService.getTenantDevices(pageLink,'','','Gateway').then(function success(response) {
            var devicelist = response.data;
            devicelist.forEach(device => {
                vm.gatewayDevicelist = {};
                deviceService.getDeviceCredentials(device.id.id).then(function success(res) {
                    var data = res;
                    vm.gatewayDevicelist[data.credentialsId] = device.name;

                })

            });

        });
    }

    function getTempusGatewayPodsStatus(){

        gatewayConfigurationService.getTempusGatewayPodsStatus().then(function success(response){
            vm.podStatus = response;
            if(vm.podStatus.replica == 0) {

                vm.getTempusGatewayPodsStatus();
            }
        });

    }

    function refresh() {

        vm.getTempusGatewayPodsStatus();
    }

    function loadGatewayConfiguration() {

        gatewayConfigurationService.getGatewayConfiguration().then(function success(response){
            vm.scopeData = response;
            if(response.id !== null) {
                vm.replica = response.replicas;
                vm.accesstoken = response.gatewayToken;
                vm.configId = response.id.id;
            }
        });

    }

    function save() {

        if(vm.scopeData.id !== null) {
            vm.item = vm.scopeData;
            vm.item.replicas = vm.replica;
            vm.item.gatewayToken = vm.accesstoken;
        } else {
            vm.item ={};
            vm.item = {
                     replicas:vm.replica,
                     gatewayToken:vm.accesstoken
                   };
        }
        gatewayConfigurationService.saveGatewayConfiguration(vm.item).then(function success(item){
            if(item) {
                  vm.scopeData = item;
                  vm.accesstoken = item.gatewayToken;
                  vm.configId = item.id.id;
                  toast.showSuccess($translate.instant('gateway.config-save-message'));
            }
        });
    }


    function deployTempusGateway() {

      gatewayConfigurationService.deployTempusGateway().then(function success(res){
            if(res.data == false) {
                toast.showError($translate.instant('gateway.config-deploy-fail-message'));
            } else {
                vm.getTempusGatewayPodsStatus();
                toast.showSuccess($translate.instant('gateway.config-deploy-message'));
            }
      });

    }

}