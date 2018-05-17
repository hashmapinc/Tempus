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
export default angular.module('tempus.api.tempusboard', [])
    .factory('tempusboardService', TempusboardService)
    .name;

/*@ngInject*/
function TempusboardService($http, $q) {

    var service = {
        getTempusboardSettings: getTempusboardSettings,
        saveTempusboardSettings: saveTempusboardSettings
    }

    return service;

    function getTempusboardSettings(key) {
        var deferred = $q.defer();
        var url = '/api/settings/' + key;
        $http.get(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function saveTempusboardSettings(settings) {
        var deferred = $q.defer();
        var url = '/api/settings';
        $http.post(url, settings).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }
}
