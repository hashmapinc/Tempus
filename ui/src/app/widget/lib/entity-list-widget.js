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
    .directive('tbDeviceListWidget', DeviceListWidget)
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
function DeviceListWidgetController($rootScope,$scope,$filter,$log,deviceService,types,assetService) {
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
    $log.log("in devices");
    $log.log(vm.config)
    vm.loadTableData();
    function loadTableData(){
        if(vm.config.config.datasources && vm.config.config.datasources.length > 0){
            if(vm.config.config.datasources[0].dataKeys[0] == 'Assets'){
                promise = assetService.getTenantAssets({limit: 200, textSearch: ''}, true, null, false);
            }else if(vm.config.config.datasources[0].dataKeys[0] == 'Devices'){
                promise = deviceService.getTenantDevices({limit: 200, textSearch: ''}, true, null, false);
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
    function deviceDetailFunc($event,device){
        $log.log($event);
        $log.log(device);
        $rootScope.$emit("CallTableDetailDevice", [$event, device]);
        //$rootScope.$emit("CallTableDetailDeviceOnDashboard", [$event, device]);
    }
}