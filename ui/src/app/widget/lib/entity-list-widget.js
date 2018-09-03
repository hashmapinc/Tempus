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
import addAssetTemplate from './add-asset.tpl.html'
import deviceListWidgetTemplate from './entity-list-widget.tpl.html';
  


export default angular.module('tempus.widgets.deviceListWidget', [])
    .directive('tbEntityListWidget', DeviceListWidget)
    .controller('addAssetController',AddAssetController)
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

function AddAssetController ($log,customerService,$state,dashboardService,datamodelService,userService,
    $rootScope,assetService,$mdDialog){
    $log.log($state);
    var vm = this;
    vm.name = null;
    vm.dataModelObject = null;
    vm.parentName = null;
    vm.parentId = null;
    vm.addAsset = addAsset;
    vm.cancel = cancel;
    vm.showCustList = false;
    vm.showParent =  false;
    vm.attributeList;
    var currentuser = userService.getCurrentUser();
    $log.log("currentuser");
    $log.log(currentuser);
    initController();
    function initController(){
        var pageSize = 10;
        dashboardService.getDashboard($state.params.dashboardId).then(function success(response) {
            if(response){
                vm.isParent = getDataModelObjectDetails(response.dataModelObjectId);
                vm.isParent.then(function success(object_response) {
                    $log.log(object_response.data)
                    if(object_response.data){ 
                        vm.name = object_response.data.name;
                        if(object_response.data.attributeDefinitions.length > 0){
                            vm.attributeList = object_response.data.attributeDefinitions;
                        }
                        $log.log(vm.attributeList)
                        if(object_response.data.parentId) {
                            var parentDetails = getDataModelObjectDetails(object_response.data.parentId.id);
                            parentDetails.then(function success(parentResponse) {
                                vm.showParent =  true;
                                vm.parentName = parentResponse.data.name;
                                vm.parentId = parentResponse.data.id.id
                            });
                        }else{
                            vm.showParent =  false;
                        }
                    }
                },
                function fail(){
                });               
            } 
        },
        function fail() {
        });
        if (currentuser.authority === 'CUSTOMER_USER') {
            vm.showCustList = false;
        }else {
            vm.showCustList = true;
            customerService.getCustomers({limit: pageSize, textSearch: ''}).then(
                function success(_customers) {
                    vm.users = _customers.data;
                    $log.log("vm.users")
                    $log.log(vm.users)
                },
                function fail() {
            });
        }
    }
    function getDataModelObjectDetails(dataModelObjectId){
        return datamodelService.getDatamodelObject(dataModelObjectId);
    }
    function addAsset(){
        
        var requestObj = {
            name:vm.asset_name,
            additionalInfo:{
                description:vm.description,
                parentId:vm.parentId
            },
            customerId:{
                entityType:"CUSTOMER"
            },
            type:vm.name.toLowerCase()
        }
        if(currentuser.authority === 'CUSTOMER_USER'){
            requestObj.customerId.id = currentuser.id
        }else {
            if (vm.user){
                requestObj.customerId.id = vm.user.id.id 
            }
        }
        $log.log(requestObj);
        assetService.saveAsset(requestObj).then(
            function success() {
                $rootScope.$broadcast('assetSaved');
                cancel();
            },
            function fail() {
            }
        );
    }
    function cancel() {
        $mdDialog.cancel();
    }

}

/*@ngInject*/
function DeviceListWidgetController($rootScope, $scope, $filter, deviceService, types, assetService, userService,
    $state, $stateParams, $translate, customerService,$document, $mdDialog,$log) {
    
    var vm = this,promise;
    $scope.query = {
        order: 'name',
        limit: 10,
        page: 1,
        search: null
    };
    vm.deviceDetailFunc = deviceDetailFunc;
    vm.loadTableData = loadTableData;
    $scope.showList = false;
    
    var customerId = $stateParams.customerId;
    $log.log($state);
    $log.log(vm);
    
    initController();
    
    $scope.devices = {
        count: 0,
        data: []
    };
    vm.devicesScope = $state.$current.data.devicesType;

    function initController() {
        var user = userService.getCurrentUser();
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
        vm.loadTableData();
    }
    function loadTableData(){
        $log.log($log.log("in load"))
        $log.log(vm.devicesScope);
        $log.log(customerId)
        if (vm.devicesScope === 'tenant') {
            promise = assetService.getTenantAssets({limit: 200, textSearch: ''}, true, null, false);
        }else if (vm.devicesScope === 'customer' || vm.devicesScope === 'customer_user') {
            promise = assetService.getCustomerAssets(customerId, {limit: 200, textSearch: ''}, true, null, false);
        }   
        if(promise) {
            promise.then(function success(items) {
                $log.log(items)
                if(items.data.length > 0){
                    $scope.showList = true;
                }
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
        
    }
    function deviceDetailFunc($event,list){
        $rootScope.$emit("CallTableDetailDeviceOnDashboard", [$event, list]);
    }

    /**
     * Add Asset
     */

    $scope.addAsset = function($event) {
        $mdDialog.show({
            controller: AddAssetController,
            controllerAs: 'vm',
            templateUrl: addAssetTemplate,
            parent: angular.element($document[0].body),
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
        }, function () {
            initController();
        });
    }
    
}