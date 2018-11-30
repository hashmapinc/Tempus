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
export default function AddAssetController (customerService, $state, dashboardService, datamodelService, userService,
    $rootScope, assetService, $mdDialog, entityRelationService, attributeService,$log){
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
    $log.log("here")
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
                        $log.log('vm.attributeList');
                        $log.log(vm.attributeList);
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