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
export default angular.module('tempus.api.gatewayConfiguration', [])
    .factory('gatewayConfigurationService', GatewayConfigurationService)
    .name;

/*@ngInject*/
function GatewayConfigurationService($q, $http) {


    var service = {
      saveGatewayConfiguration: saveGatewayConfiguration,
      getGatewayConfiguration:getGatewayConfiguration
    }

    return service;

    function saveGatewayConfiguration(item) {

        var deferred = $q.defer();
        var url = '/api/configuration/tempusGateway';
        $http.post(url, item).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getGatewayConfiguration() {

        var deferred = $q.defer();
        var url = '/api/configuration/tempusGateway';
        $http.get(url).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }



}
