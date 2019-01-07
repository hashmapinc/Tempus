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
import AddAssetController from './add-asset.controller.js';



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
    vm.assetScope = null;

    function initController() {
        var user = userService.getCurrentUser();
        if (user.authority === 'CUSTOMER_USER') {
            vm.assetScope = 'customer_user';
            customerId = user.customerId;
        }else {
            vm.assetScope = 'tenant';
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
        if($state.current.url != '/:widgetsBundleId/widgetTypes'){
            vm.loadTableData(vm.assetScope);
        }

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
                if(value.dataModelObjectId && value.dataModelObjectId.id == dataObjectId){
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

    var displayAsset = $rootScope.$on("displayAsset", function(){
        initController();
    });
    $scope.$on('$destroy', displayAsset);
    
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

    $scope.onPaginate = function() {
        initController();
    }
    
}