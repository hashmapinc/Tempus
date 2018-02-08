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
    var allActionComputations = undefined;
    var systemComputations = undefined;
    var tenantComputations = undefined;

    $rootScope.computationServiceStateChangeStartHandle = $rootScope.$on('$stateChangeStart', function () {
        invalidateComputationsCache();
    });
    var service = {
        upload: upload,
        getSystemComputations: getSystemComputations,
        getTenantComputations: getTenantComputations,
        getAllComputations: getAllComputations,
        getAllActionComputations: getAllActionComputations,
        getComputationByToken: getComputationByToken,
        getComputation: getComputation,
        deleteComputation: deleteComputation,
        saveComputation: saveComputation,
        activateComputation: activateComputation,
        suspendComputation: suspendComputation
    }

    return service;

    function invalidateComputationsCache() {
        allComputations = undefined;
        allActionComputations = undefined;
        systemComputations = undefined;
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
                //componentDescriptorService.getComponentDescriptorsByType(types.componentType.computation).then(
                    //function success(computationComponents) {
                        allComputations = response.data;
                        //allActionComputations = [];
                        systemComputations = [];
                        tenantComputations = [];
                        allComputations = $filter('orderBy')(allComputations, ['+name', '-createdTime']);
                        /*var computationHasActionsByClazz = {};
                        for (var index in computationComponents) {
                            computationHasActionsByClazz[computationComponents[index].clazz] =
                                (computationComponents[index].actions != null && computationComponents[index].actions.length > 0);
                        }*/
                        for (var i = 0; i < allComputations.length; i++) {
                            var computation = allComputations[i];
                
                            /*if (computation.tenantId.id === types.id.nullUid) {
                                systemComputations.push(computation);
                            } else {*/
                                tenantComputations.push(computation);
                            //}
                        }

                        $log.error("HMDC tenantComputations : " + angular.toJson(tenantComputations));

                        deferred.resolve();
                    //},
                    /*function fail() {
                        deferred.reject();
                    }*/
                //);
            }, function fail() {
                deferred.reject();
            });
        } else {
            deferred.resolve();
        }
        return deferred.promise;
    }

    function getSystemComputations(pageLink) {
        var deferred = $q.defer();
        loadComputationsCache().then(
            function success() {
                utils.filterSearchTextEntities(systemComputations, 'name', pageLink, deferred);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function getTenantComputations(pageLink) {
        var deferred = $q.defer();
        loadComputationsCache().then(
            function success() {
                utils.filterSearchTextEntities(tenantComputations, 'name', pageLink, deferred);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function getAllActionComputations(pageLink) {
        var deferred = $q.defer();
        loadComputationsCache().then(
            function success() {
                utils.filterSearchTextEntities(allActionComputations, 'name', pageLink, deferred);
            },
            function fail() {
                deferred.reject();
            }
        );
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

    function getComputationByToken(computationToken) {
        var deferred = $q.defer();
        var url = '/api/computation/token/' + computationToken;
        $http.get(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
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

    function saveComputation(computation) {
        var deferred = $q.defer();
        var url = '/api/computation';
        $http.post(url, computation).then(function success(response) {
            invalidateComputationsCache();
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function deleteComputation(computationId) {
        var deferred = $q.defer();
        var url = '/api/computation/' + computationId;
        $http.delete(url).then(function success() {
            invalidateComputationsCache();
            deferred.resolve();
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function activateComputation(computationId) {
        var deferred = $q.defer();
        var url = '/api/computation/' + computationId + '/activate';
        $http.post(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function suspendComputation(computationId) {
        var deferred = $q.defer();
        var url = '/api/computation/' + computationId + '/suspend';
        $http.post(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

}