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
/*@ngInject*/
export default function AssignDeviceToCustomerController(customerService, entityRelationService,  datamodelService, deviceService, $mdDialog, $q, deviceIds, customers) {

    var vm = this;

    vm.customers = customers;
    vm.searchText = '';

    vm.assign = assign;
    vm.cancel = cancel;
    vm.isCustomerSelected = isCustomerSelected;
    vm.hasData = hasData;
    vm.noData = noData;
    vm.searchCustomerTextUpdated = searchCustomerTextUpdated;
    vm.toggleCustomerSelection = toggleCustomerSelection;
    vm.dmo = '';
    vm.dataModelObject = '';
    vm.getDataModelObjectParent = getDataModelObjectParent;
    vm.parentObject = '';
    vm.entityName = '';

    vm.theCustomers = {
        getItemAtIndex: function (index) {
            if (index > vm.customers.data.length) {
                vm.theCustomers.fetchMoreItems_(index);
                return null;
            }
            var item = vm.customers.data[index];
            if (item) {
                item.indexNumber = index + 1;
            }
            return item;
        },

        getLength: function () {
            if (vm.customers.hasNext) {
                return vm.customers.data.length + vm.customers.nextPageLink.limit;
            } else {
                return vm.customers.data.length;
            }
        },

        fetchMoreItems_: function () {
            if (vm.customers.hasNext && !vm.customers.pending) {
                vm.customers.pending = true;
                customerService.getCustomers(vm.customers.nextPageLink).then(
                    function success(customers) {
                        vm.customers.data = vm.customers.data.concat(customers.data);
                        vm.customers.nextPageLink = customers.nextPageLink;
                        vm.customers.hasNext = customers.hasNext;
                        if (vm.customers.hasNext) {
                            vm.customers.nextPageLink.limit = vm.customers.pageSize;
                        }
                        vm.customers.pending = false;
                    },
                    function fail() {
                        vm.customers.hasNext = false;
                        vm.customers.pending = false;
                    });
            }
        }
    };

    function cancel() {
        $mdDialog.cancel();
    }

    function assign() {
        var tasks = [];
        for (var i=0;i<deviceIds.length;i++) {
         tasks.push(deviceService.assignDeviceToCustomer(vm.customers.selection.id.id, deviceIds[i]));
        }


       $q.all(tasks).then(function () {
       for (var i=0;i<deviceIds.length;i++) {
            if(angular.isDefined(vm.dataModelname)){
                deviceService.getDevice(deviceIds[i]).then(function success(item){
                item.dataModelObjectId.id = vm.selectDataModelId;
                deviceService.saveDevice(item);
                if(angular.isDefined(vm.entityName) && vm.entityName !== "") {
                 var relation ={
                       type:"Contains",
                         from: {
                           id: item.id.id,
                           entityType: "DEVICE"
                        },
                         to:{
                           entityType: "ASSET",
                           id: vm.entityName
                     }
                  }
                 entityRelationService.saveRelation(relation);

                }
               })
           }
        }
           $mdDialog.hide();
        });
    }

    function getDataModelObjectParent() {
        datamodelService.getDatamodelObject(vm.dataModelObject).then(function success(item){
            vm.selectDataModelId = item.data.id.id;
            if(angular.isDefined(item.data.parentId)) {
               datamodelService.getDatamodelObject(item.data.parentId.id).then(function success(item){
                vm.parentObject = item.data.name;
                if(item.data.type.toLowerCase() == "asset") {
                  vm.showEnt = true;
                  datamodelService.getDatamodelObjectAttributes(item.data.id.id).then(function success(item){
                    item.forEach(dmo => { //
                       vm.entityList[dmo.id.id] = dmo.name;
                    });
                  })
                 }
               })
            }
        })
    }


    function noData() {
        return vm.customers.data.length == 0 && !vm.customers.hasNext;
    }

    function hasData() {
        return vm.customers.data.length > 0;
    }

    function toggleCustomerSelection($event, customer) {
        $event.stopPropagation();
        vm.dmo ='';
        vm.parentObject = '';
        vm.dataModelObjectValues = {};
        vm.entityList = {};
        vm.showEnt = false;
        vm.dataModelObject ='';
        vm.selectDataModelId = '';

        if (vm.isCustomerSelected(customer)) {
            vm.customers.selection = null;

        } else {
          if(customer.dataModelId.id !== "13814000-1dd2-11b2-8080-808080808080") {
            var promise = datamodelService.getDatamodel(customer.dataModelId.id);
            if(promise){
                promise.then(function success(result) {
                    vm.dataModelname = result.name;
                    datamodelService.getDatamodelObjects(customer.dataModelId.id).then(function success(item){
                        vm.dmo = item;
                        vm.dmo.forEach(dmo => { //
                          if (dmo.type.toLowerCase() == 'device') {
                            vm.dataModelObjectValues[dmo.id.id] = dmo.name;
                          }

                        });

                    })
                })
            }
          }
            vm.customers.selection = customer;
        }
    }

    function isCustomerSelected(customer) {
        return vm.customers.selection != null && customer &&
            customer.id.id === vm.customers.selection.id.id;
    }

    function searchCustomerTextUpdated() {
        vm.customers = {
            pageSize: vm.customers.pageSize,
            data: [],
            nextPageLink: {
                limit: vm.customers.pageSize,
                textSearch: vm.searchText
            },
            selection: null,
            hasNext: true,
            pending: false
        };
    }
}
