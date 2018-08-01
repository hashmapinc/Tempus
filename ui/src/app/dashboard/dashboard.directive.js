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
export default function DashboardDirective($compile, $templateCache, $translate, types, toast, dashboardService, $log, datamodelService) {
    var linker = function (scope, element) {
        var template = $templateCache.get(dashboardFieldsetTemplate);
        element.html(template);
        scope.publicLink = null;
        scope.dataModels = null;
        scope.landingDashboard = false;
        scope.showAssetsList = false;
        scope.listOfDataModel = [];
        scope.dataModelView = null;
        scope.types = types;
        scope.listOfDataModelAssets = [];
        scope.$watch('dashboard', function(newVal) {
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
            scope.dataModels = datamodelService.listDatamodels();
            scope.dataModels.then(function (data) {
                scope.listOfDataModel = data;
            }, function (error) {
                $log.error(error);
            }) 
        }

        /**
         * Fetch Assets of data model
         * @param dataModelId
         */
        scope.loadDataModelAssets = function(){
            scope.listOfDataModelAssets = [];
            scope.dataModelsAssets = datamodelService.getDatamodelObjects(scope.dataModelView.id.id);
            scope.dataModelsAssets.then(function (data) {
                angular.forEach(data, function (list) {
                    if (list.type === "Asset") {
                        scope.listOfDataModelAssets.push(list);
                    }
                  });
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
