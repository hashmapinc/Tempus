/*
 * Copyright © 2016-2017 The Thingsboard Authors
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

 export default angular.module('thingsboard.api.computation', [])
     .factory('computationService', ComputationService)
     .name;


/*@ngInject*/
function ComputationService($http, $q, $rootScope, $filter, componentDescriptorService, types, utils, $log) {

    var allComputations = undefined;
    var tenantComputations = undefined;

    $rootScope.computationServiceStateChangeStartHandle = $rootScope.$on('$stateChangeStart', function () {
        invalidateComputationsCache();
    });
    var service = {
        upload: upload,
        getAllComputations: getAllComputations,
        getComputation: getComputation,
        deleteComputation: deleteComputation
    }

    return service;

    function invalidateComputationsCache() {
        allComputations = undefined;
        tenantComputations = undefined;
    }

    function upload(file) {
        var deferred = $q.defer();
        var url = '/api/computations/upload';
        var fd = new FormData();
        fd.append("file", file);
        $http.post(url, fd, {
                    transformRequest: angular.identity,
                    headers: {'Content-Type': undefined}
                }).then(function success(response) {
                      deferred.resolve(response.data);
                  }, function fail(response) {
                      deferred.reject(response.data);
                  });
        return deferred.promise;
    }

    function loadComputationsCache() {
        var deferred = $q.defer();
        if (!allComputations) {
            var url = '/api/computations';
            $http.get(url, null).then(function success(response) {
                        allComputations = response.data;
                        tenantComputations = [];
                        allComputations = $filter('orderBy')(allComputations, ['+name', '-createdTime']);
                        for (var i = 0; i < allComputations.length; i++) {
                            var computation = allComputations[i];
                            tenantComputations.push(computation);
                        }

                        $log.log("tenantComputations : " + angular.toJson(tenantComputations));

                        deferred.resolve();
            }, function fail() {
                deferred.reject();
            });
        } else {
            deferred.resolve();
        }
        return deferred.promise;
    }

    function getAllComputations(pageLink) {
        var deferred = $q.defer();
        loadComputationsCache().then(
            function success() {
                utils.filterSearchTextEntities(allComputations, 'name', pageLink, deferred);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function getComputation(computationId) {
        var deferred = $q.defer();
        var url = '/api/computations/' + computationId;
        $http.get(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function deleteComputation(computationId) {
        var deferred = $q.defer();
        var url = '/api/computations/' + computationId;
        $http.delete(url).then(function success() {
            invalidateComputationsCache();
            deferred.resolve();
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

}