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
export default angular.module('tempus.api.preferences', [])
    .factory('preferencesService', PreferencesService).name;

/*@ngInject*/
function PreferencesService($http, $q, $rootScope, $filter, types, utils, $log) {


    var service = {
        getUnitSystem: getUnitSystem,
        saveUnitSystem: saveUnitSystem
    }

    return service;

    function getUnitSystem(tenantId) {
        $log.log("getUnitSystem");
        var deferred = $q.defer();
        var url = '/api/unit-system/tenant/' + tenantId ;
        $http.get(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function saveUnitSystem(tenantId, unitSystem) {
        var deferred = $q.defer();
        var url = '/api/unit-system/tenant/' + tenantId;
        $http.post(url, unitSystem).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }


}
