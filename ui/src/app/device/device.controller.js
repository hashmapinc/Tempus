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
/* eslint-disable import/no-unresolved, import/default */

import addDeviceTemplate from './add-device.tpl.html';
import deviceCard from './device-card.tpl.html';
import assignToCustomerTemplate from './assign-to-customer.tpl.html';
import assignDatamodelToDevice from './assign-datamodel-to-device.tpl.html';
import addDevicesToCustomerTemplate from './add-devices-to-customer.tpl.html';
import deviceCredentialsTemplate from './device-credentials.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export function DeviceCardController(types) {

    var vm = this;

    vm.types = types;

    vm.isAssignedToCustomer = function() {
        if (vm.item && vm.item.customerId && vm.parentCtl.devicesScope === 'tenant' &&
            vm.item.customerId.id != vm.types.id.nullUid && !vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }

    vm.isPublic = function() {
        if (vm.item && vm.item.assignedCustomer && vm.parentCtl.devicesScope === 'tenant' && vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }

}


/*@ngInject*/
export function DeviceController($rootScope,userService, deviceService, customerService, $state, $stateParams,
                                 $document, $mdDialog, $q, $translate, types, $scope, $filter, entityRelationService, $log) {

    var customerId = $stateParams.customerId;

    var deviceActionsList = [];

    var deviceGroupActionsList = [];

    var vm = this;



    vm.types = types;

    $scope.tableView = true;


    $scope.devices = {
        count: 0,
        data: []
    };

    $scope.query = {
        order: 'name',
        limit: 15,
        page: 1,
        search: null
    };


    vm.deviceGridConfig = {
        deleteItemTitleFunc: deleteDeviceTitle,
        deleteItemContentFunc: deleteDeviceText,
        deleteItemsTitleFunc: deleteDevicesTitle,
        deleteItemsActionTitleFunc: deleteDevicesActionTitle,
        deleteItemsContentFunc: deleteDevicesText,

        saveItemFunc: saveDevice,

        getItemTitleFunc: getDeviceTitle,

        itemCardController: 'DeviceCardController',
        itemCardTemplateUrl: deviceCard,
        parentCtl: vm,

        actionsList: deviceActionsList,
        groupActionsList: deviceGroupActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addDeviceTemplate,

        addItemText: function() { return $translate.instant('device.add-device-text') },
        noItemsText: function() { return $translate.instant('device.no-devices-text') },
        itemDetailsText: function() { return $translate.instant('device.device-details') },
        isDetailsReadOnly: isCustomerUser,
        isSelectionEnabled: function () {
            return !isCustomerUser();
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.deviceGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.deviceGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.devicesScope = $state.$current.data.devicesType;

    vm.assignToDatamodel = assignToDatamodel;
    vm.assignToCustomer = assignToCustomer;
    vm.unassignFromCustomer = unassignFromCustomer;
    vm.manageCredentials = manageCredentials;
    var user = userService.getCurrentUser();

    initController();

    function initController() {
        var fetchDevicesFunction = null;
        var deleteDeviceFunction = null;
        var refreshDevicesParamsFunction = null;



        if (user.authority === 'CUSTOMER_USER') {
            vm.devicesScope = 'customer_user';
            customerId = user.customerId;
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

        if (vm.devicesScope === 'tenant') {
            fetchDevicesFunction = function (pageLink, deviceType,pageNumber) {
                $log.log("fetchDevicesFunction")
                $log.log($scope.query.page)
                $log.log(pageNumber)
                if($scope.query.page == 1){
                    return deviceService.getTenantDevices(pageLink, true, null, deviceType,0);
                }else {
                    return deviceService.getTenantDevices(pageLink, true, null, deviceType,$scope.query.page - 1);
                }

            };
            deleteDeviceFunction = function (deviceId) {
                return deviceService.deleteDevice(deviceId);
            };
            refreshDevicesParamsFunction = function() {
                return {"topIndex": vm.topIndex};
            };

            deviceActionsList.push(
                {
                    onAction: function ($event, item) {
                        assignToCustomer($event, [ item.id.id ]);
                    },
                    name: function() { return $translate.instant('action.assign') },
                    details: function() { return $translate.instant('device.assign-to-customer') },
                    icon: "assignment_ind",
                    isEnabled: function(device) {
                        return device && (!device.customerId || device.customerId.id === types.id.nullUid);
                    }
                }
            );

            deviceActionsList.push(
                {
                    onAction: function ($event, item) {
                        unassignFromCustomer($event, item, false);
                    },
                    name: function() { return $translate.instant('action.unassign') },
                    details: function() { return $translate.instant('device.unassign-from-customer') },
                    icon: "assignment_return",
                    isEnabled: function(device) {
                        return device && device.customerId && device.customerId.id !== types.id.nullUid && !device.assignedCustomer.isPublic;
                    }
                }
            );


            deviceActionsList.push(
                {
                    onAction: function ($event, item) {
                        manageCredentials($event, item);
                    },
                    name: function() { return $translate.instant('device.credentials') },
                    details: function() { return $translate.instant('device.manage-credentials') },
                    icon: "security"
                }
            );

            deviceActionsList.push(
                {
                    onAction: function ($event, item) {
                        vm.grid.deleteItem($event, item);
                    },
                    name: function() { return $translate.instant('action.delete') },
                    details: function() { return $translate.instant('device.delete') },
                    icon: "delete"
                }
            );

            deviceGroupActionsList.push(
                {
                    onAction: function ($event, items) {
                        assignDevicesToCustomer($event, items);
                    },
                    name: function() { return $translate.instant('device.assign-devices') },
                    details: function(selectedCount) {
                        return $translate.instant('device.assign-devices-text', {count: selectedCount}, "messageformat");
                    },
                    icon: "assignment_ind"
                }
            );

            deviceGroupActionsList.push(
                {
                    onAction: function ($event) {
                        vm.grid.deleteItems($event);
                    },
                    name: function() { return $translate.instant('device.delete-devices') },
                    details: deleteDevicesActionTitle,
                    icon: "delete"
                }
            );



        } else if (vm.devicesScope === 'customer' || vm.devicesScope === 'customer_user') {
            fetchDevicesFunction = function (pageLink, deviceType) {
                if($scope.query.page == 1){
                    return deviceService.getCustomerDevices(customerId, pageLink, true, null, deviceType, 0);
                }else{
                    return deviceService.getCustomerDevices(customerId, pageLink, true, null, deviceType, $scope.query.page - 1);
                }

            };
            deleteDeviceFunction = function (deviceId) {
                return deviceService.unassignDeviceFromCustomer(deviceId);
            };
            refreshDevicesParamsFunction = function () {
                return {"customerId": customerId, "topIndex": vm.topIndex};
            };

            if (vm.devicesScope === 'customer') {
                deviceActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, false);
                        },
                        name: function() { return $translate.instant('action.unassign') },
                        details: function() { return $translate.instant('device.unassign-from-customer') },
                        icon: "assignment_return",
                        isEnabled: function(device) {
                            return device && !device.assignedCustomer.isPublic;
                        }
                    }
                );


                deviceActionsList.push(
                    {
                        onAction: function ($event, item) {
                            manageCredentials($event, item);
                        },
                        name: function() { return $translate.instant('device.credentials') },
                        details: function() { return $translate.instant('device.manage-credentials') },
                        icon: "security"
                    }
                );

                deviceGroupActionsList.push(
                    {
                        onAction: function ($event, items) {
                            unassignDevicesFromCustomer($event, items);
                        },
                        name: function() { return $translate.instant('device.unassign-devices') },
                        details: function(selectedCount) {
                            return $translate.instant('device.unassign-devices-action-title', {count: selectedCount}, "messageformat");
                        },
                        icon: "assignment_return"
                    }
                );

                vm.deviceGridConfig.addItemAction = {
                    onAction: function ($event) {
                        addDevicesToCustomer($event);
                    },
                    name: function() { return $translate.instant('device.assign-devices') },
                    details: function() { return $translate.instant('device.assign-new-device') },
                    icon: "add"
                };


            } else if (vm.devicesScope === 'customer_user') {
                deviceActionsList.push(
                    {
                        onAction: function ($event, item) {
                            manageCredentials($event, item);
                        },
                        name: function() { return $translate.instant('device.credentials') },
                        details: function() { return $translate.instant('device.view-credentials') },
                        icon: "security"
                    }
                );

                //vm.deviceGridConfig.addItemAction = {};
            }
        }

        vm.deviceGridConfig.refreshParamsFunc = refreshDevicesParamsFunction;
        vm.deviceGridConfig.fetchItemsFunc = fetchDevicesFunction;
        vm.deviceGridConfig.deleteItemFunc = deleteDeviceFunction;

    }

    loadTableData();

    function loadTableData() {
        $log.log("loadTableData")
        var promise = vm.deviceGridConfig.fetchItemsFunc({limit: $scope.query.limit, textSearch: ''}, false);
        if(promise) {
            promise.then(function success(items) {
                $log.log(items)
                $scope.devices.data = [];
                var deviceSortList = $filter('orderBy')(items.data, $scope.query.order);
                if ($scope.query.search != null) {

                    deviceSortList = $filter('filter')(items.data, function(data) {
                        if ($scope.query.search) {
                            return data.name.toLowerCase().indexOf($scope.query.search.toLowerCase()) > -1 || data.type.toLowerCase().indexOf($scope.query.search.toLowerCase()) > -1;
                        } else {
                            return true;
                        }
                    });
                    deviceSortList = $filter('orderBy')(deviceSortList, $scope.query.order);
                }

                var devicePaginatedata = deviceSortList;
                $scope.devices = {
                    count: items.totalElements,
                    data: devicePaginatedata
                };
                },
            )

        }
    }


    $scope.enterFilterMode = function() {

        $scope.query.search = '';
        //loadTableData();
    }

    $scope.exitFilterMode = function() {

        $scope.query.search = null;
        loadTableData();
    }

    $scope.resetFilter = function() {

        $scope.query = {
            order: 'name',
            limit: $scope.query.limit,
            page: 1,
            search: null
        };

        loadTableData();
    }

    vm.loadTableData = loadTableData;
    $scope.$watch("query.search", function(newVal, prevVal) {
        if (!angular.equals(newVal, prevVal) && $scope.query.search != null) {

            loadTableData();
        }
    });

    $scope.onReorder = function() {

        loadTableData();
    }

    $scope.onPaginate = function(page) {
        $scope.query.page = page;
        loadTableData();
    }

    $scope.deleteDevice = function($event,item) {
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(deleteDeviceTitle(item))
            .htmlContent(deleteDeviceText(item))
            .ariaLabel($translate.instant('grid.delete-item'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            vm.deviceGridConfig.deleteItemFunc(item.id.id).then(function success() {
                $scope.resetFilter();

            });
        },
        function () {
        });
    }

    $scope.addDevice = function($event) {

        $mdDialog.show({
            controller: 'AddItemController',
            controllerAs: 'vm',
            templateUrl: 'device/add-device.tpl.html',
            parent: angular.element($document[0].body),
            locals: {saveItemFunction: vm.deviceGridConfig.saveItemFunc},
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
            $scope.resetFilter();
        }, function () {
        });

    }

    $scope.deviceDetailFunc = function($event,device) {
        $rootScope.$emit("CallTableDetailDevice", [$event, device]);
    }

    function deleteDeviceTitle(device) {
        return $translate.instant('device.delete-device-title', {deviceName: device.name});
    }

    function deleteDeviceText() {
        return $translate.instant('device.delete-device-text');
    }

    function deleteDevicesTitle(selectedCount) {
        return $translate.instant('device.delete-devices-title', {count: selectedCount}, 'messageformat');
    }

    function deleteDevicesActionTitle(selectedCount) {
        return $translate.instant('device.delete-devices-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteDevicesText () {
        return $translate.instant('device.delete-devices-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function getDeviceTitle(device) {
        return device ? device.name : '';
    }

    function saveDevice(device) {
        if(vm.devicesScope == 'customer_user'){
            device.customerId =null;
            device.customerId = {
                id:customerId,
                entityType:'CUSTOMER'
            }
        }
        var deferred = $q.defer();
        deviceService.saveDevice(device).then(
            function success(savedDevice) {
                $rootScope.$broadcast('deviceSaved');
                var devices = [ savedDevice ];
                customerService.applyAssignedCustomersInfo(devices).then(
                    function success(items) {
                        if (items && items.length == 1) {
                            deferred.resolve(items[0]);
                        } else {
                            deferred.reject();
                        }
                    },
                    function fail() {
                        deferred.reject();
                    }
                );
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function isCustomerUser() {
        return vm.devicesScope === 'customer_user';
    }

    function assignToCustomer($event, deviceIds) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        customerService.getCustomers({limit: pageSize, textSearch: ''}).then(
            function success(_customers) {
                var customers = {
                    pageSize: pageSize,
                    data: _customers.data,
                    nextPageLink: _customers.nextPageLink,
                    selection: null,
                    hasNext: _customers.hasNext,
                    pending: false
                };
                if (customers.hasNext) {
                    customers.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AssignDeviceToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: assignToCustomerTemplate,
                    locals: {deviceIds: deviceIds, customers: customers},
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function () {
                    vm.grid.refreshList();
                    loadTableData();
                }, function () {
                });
            },
            function fail() {
            });
    }


    function assignToDatamodel($event, device) {
        if ($event) {
            $event.stopPropagation();
        }
        $mdDialog.show({
           controller: 'AssignDatamodelToDeviceController',
           controllerAs: 'vm',
           templateUrl: assignDatamodelToDevice,
           locals: {device: device[0]},
           parent: angular.element($document[0].body),
           fullscreen: true,
                    targetEvent: $event
           }).then(function () {
                    vm.grid.refreshList();
                    loadTableData();
            }, function () {
         });

    }

    function addDevicesToCustomer($event) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        deviceService.getTenantDevices({limit: pageSize, textSearch: ''}, false).then(
            function success(_devices) {
                var devices = {
                    pageSize: pageSize,
                    data: _devices.data,
                    nextPageLink: _devices.nextPageLink,
                    selections: {},
                    selectedCount: 0,
                    hasNext: _devices.hasNext,
                    pending: false
                };
                if (devices.hasNext) {
                    devices.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AddDevicesToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: addDevicesToCustomerTemplate,
                    locals: {customerId: customerId, devices: devices},
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function () {
                    vm.grid.refreshList();
                }, function () {
                });
            },
            function fail() {
            });
    }

    function assignDevicesToCustomer($event, items) {
        var deviceIds = [];
        for (var id in items.selections) {
            deviceIds.push(id);
        }
        assignToCustomer($event, deviceIds);
    }

    function unassignFromCustomer($event, device, isPublic) {
        if ($event) {
            $event.stopPropagation();
        }
        var title;
        var content;
        var label;
        if (isPublic) {
            title = $translate.instant('device.make-private-device-title', {deviceName: device.name});
            content = $translate.instant('device.make-private-device-text');
            label = $translate.instant('device.make-private');
        } else {
            title = $translate.instant('device.unassign-device-title', {deviceName: device.name});
            content = $translate.instant('device.unassign-device-text');
            label = $translate.instant('device.unassign-device');
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            deviceService.unassignDeviceFromCustomer(device.id.id).then(function success(item) {
              if(item.dataModelObjectId.id !== vm.types.id.nullUid) {
                item.dataModelObjectId.id = vm.types.id.nullUid;
                deviceService.saveDevice(item);
                if(item.additionalInfo == null) {
                    entityRelationService.findInfoByFrom(item.id.id,'DEVICE').then(function success(itemDevice) {
                        if(itemDevice.length > 0) {
                            entityRelationService.deleteRelation(itemDevice[0].from.id, itemDevice[0].from.entityType, 'Contains', itemDevice[0].to.id, itemDevice[0].to.entityType);
                        }
                    });
                }
              }
                vm.grid.refreshList();
                loadTableData();
            });
        });
    }

    function unassignDevicesFromCustomer($event, items) {
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('device.unassign-devices-title', {count: items.selectedCount}, 'messageformat'))
            .htmlContent($translate.instant('device.unassign-devices-text'))
            .ariaLabel($translate.instant('device.unassign-device'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            var tasks = [];
            for (var id in items.selections) {
                tasks.push(deviceService.unassignDeviceFromCustomer(id));
            }
            $q.all(tasks).then(function () {
                vm.grid.refreshList();
            });
        });
    }

    function manageCredentials($event, device) {
        if ($event) {
            $event.stopPropagation();
        }
        $mdDialog.show({
            controller: 'ManageDeviceCredentialsController',
            controllerAs: 'vm',
            templateUrl: deviceCredentialsTemplate,
            locals: {deviceId: device.id.id, isReadOnly: isCustomerUser()},
            parent: angular.element($document[0].body),
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
        }, function () {
        });
    }
}