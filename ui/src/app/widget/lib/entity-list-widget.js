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

function AddAssetController (customerService, $state, dashboardService, datamodelService, userService,
    $rootScope, assetService, $mdDialog, entityRelationService, attributeService){
    var vm = this;
    vm.name = null;
    vm.dataModelObject = null;
    vm.parentName = null;
    vm.parentId = null;
    vm.addAsset = addAsset;
    vm.cancel = cancel;
    vm.showCustList = false;
    vm.showParent =  false;
    vm.showChildOf =  false;
    vm.assetList= [];
    vm.attributeList =[];
    vm.dashboardDetail = null;
    vm.associatedAsset = null;
    vm.loadrelationModel =loadrelationModel;
    var currentuser = userService.getCurrentUser();
    vm.disableFlag = false;
    initController();
    function initController(){
        var pageSize = 10;
        dashboardService.getDashboard($state.params.dashboardId).then(function success(response) {
            if(response){
                vm.dashboardDetail = response;
                vm.isParent = getDataModelObjectDetails(response.assetLandingInfo.dataModelObjectId.id);
                vm.isParent.then(function success(object_response) {
                    if(object_response.data){ 
                        vm.name = object_response.data.name;
                        if(object_response.data.attributeDefinitions.length > 0){
                            vm.attributeList = object_response.data.attributeDefinitions;
                        }
                        
                        if(object_response.data.parentId) {
                            var parentDetails = getDataModelObjectDetails(object_response.data.parentId.id);
                            parentDetails.then(function success(parentResponse) {
                                vm.parentName = parentResponse.data.name;
                                vm.parentId = parentResponse.data.id.id;
                                if (currentuser.authority != 'CUSTOMER_USER') {
                                    vm.showParent =  true;
                                    vm.showChildOf =  true;
                                }else{vm.showParent =  false;
                                    vm.showChildOf =  true;}
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
        vm.assetList = [];
        assetService.getAssetByObjectId(vm.parentId).then(function success(parentList){
            angular.forEach(parentList, function(value){
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
            type:vm.name.toLowerCase(),
            attributes:vm.attributes,
            dataModelObjectId:{
                entityType:"DATA_MODEL_OBJECT",
                id:vm.dashboardDetail.assetLandingInfo.dataModelObjectId.id
            }
        }
        if(currentuser.authority === 'CUSTOMER_USER'){
            requestObj.customerId ={};
            requestObj.customerId.entityType = "CUSTOMER";
            requestObj.customerId.id = currentuser.customerId;
        }else {
            if (vm.user){
                requestObj.customerId ={};
                requestObj.customerId.entityType = "CUSTOMER";
                requestObj.customerId.id = vm.user.id.id 
            }
        }
        assetService.saveAsset(requestObj).then(
            function success(response) {
                if(response && response.additionalInfo.parentId && vm.associatedAsset){
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
                var att =[];
                if(vm.attributes){
                    angular.forEach(vm.attributes, function(value,key){
                        var a ={
                            key :key,
                            value:value
                        }
                        att.push(a);
                    });
                    attributeService.saveEntityAttributes("ASSET",response.id.id,"SERVER_SCOPE",att).then(function success(){},
                    function fail(){
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
    $state, $stateParams, $translate, customerService,$document, $mdDialog) {
    
    var vm = this;
    $scope.query = {
        order: 'name',
        limit: 10,
        page: 1,
        search: null
    };
    vm.entityDetailFunc = entityDetailFunc;
    vm.deleteAsset = deleteAsset;
    vm.loadTableData = loadTableData;
    $scope.showList = false;
    
    
    var customerId = $stateParams.customerId;
    
    initController();
    
    $scope.entityList = {
        count: 0,
        data: []
    };
    vm.devicesScope = null;

    function initController() {
        var user = userService.getCurrentUser();
        if (user.authority === 'CUSTOMER_USER') {
            vm.devicesScope = 'customer_user';
            customerId = user.customerId;
        }else {
            vm.devicesScope = 'tenant';
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
        
        vm.loadTableData(vm.devicesScope);
    }
    function loadTableData(scope){
        dashboardService.getDashboard($state.params.dashboardId).then(function success(response) {           
            if(response){
                if (scope === 'tenant') {
                    assetService.getTenantAssets({limit: 200, textSearch: ''}, true, null, false).
                    then(function success(response_data) {
                        getAssetList(response_data,response.assetLandingInfo.dataModelObjectId.id);
                    },function fail(){});
                }else if (scope === 'customer' || scope === 'customer_user') {
                    assetService.getCustomerAssets(customerId, {limit: 200, textSearch: ''}, true, null, false).
                    then(function success(response_data) {
                        getAssetList(response_data,response.assetLandingInfo.dataModelObjectId.id)
                    },function fail(){});
                }
            }
        },function fail(){
        });
    }
    function getAssetList(result,dataObjectId){
        var list = [];
        if(result.data.length > 0){
            $scope.showList = true;
            angular.forEach(result.data, function(value){
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
        }else{
            $scope.showList = false;
        }
    }
    function entityDetailFunc($event,list){
        $rootScope.$emit("CallTableDetailDeviceOnDashboard", [$event, list]);
    }

    var assetAdd = $rootScope.$on("addAssetByEntity", function($event){
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

    function deleteAsset($event,item) {
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('asset.delete-asset-title', {assetName: item.name}))
            .htmlContent($translate.instant('asset.delete-asset-text'))
            .ariaLabel($translate.instant('grid.delete-item'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            assetService.deleteAsset(item.id.id).then(function success() {
                initController();
            });
        },
        function () {
        });
    }
    
}