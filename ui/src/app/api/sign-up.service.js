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

export default angular.module('tempus.api.signup', [])
    .factory('signUpService', SignUpService)
    .name;

/*@ngInject*/
function SignUpService($http, $q, $window, userService, attributeService, $log) {

    var service = {
        saveTrialUser: saveTrialUser
    }

    return service;

    function saveTrialUser(signupRequest) {
        var deferred = $q.defer();

        var url = '/api/noauth/user';

        $http.post(url, signupRequest).then(function success(response) {
                $log.log(response)
                deferred.resolve(response.data);

        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }


}
