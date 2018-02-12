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

 export default angular.module('thingsboard.api.computationJob', [])
     .factory('computationJobService', ComputationJobService)
     .name;


/*@ngInject*/
function ComputationJobService($http, $q, $rootScope, $filter, componentDescriptorService, types, utils, $log) {

    var allComputationJobs = undefined;
    var allActionComputationJobs = undefined;
    var systemComputationJobs = undefined;
    var tenantComputationJobs = undefined;

    $rootScope.computationJobServiceStateChangeStartHandle = $rootScope.$on('$stateChangeStart', function () {
        invalidateComputationJobsCache();
    });
    var service = {
        getSystemComputationJobs: getSystemComputationJobs,
        getTenantComputationJobs: getTenantComputationJobs,
        getAllComputationJobs: getAllComputationJobs,
        getAllActionComputationJobs: getAllActionComputationJobs,
        getComputationJobByToken: getComputationJobByToken,
        getComputationJob: getComputationJob,
        deleteComputationJob: deleteComputationJob,
        saveComputationJob: saveComputationJob,
        activateComputationJob: activateComputationJob,
        suspendComputationJob: suspendComputationJob
    }

    return service;

    function invalidateComputationJobsCache() {
        allComputationJobs = undefined;
        allActionComputationJobs = undefined;
        systemComputationJobs = undefined;
        tenantComputationJobs = undefined;
    }

    function loadComputationJobsCache(computationId) {
        var deferred = $q.defer();
        computationId;
        //if (!allComputationJobs) {
            var url = '/api/computations/'+ computationId +'/jobs';
            $log.log("URL is ", url);
            $http.get(url, null).then(function success(response) {
                $log.log("success response is ", response.data);
                //componentDescriptorService.getComponentDescriptorsByType(types.componentType.computationJob).then(
                    //function success(computationJobComponents) {
                        allComputationJobs = response.data;
                        //allActionComputationJobs = [];
                        systemComputationJobs = [];
                        tenantComputationJobs = [];
                        allComputationJobs = $filter('orderBy')(allComputationJobs, ['+name', '-createdTime']);
                        /*var computationJobHasActionsByClazz = {};
                        for (var index in computationJobComponents) {
                            computationJobHasActionsByClazz[computationJobComponents[index].clazz] =
                                (computationJobComponents[index].actions != null && computationJobComponents[index].actions.length > 0);
                        }*/
                        for (var i = 0; i < allComputationJobs.length; i++) {
                            var computationJob = allComputationJobs[i];
                
                            /*if (computationJob.tenantId.id === types.id.nullUid) {
                                systemComputationJobs.push(computationJob);
                            } else {*/
                                tenantComputationJobs.push(computationJob);
                            //}
                        }

                        $log.error("HMDC tenantComputationJobs : " + angular.toJson(tenantComputationJobs));

                        deferred.resolve();
                    //},
                    /*function fail() {
                        deferred.reject();
                    }*/
                //);
            }, function fail() {
                $log.log("failed to get response");
                deferred.reject();
            });
        /*} else {
            deferred.resolve();
        }*/
        return deferred.promise;
    }

    function getSystemComputationJobs(pageLink) {
        var deferred = $q.defer();
        loadComputationJobsCache().then(
            function success() {
                utils.filterSearchTextEntities(systemComputationJobs, 'name', pageLink, deferred);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function getTenantComputationJobs(pageLink) {
        var deferred = $q.defer();
        loadComputationJobsCache().then(
            function success() {
                utils.filterSearchTextEntities(tenantComputationJobs, 'name', pageLink, deferred);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function getAllActionComputationJobs(pageLink) {
        var deferred = $q.defer();
        loadComputationJobsCache().then(
            function success() {
                utils.filterSearchTextEntities(allActionComputationJobs, 'name', pageLink, deferred);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function getAllComputationJobs(pageLink, computationId) {
        var deferred = $q.defer();
        loadComputationJobsCache(computationId).then(
            function success() {
                utils.filterSearchTextEntities(allComputationJobs, 'name', pageLink, deferred);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function getComputationJobByToken(computationJobToken) {
        var deferred = $q.defer();
        var url = '/api/computationJob/token/' + computationJobToken;
        $http.get(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getComputationJob(computationJobId) {
        var deferred = $q.defer();
        var url = '/api/computationJob/' + computationJobId;
        $http.get(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function saveComputationJob(computationJob, computationId) {
        $log.log("computationJob to be posted " + computationJob.name);
        var deferred = $q.defer();
        computationId;
        var url = '/api/computations/'+computationId+'/jobs';
        $http.post(url, computationJob).then(function success(response) {
            invalidateComputationJobsCache();
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function deleteComputationJob(computationJobId) {
        var deferred = $q.defer();
        var url = '/api/computationJob/' + computationJobId;
        $http.delete(url).then(function success() {
            invalidateComputationJobsCache();
            deferred.resolve();
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function activateComputationJob(computationJobId) {
        var deferred = $q.defer();
        var url = '/api/computationJob/' + computationJobId + '/activate';
        $http.post(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function suspendComputationJob(computationJobId) {
        var deferred = $q.defer();
        var url = '/api/computationJob/' + computationJobId + '/suspend';
        $http.post(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

}