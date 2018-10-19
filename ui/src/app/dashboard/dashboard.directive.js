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

import dashboardFieldsetTemplate from './dashboard-fieldset.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function DashboardDirective($compile, $templateCache, $translate, types, toast, dashboardService, $log, datamodelService, userService) {
    var linker = function (scope, element) {
        var template = $templateCache.get(dashboardFieldsetTemplate);
        element.html(template);
        scope.publicLink = null;
        scope.dataModels = null;
        scope.dashboardDataModel = null;
        scope.showAssetsList = false;
        scope.listOfDataModel = [];
        scope.dataModelView = null;
        scope.types = types;
        scope.listOfDataModelAssets = [];
        scope.user = userService.getCurrentUser();
        scope.$watch('dashboard', function(newVal) {
            if(newVal !=null && newVal.hasOwnProperty("title")){
                scope.theForm.editFlag = true;
            }else{
                scope.theForm.editFlag = false;
            }            
            if(scope.dashboard && scope.dashboard.type == "ASSET_LANDING_PAGE"){    
                scope.dashboard.landingDashboard = true;
                scope.loadDataModel();
                scope.loadDataModelAssets();
            }
            if (newVal) {
                if (scope.dashboard.publicCustomerId) {
                    scope.publicLink = dashboardService.getPublicDashboardLink(scope.dashboard);
                } else {
                    scope.publicLink = null;
                }
            }
        });

        scope.onPublicLinkCopied = function() {
            toast.showSuccess($translate.instant('dashboard.public-link-copied-message'), 750, angular.element(element).parent().parent(), 'bottom left');
        };

        /**
         * Fetch all the data models related to login tenant
         */
        scope.loadDataModel = function(){
            scope.listOfDataModel = [];
            var val,dataModelId;
            if(scope.dashboard && scope.dashboard.assetLandingInfo){
                dataModelId = scope.dashboard.assetLandingInfo.dataModelId.id
            }else{
                dataModelId = scope.dashboard.dataModelId;
            }
            scope.dataModels = datamodelService.listDatamodels();
            scope.dataModels.then(function (data) {
                scope.listOfDataModel = data;
                val = scope.listOfDataModel.filter(e => e.id.id === dataModelId);
                scope.dashboardDataModel = val[0];
            }, function (error) {
                $log.error(error);
            }) 
        }

        /**
         * Fetch Assets of data model
         * @param dataModelId
         */
        scope.loadDataModelAssets = function(){
            var dataModelObjectId,dataModelId;
            
            scope.listOfDataModelAssets = [];
            var val;
            if(scope.dashboard && scope.dashboard.assetLandingInfo){
                dataModelId = scope.dashboard.assetLandingInfo.dataModelId.id;
                dataModelObjectId = scope.dashboard.assetLandingInfo.dataModelObjectId.id
            }else{
                dataModelId = scope.dashboard.dataModelId;
                dataModelObjectId = scope.dashboard.dataModelObjectId
            }
            scope.dataModelsAssets = datamodelService.getDatamodelObjects(dataModelId);
            scope.dataModelsAssets.then(function (data) {
                angular.forEach(data, function (list) {
                    if (list.type === "Asset") {
                        scope.listOfDataModelAssets.push(list);
                    }
                  });
                val = scope.listOfDataModelAssets.filter(e => e.id.id === dataModelObjectId);
                scope.dashboarddataModelAsset = val[0];   
                scope.showAssets();  
            }, function (error) {
                $log.error(error);
            }) 
        }
        /**
         * Show Assets List
         */
        scope.showAssets = function(dataModel){ 
            scope.showAssetsList = true;
            scope.dataModelView = dataModel;
            if(dataModel){
                scope.dashboard.dataModelId= dataModel.id.id
            }
        }
        /**
         * Set data model asset id to dashboard
         */

        scope.setAssets = function(dataModelobj){ 
            if(dataModelobj){
                scope.dashboard.dataModelObjectId= dataModelobj.id.id
            }
        }
        
        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            dashboard: '=',
            isEdit: '=',
            customerId: '=',
            dashboardScope: '=',
            theForm: '=',
            onMakePublic: '&',
            onMakePrivate: '&',
            onManageAssignedCustomers: '&',
            onUnassignFromCustomer: '&',
            onExportDashboard: '&',
            onDeleteDashboard: '&',
            loadDataModel: '&'
        }
    };
}
