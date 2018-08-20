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



import './entity-list-widget.scss'
import deviceListWidgetTemplate from './entity-list-widget.tpl.html';
  


export default angular.module('tempus.widgets.deviceListWidget', [])
    .directive('tbEntityListWidget', DeviceListWidget)
    .name;

/*@ngInject*/
function DeviceListWidget() {
    return {
        restrict: "E",
        scope: true,
        bindToController: {
            tableId: '=',
            ctx: '=',
            config: '='
        },
        controller: DeviceListWidgetController,
        controllerAs: 'vm',
        templateUrl: deviceListWidgetTemplate
    };
}

/*@ngInject*/
function DeviceListWidgetController($rootScope,$scope,$filter,$log,deviceService,types,assetService,userService,
    $state,$stateParams,$translate,customerService) {
    var vm = this,promise;
    $scope.query = {
        order: 'name',
        limit: 15,
        page: 1,
        search: null
    };
    vm.deviceDetailFunc = deviceDetailFunc;
    vm.loadTableData = loadTableData;
    $scope.showList = true;
    var customerId = $stateParams.customerId;
    
    initController();
    vm.loadTableData();
    $scope.devices = {
        count: 0,
        data: []
    };
    vm.devicesScope = $state.$current.data.devicesType;

    function initController() {
        var user = userService.getCurrentUser();
        $log.log("user");
        $log.log(user);
        if (user.authority === 'CUSTOMER_USER') {
            vm.devicesScope = 'customer_user';
            customerId = user.customerId;
        }else {
            vm.devicesScope = 'tenant';
        }
        if (customerId) {
            vm.customerDevicesTitle = $translate.instant('customer.devices');
            customerService.getShortCustomerInfo(customerId).then(
                function success(info) {
                    if (info.isPublic) {
                        vm.customerDevicesTitle = $translate.instant('customer.public-devices');
                    }
                }
            );
        }
    }
    function loadTableData(){
        if(vm.config.config.datasources && vm.config.config.datasources.length > 0){
            if(vm.config.config.datasources[0].dataKeys[0] == 'Assets'){
                if (vm.devicesScope === 'tenant') {
                    promise = assetService.getTenantAssets({limit: 200, textSearch: ''}, true, null, false);
                }else if (vm.devicesScope === 'customer' || vm.devicesScope === 'customer_user') {
                    promise = assetService.getCustomerAssets(customerId, {limit: 200, textSearch: ''}, true, null, false);
                }   
            }else if(vm.config.config.datasources[0].dataKeys[0] == 'Devices'){
                if (vm.devicesScope === 'tenant') {
                    promise = deviceService.getTenantDevices({limit: 200, textSearch: ''}, true, null, false);
                }else if (vm.devicesScope === 'customer' || vm.devicesScope === 'customer_user') {
                    promise = deviceService.getCustomerDevices(customerId, {limit: 200, textSearch: ''}, true, null,false);
                }
            }
            if(promise) {
                promise.then(function success(items) {
                    $scope.loadingData = false;
    
                    var deviceSortList = $filter('orderBy')(items.data, $scope.query.order);
                    var startIndex = $scope.query.limit * ($scope.query.page - 1);
    
    
                    if ($scope.query.search != null) {
    
                        deviceSortList = $filter('filter')(items.data, function(data) {
                            if ($scope.query.search) {
                                return data.name.toLowerCase().indexOf($scope.query.search.toLowerCase()) > -1 || data.type.toLowerCase().indexOf($scope.query.search.toLowerCase()) > -1;
                            } else {
                                return true;
                            }
                        });
                        //$scope.query.page =1;
                        deviceSortList = $filter('orderBy')(deviceSortList, $scope.query.order);
                        if ($scope.query.search != '') {startIndex =0;}
                    }
                        var devicePaginatedata = deviceSortList.slice(startIndex, startIndex + $scope.query.limit);
                        $scope.devices = {
                            count: items.data.length,
                            data: devicePaginatedata
                        };
                    })
                }
        }else{
            $scope.showList = false;
        }
        
    }
    function deviceDetailFunc($event,list){
        $rootScope.$emit("CallTableDetailDeviceOnDashboard", [$event, list]);
    }
}