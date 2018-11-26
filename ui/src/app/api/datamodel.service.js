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
import angularStorage from 'angular-storage';

export default angular.module('tempus.api.datamodel', [
    angularStorage])
    .factory('datamodelService', DatamodelService)
    .name;

/*@ngInject*/
function DatamodelService($http, $q) {
    return {
        getDatamodel:           getDatamodel,
        getDatamodelObjects:    getDatamodelObjects,
        saveDatamodel:          saveDatamodel,
        saveDatamodelObject:    saveDatamodelObject,
        listDatamodels:         listDatamodels,
        deleteDatamodelObject:  deleteDatamodelObject,
        getDatamodelObjectAttributes: getDatamodelObjectAttributes,
        getDatamodelObject:     getDatamodelObject,
        deleteDatamodel:        deleteDatamodel,
        getDatamodelObjectAttributesDeviceType:getDatamodelObjectAttributesDeviceType
    }

    // loads the datamodel objects for the datamodel with ID = datamodelID
    function getDatamodelObjects(datamodelID) {
        var deferred = $q.defer();
        var url = '/api/data-model/' + datamodelID + '/objects';
        $http.get(url).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function getDatamodelObjectAttributes(datamodelObjectID) {
        var deferred = $q.defer();
        var url = '/api/datamodelobject/assets/' + datamodelObjectID +'?limit=30';
        $http.get(url).then(function success(response) {
            deferred.resolve(response.data.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function getDatamodelObjectAttributesDeviceType(datamodelObjectID) {
        var deferred = $q.defer();
        var url = '/api/datamodelobject/devices/' + datamodelObjectID +'?limit=30';
        $http.get(url).then(function success(response) {
            deferred.resolve(response.data.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    // loads the datamodel with ID = datamodelID
    function getDatamodel(datamodelID) {
        var deferred = $q.defer();
        var url = '/api/data-model/' + datamodelID;
        $http.get(url).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    /**
     *  saves a datamodel object in the backend
     *  @param datamodelObject - datamodel object to save
     *  @param datamodelID - id of the datamodel to save datamodelObject to
     */
    function saveDatamodelObject(datamodelObject, datamodelID) {
        var deferred = $q.defer();
        var url = '/api/data-model/' + datamodelID + '/objects';
        $http.post(url, datamodelObject).then(function success(response) {
            deferred.resolve(response);
        }, function fail(response) {
            deferred.reject(response);
        });
        return deferred.promise;
    }

    // saves a datamodel in the backend
    function saveDatamodel(dataModeldata) {
        var deferred = $q.defer();
        var url = '/api/data-model';
        $http.post(url, dataModeldata).then(function success(response) {
            deferred.resolve(response);
        }, function fail(response) {
            deferred.reject(response);
        });
        return deferred.promise;
    }

    // gets all datamodels from the backend
    function listDatamodels() {
        var deferred = $q.defer();
        var url = '/api/data-model';
        $http.get(url).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    /**
     *  deletes a datamodel object in the backend
     *  @param dmObjectId - ID of the datamodel object to delete
     */
    function deleteDatamodelObject(dmObjectId) {
        var deferred = $q.defer();
        var url = '/api/data-model/objects/' + dmObjectId;
        $http.delete(url).then(function success(response) {
            deferred.resolve(response);
        }, function fail(response) {
            deferred.reject(response);
        });
        return deferred.promise;
    }

    /**
     *  deletes a datamodel in the backend
     *  @param datmodelID - ID of the datamodel to delete
     */
    function deleteDatamodel(dmoId) {
        var deferred = $q.defer();
        var url = '/api/data-model/' + dmoId;
        $http.delete(url).then(function success(response) {
            deferred.resolve(response);
        }, function fail(response) {
            deferred.reject(response);
        });
        return deferred.promise;
    }

    /**
     *  Get a datamodel object
     *  @param dmObjectId - ID of the datamodel object
     */
    function getDatamodelObject(dmObjectId) {
        var deferred = $q.defer();
        var url = '/api/data-model/objects/' + dmObjectId;
        $http.get(url).then(function success(response) {
            deferred.resolve(response);
        }, function fail(response) {
            deferred.reject(response);
        });
        return deferred.promise;
    }
}
