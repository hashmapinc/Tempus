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
export default function AssignDatamodelToDeviceController(customerService, types, entityRelationService, datamodelService, deviceService, $mdDialog, $q,device) {

    var vm = this;
    vm.device = device;
    vm.cancel = cancel;
    vm.getDataModelObjectParent = getDataModelObjectParent;
    vm.selectDataModelId = '';
    vm.entityName = '';
    vm.assign = assign;
    vm.parentObject = '';
    vm.type = vm.device.type;
    vm.relation = '';
    vm.types = types;

    getCustomerDataModel();
    if(vm.device.dataModelObjectId.id !== vm.types.id.nullUid) {
        vm.dataModelObject = vm.device.dataModelObjectId.id;
        if(vm.device.additionalInfo == null) {
            vm.getDataModelObjectParent();
            entityRelationService.findInfoByFrom(vm.device.id.id,'DEVICE').then(function success(itemDevice) {
                vm.relation = itemDevice[0]
                vm.entityName = vm.relation.to.id;
            });
        }
    }
    function getCustomerDataModel() {
        vm.dataModelObjectValues = {};
        var promise = customerService.getCustomer(vm.device.customerId.id);
        if(promise){
          promise.then(function success(result) {
            datamodelService.getDatamodel(result.dataModelId.id).then(function success(item){
                 vm.dataModelname = item.name;
                 datamodelService.getDatamodelObjects(item.id.id).then(function success(item){
                   vm.dmo = item;
                   vm.dmo.forEach(dmo => { //
                    if (dmo.type.toLowerCase() == 'device') {
                       vm.dataModelObjectValues[dmo.id.id] = dmo.name;
                    }
                   });
                 })
            });
          })
        }
    }

    function assign(){

        if(vm.device.dataModelObjectId.id !== vm.selectDataModelId) {
            vm.device.dataModelObjectId.id = vm.selectDataModelId;
            deviceService.saveDevice(vm.device);
        }

           if(vm.device.additionalInfo == null) {
              if(angular.isDefined(vm.entityName) && vm.entityName !== "") {
                 var relation ={
                    type:"Contains",
                     from: {
                       id: vm.device.id.id,
                       entityType: "DEVICE"
                     },
                    to:{
                      entityType: "ASSET",
                      id: vm.entityName
                    }
                 }

                if(vm.relation !== "") {
                   if(vm.relation.to.id !== vm.entityName) {
                        entityRelationService.deleteRelation(vm.relation.from.id, vm.relation.from.entityType, 'Contains', vm.relation.to.id, vm.relation.to.entityType).then(function success(){
                            entityRelationService.saveRelation(relation);
                        })
                   }
                } else {
                    entityRelationService.saveRelation(relation);
                }

             }
            }
            $mdDialog.hide();

    }


    function getDataModelObjectParent() {
        vm.entityList = {};
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

     function cancel() {
        $mdDialog.cancel();
     }



}
