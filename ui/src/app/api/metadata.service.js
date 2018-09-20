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
export default angular.module('tempus.api.metadata', [])
    .factory('metadataService', MetadataService)
    .name;


/*@ngInject*/

function MetadataService($http, $q, $filter, utils) {

    var service = {
        upload: upload,
        getAllMetadatas: getAllMetadatas,
        getMetadata: getMetadata,


        getAllTenantMetadata :getAllTenantMetadata,
        saveMetadataQuery: saveMetadataQuery,
        getAllMetadataQuery:getAllMetadataQuery,
        deleteMetadataQuery: deleteMetadataQuery,
        testMetadataConfig: testMetadataConfig
        getAllTenantMetadata :getAllTenantMetadata
        deleteMetadata: deleteMetadata
        saveMetadata: saveMetadata
    }

    return service;

    function upload(file) {
        var deferred = $q.defer();
        var url = '/api/metadata/upload';
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

    function getAllMetadatas(pageLink) {
        var deferred = $q.defer();
        var url = '/api/metadata';
        $http.get(url, null).then(
            function success(response) {
                var allMetadatas = response.data;
                allMetadatas = $filter('orderBy')(allMetadatas, ['+name', '-createdTime']);
                utils.filterSearchTextEntities(allMetadatas, 'name', pageLink, deferred);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }


    function getAllTenantMetadata(pageLink) {
        $log.log(pageLink);
        var deferred = $q.defer();
        var url = '/api/metadata/tenant/configs?limit=' + pageLink.limit;
        $http.get(url, null).then(
            function success(response) {
                $log.log(response);
                var allMetadatas = response.data.data;
                allMetadatas = $filter('orderBy')(allMetadatas, ['+name', '-createdTime']);
                utils.filterSearchTextEntities(allMetadatas, 'name', pageLink, deferred);
            },
            function fail(error) {
                $log.log(error);
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function getMetadata(metadataId) {
        var deferred = $q.defer();
        var url = '/api/metadata/' + metadataId;
        $http.get(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    function deleteMetadata(metadataId) {
        var deferred = $q.defer();
        var url = '/api/metadata/config/' + metadataId;
.
        $http.delete(url).then(function success() {
            deferred.resolve();
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

    /**
     * Save Metadata configuration details.
     *
     * @param {*} metadataConfig
     */
    function saveMetadata(metadataConfig) {
        var deferred = $q.defer();
        var url = '/api/metadata/config';
        $http.post(url, metadataConfig).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    /**
     * Save Metadata configuration details.
     *

     * @param {*} metadataConfig
     */
    function saveMetadata(metadataConfig) {
        var deferred = $q.defer();
        var url = '/api/metadata/config';
        $http.post(url, metadataConfig).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    /**
     * Save Metadata configuration query.
     *
     * @param {*} metadataQuery
     */

    function saveMetadataQuery(metadataQuery) {
            var deferred = $q.defer();
            var url = '/api//metadata/query';
            $http.post(url, metadataQuery).then(function success(response) {
                deferred.resolve(response.data);
            }, function fail() {
                deferred.reject();
            });
            return deferred.promise;
    }

   /**
    * Get all Metadata Queries.
    *
    * @param {*} metadataQuery
    */
    function getAllMetadataQuery(metadataConfigId,pageLink) {
            var deferred = $q.defer();
            var url = '/api/metadata/config/'+metadataConfigId+'/query?limit=' + pageLink;
            $http.get(url, null).then(
                function success(response) {
                    var allMetadatas = response.data.data;
                    $log.log(allMetadatas);
                    allMetadatas = $filter('orderBy')(allMetadatas, ['+queryStmt', '-createdTime']);
                    utils.filterSearchTextEntities(allMetadatas, 'queryStmt', pageLink, deferred);
                },
                function fail() {
                    deferred.reject();
                }
            );
            return deferred.promise;
    }

    /**
    * Delete Metadata query
    *
    * @param {*} metadataQueryId
    */
    function deleteMetadataQuery(metadataQueryId) {
            var deferred = $q.defer();
            var url = '/api/metadata/query/' + metadataQueryId;
            $http.delete(url).then(function success() {
                deferred.resolve();
            }, function fail() {
                deferred.reject();
            });
            return deferred.promise;
    }

    /**
    * Test Metadata config.
    *
    * @param {*} metadataQuery
    */
    function testMetadataConfig(metadataConfigId) {
            var deferred = $q.defer();
            var url = '/api/metadata/config/'+metadataConfigId+'/test';
            $http.get(url, null).then(
                function success(response) {
                    deferred.resolve(response.data);
                },
                function fail() {
                    deferred.reject();
                }
            );
            return deferred.promise;
    }

}