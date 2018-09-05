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
    $rootScope,assetService,$mdDialog,entityRelationService,attributeService){
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
    vm.assetList= [];
    vm.attributeList;
    vm.dashboardDetail = null;
    vm.associatedAsset = null;
    vm.loadrelationModel =loadrelationModel;
    var currentuser = userService.getCurrentUser();
    vm.disableFlag = false;
    initController();
    function initController(){
        var pageSize = 10;
        dashboardService.getDashboard($state.params.dashboardId).then(function success(response) {
            $log.log(response);
            if(response){
                vm.dashboardDetail = response;
                vm.isParent = getDataModelObjectDetails(response.dataModelObjectId);
                vm.isParent.then(function success(object_response) {
                    $log.log(object_response.data)
                    if(object_response.data){ 
                        vm.name = object_response.data.name;
                        if(object_response.data.attributeDefinitions.length > 0){
                            vm.attributeList = object_response.data.attributeDefinitions;
                        }
                        
                        if(object_response.data.parentId) {
                            var parentDetails = getDataModelObjectDetails(object_response.data.parentId.id);
                            parentDetails.then(function success(parentResponse) {
                                vm.showParent =  true;
                                vm.parentName = parentResponse.data.name;
                                vm.parentId = parentResponse.data.id.id;
                                if (currentuser.authority === 'CUSTOMER_USER') {
                                    $log.log("parentResponse")
                                    $log.log(parentResponse)
                                    customerService.getCustomer(parentResponse.data.customerId.id).then(
                                        function success(_customer) {
                                            $log.log(_customer)
                                            vm.user = _customer.data;
                                        },
                                        function fail() {
                                    });
                                }
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
            vm.showCustList = true;
            vm.disableFlag = true
        }else {
            vm.showCustList = true;
            customerService.getCustomers({limit: pageSize, textSearch: ''}).then(
                function success(_customers) {
                    vm.users = _customers.data;
                },
                function fail() {
            });
        }
    }
    function loadrelationModel(){
        $log.log(vm.parentId)
        assetService.getAssetByObjectId(vm.parentId).then(function success(parentList){
            angular.forEach(parentList, function(value){
                $log.log("value");
                $log.log(value);
                $log.log(vm.user);
                if(value.customerId.id == vm.user.id.id){
                    vm.assetList.push(value)
                }
            });
        },function fail(){});
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
            type:vm.name.toLowerCase(),
            attributes:vm.attributes,
            dataModelObjectId:{
                entityType:"DATA_MODEL_OBJECT",
                id:vm.dashboardDetail.dataModelObjectId
            }
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
            function success(response) {
                if(vm.parentId && vm.associatedAsset.hasOwnProperty("name") ){
                    var relation ={
                        type:"Contains",
                        from: {
                            id: response.id.id,
                            entityType: "ASSET"
                            },
                        to:{
                            entityType: "ASSET",
                            id: vm.associatedAsset.id.id
                        }
                    }
                    entityRelationService.saveRelation(relation).then(function success(){},
                    function fail(){})
                }
                $log.log("vm.attributes")
                $log.log(vm.attributes)
                var att =[];
                if(vm.attributes){
                    angular.forEach(vm.attributes, function(value,key){
                        var a ={
                            key :key,
                            value:value
                        }
                        att.push(a);
                    });
                    $log.log(att)
                    attributeService.saveEntityAttributes("ASSET",response.id.id,"SERVER_SCOPE",att).then(function success(re){
                        $log.log(re)
                    },
                    function fail(){
                        $log.log("in fail")
                    })
                }

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
function DeviceListWidgetController($rootScope, $scope, $filter, dashboardService, assetService, userService,
    $state, $stateParams, $translate, customerService,$document, $mdDialog,$log) {
    
    var vm = this;
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
    
    $scope.entityList = {
        count: 0,
        data: []
    };
    vm.devicesScope = null;

    function initController() {
        var user = userService.getCurrentUser();
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
        vm.loadTableData(vm.devicesScope);
    }
    function loadTableData(scope){
        $log.log($log.log("in load"))
        dashboardService.getDashboard($state.params.dashboardId).then(function success(response) {
            $log.log("response");
            $log.log(response);
           
            if(response){
                $log.log(scope);
                if (scope === 'tenant') {
                    assetService.getTenantAssets({limit: 200, textSearch: ''}, true, null, false).
                    then(function success(response_data) {
                        $log.log(response_data);
                        getAssetList(response_data,response.dataModelObjectId);
                    },function fail(){});
                }else if (scope === 'customer' || scope === 'customer_user') {
                    assetService.getCustomerAssets(customerId, {limit: 200, textSearch: ''}, true, null, false).
                    then(function success(response_data) {
                        $log.log(response);
                        getAssetList(response_data,response.dataModelObjectId)
                    },function fail(){});
                }
            }
        },function fail(){
        });
    }
    function getAssetList(result,dataObjectId){
        $log.log(result.data)
        $log.log(dataObjectId);
        var list = [];
        if(result.data.length > 0){
            $scope.showList = true;
            angular.forEach(result.data, function(value){
                $log.log("value");
                $log.log(value);
                if(value.dataModelObjectId.id == dataObjectId){
                    list.push(value);
                }
            });
            $scope.loadingData = false;
            var entitySortList = $filter('orderBy')(list, $scope.query.order);
            var startIndex = $scope.query.limit * ($scope.query.page - 1);
            if ($scope.query.search != null) {

                entitySortList = $filter('filter')(list, function(data) {
                    if ($scope.query.search) {
                        return data.name.toLowerCase().indexOf($scope.query.search.toLowerCase()) > -1 || data.type.toLowerCase().indexOf($scope.query.search.toLowerCase()) > -1;
                    } else {
                        return true;
                    }
                });
                entitySortList = $filter('orderBy')(entitySortList, $scope.query.order);
                if ($scope.query.search != '') {startIndex =0;}
            }
            var entityPaginatedata = entitySortList.slice(startIndex, startIndex + $scope.query.limit);
            $scope.entityList = {
                count: list.length,
                data: entityPaginatedata
            };
            $log.log($scope.entityList);
        }else{
            $scope.showList = false;
        }
    }
    function deviceDetailFunc($event,list){
        $rootScope.$emit("CallTableDetailDeviceOnDashboard", [$event, list]);
    }

    var assetAdd = $rootScope.$on("CallAddAssetDialog", function($event){
        $scope.addAsset($event);
     });
 
     $scope.$on('$destroy', assetAdd);
    
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