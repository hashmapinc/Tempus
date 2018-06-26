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

import angularStorage from 'angular-storage';

export default angular.module('tempus.api.datamodel', [
    angularStorage])
    .factory('datamodelService', DatamodelService)
    .name;

/*@ngInject*/
function DatamodelService($http, $q, $rootScope, adminService, dashboardService, toast, store) {


    var service = {
        setDataModelData:setDataModelData,
        clearDataModelData:clearDataModelData,
        getDataModelData:getDataModelData,
        saveDataModel:saveDataModel,
        listDataModel:listDataModel

    }

    return service;

    function setDataModelData(dataModel) {
        store.set('data_model', dataModel);
        //var data = getDataModelData();

    }

    function clearDataModelData() {
        store.remove('data_model');
    }

    function getDataModelData() {
        return store.get('data_model');
    }

    function saveDataModel(dataModeldata) {
        var deferred = $q.defer();
        var url = '/api/data-model';
        $http.post(url, dataModeldata).then(function success(response) {
            deferred.resolve(response);
        }, function fail(response) {
            deferred.reject(response);
        });
        return deferred.promise;
    }

    function listDataModel() {
        var deferred = $q.defer();
        var url = '/api/data-model';
        $http.get(url).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;


    }


}
