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
 export default angular.module('tempus.api.computationJob', [])
     .factory('computationJobService', ComputationJobService)
     .name;


/*@ngInject*/
function ComputationJobService($http, $q, $filter, utils) {
    var service = {
        getAllComputationJobs: getAllComputationJobs,
        deleteComputationJob: deleteComputationJob,
        saveComputationJob: saveComputationJob,
        activateComputationJob: activateComputationJob,
        suspendComputationJob: suspendComputationJob
    }

    return service;

    function getAllComputationJobs(pageLink, computationId) {
        var deferred = $q.defer();

        var url = '/api/computations/'+ computationId +'/jobs';
        $http.get(url, null).then(
            function success(response) {
                var allComputationJobs = response.data;
                allComputationJobs = $filter('orderBy')(allComputationJobs, ['+name', '-createdTime']);
                utils.filterSearchTextEntities(allComputationJobs, 'name', pageLink, deferred);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function saveComputationJob(computationJob, computationId) {
        var deferred = $q.defer();
        computationId;
        var url = '/api/computations/'+computationId+'/jobs';
        $http.post(url, computationJob).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function deleteComputationJob(computationJobId) {
        var deferred = $q.defer();
        var url = '/api/computations/jobs/' + computationJobId;
        $http.delete(url).then(function success() {
            deferred.resolve();
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function activateComputationJob(computationId, computationJobId) {
        var deferred = $q.defer();
        var url = '/api/computations/' + computationId + '/jobs/' + computationJobId + '/activate';
        $http.post(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function suspendComputationJob(computationId, computationJobId) {
        var deferred = $q.defer();
        var url = '/api/computations/' + computationId + '/jobs/' + computationJobId + '/suspend';
        $http.post(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

}