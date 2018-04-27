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
/* eslint-disable import/no-unresolved, import/default */

//import AliasController from '../api/alias-controller';

import './tempusboard.scss';

/*@ngInject*/
export function TempusboardController($scope, $log, $state, $stateParams, userService, deviceService, types, attributeService, $q, dashboardService, applicationService, entityService) {
	var vm = this;
    var customerId = $stateParams.customerId;

	vm.types = types;
    vm.deviceSelected = false;
    vm.dashboardCtx = {
        state: null,
        stateController: {
            openRightLayout: false
            }
    };

	vm.assetSelected = function(device){
        vm.widgetsSet = [];
        applicationService.getApplicationsByDeviceType(device.type.toLowerCase())
            .then(function success(applications) {
                applications.forEach(function(application){
                    if(application.isValid){
                         dashboardService.getDashboard(application.miniDashboardId.id)
                        .then(function success(dashboard) {
                            vm.deviceSelected = true;
                          //  vm.dashboard = dashboardUtils.validateAndUpdateDashboard(dashboard);
                            vm.dashboardConfiguration = dashboard.configuration;
                            vm.dashboardCtx.dashboard = dashboard;
                           // vm.dashboardCtx.dashboardTimewindow = vm.dashboardConfiguration.timewindow;
                          //  vm.dashboardCtx.aliasController = new AliasController($scope, $q, $filter, utils,
                            //    types, entityService, null, vm.dashboardConfiguration.entityAliases);
                          vm.timeseriesWidgetTypes = dashboard.configuration.widgets;
                          var widgetObj = {'widget': dashboard.configuration.widgets, 'name': application.name, 'dashboard': 'dashboards/'+application.dashboardId.id};
                          vm.widgetsSet.push(widgetObj);
                        }, function fail() {
                            vm.configurationError = true;
                        });
                    }
                })                
               
            }, function fail() {
                vm.configurationError = true;
            });
	}

	var pageLink = {limit: 100};

    var subscriptionIdMap = {};
//	var entityAttributesSubscriptionMap = {};

    function success(attributes, update, apply) {
        vm.last_telemetry = attributes;
        vm.devices.forEach(function(device, index, theArray){
            if(device.subscriptionId === attributes.subscriptionId){
                theArray[index].data = attributes.data
                $log.log(theArray);
            }
        })

        if (!update) {
            $scope.selectedAttributes = [];
        }
        if (apply) {
            $scope.$digest();
        }
    }

	var getEntityAttributes = function(forceUpdate, reset) {
        if ($scope.attributesDeferred) {
            $scope.attributesDeferred.resolve();
        }
        if ($scope.entityId && $scope.entityType && $scope.attributeScope) {
            if (reset) {
                $scope.attributes = {
                    count: 0,
                    data: []
                };
            }
            $scope.checkSubscription();
            $scope.attributesDeferred = attributeService.getEntityAttributes($scope.entityType, $scope.entityId, $scope.attributeScope.value,
                $scope.query, function(attributes, update, apply) {
                    success(attributes, update || forceUpdate, apply);
                }
            );
        } else {
            var deferred = $q.defer();
            $scope.attributesDeferred = deferred;
            success({
                count: 0,
                data: []
            });
            deferred.resolve();
        }
    }
    $scope.checkSubscription = function() {
        var newSubscriptionId = null;
        if ($scope.entityId && $scope.entityType) {
            newSubscriptionId = attributeService.subscribeForEntityAttributes($scope.entityType, $scope.entityId, $scope.attributeScope.value);
        }
        if ($scope.subscriptionId && $scope.subscriptionId != newSubscriptionId) {
            //attributeService.unsubscribeForEntityAttributes($scope.subscriptionId);
        }
        $scope.subscriptionId = newSubscriptionId;
        subscriptionIdMap[$scope.entityId] = newSubscriptionId;
        $log.log(subscriptionIdMap);
    }

    var user = userService.getCurrentUser();
    if (user.authority === 'CUSTOMER_USER') {
        vm.devicesScope = 'customer_user';
        customerId = user.customerId;
    }
    else if (user.authority === 'TENANT_ADMIN') {
        vm.devicesScope = 'tenant';
    }

    if (vm.devicesScope === 'tenant') {
       
        var devices = deviceService.getTenantDevices(pageLink, true, null);
        
    }
    else if (vm.devicesScope === 'customer' || vm.devicesScope === 'customer_user') {
        devices = deviceService.getCustomerDevices(customerId, pageLink, true, null);
    }

	devices.then(function (data) {
		$log.log(data);
		vm.devices = data.data;

		$scope.entityType = 'DEVICE';
		$scope.attributeScope = {value:'LATEST_TELEMETRY'};

        deviceService.getDeviceTypes({ignoreLoading: true}).then(
            function success(deviceTypes) {
                vm.allDeviceTypes = [];
                deviceTypes.forEach(function(deviceType){
                    for (var i = 0; i < vm.devices.length; i++) {
                        if (vm.devices[i].type === deviceType.type){
                            entityService.getEntityKeys('DEVICE', vm.devices[i].id.id, null, 'timeseries', {ignoreLoading: true}).then(
                                function success(keys) {
                                     vm.allDeviceTypes.push({
                                        type: deviceType.type,
                                        selectedItem: null,
                                        selected: keys.slice(0,4),
                                        tags: keys
                                     });
                                },
                                function fail() {
                                    vm.configurationError = true;
                                }
                            );
                            break;
                        }
                    }
                })
                vm.devices.forEach(function(device, index, theArray){
                    theArray[index].subscriptionId = $scope.entityType + device.id.id + $scope.attributeScope.value;
                    $scope.query = {limit:100, order:"key", page:1, search:null};
                    $scope.entityId = device.id.id;
                    getEntityAttributes(true, false);
                })
            }, function fail() {
                vm.configurationError = true;
        });

        vm.searchText = null;
		
	vm.assetSelected(vm.devices[0]);
		
	}, function() {
		$log.log('Failed: ');
	})

    vm.chipSearch = function(text, chipSet) {
      return chipSet.filter(function(object) {
        if (angular.isString(text)) {
          return object.search(text) > -1;
        } else {
          return false;
        }
      });
    };
    
    vm.transformChip = function(chip) {
      return chip;
    };
    

    $scope.$on('$destroy', function() {
        if ($scope.subscriptionId) {
            attributeService.unsubscribeForEntityAttributes($scope.subscriptionId);
        }
    });
    $scope.$watch("vm.allDeviceTypes", function(newValue, oldValue){
        if(newValue != oldValue){
            newValue.forEach(function(deviceType){
                vm.devices.forEach(function(device){
                    if(deviceType.type === device.type){
                        device.selectedTags = deviceType.selected;
                    }
                })
            })
        }
    }, true);

}
