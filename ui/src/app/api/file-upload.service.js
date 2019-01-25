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
import tempusTypes from '../common/types.constant';

export default angular.module('tempus.api.fileUpload', [tempusTypes])
    .factory('fileUploadService', FileUploadService)
    .name;

/*@ngInject*/
function FileUploadService($http, $q, $log, types) {

    var vm = this;

    vm.types = types;

    var service = {

        getAllFile: getAllFile,
        uploadFile: uploadFile,
        deleteFile: deleteFile
    }

    return service;

//    function getAllFiles(pageLink, config, type, pageNum) {
//        var deferred = $q.defer();
//        if(angular.isDefined(type) && type == 'Gateway') {
//          pageNum = 0;
//        }
//        var url = '/api/tenant/file?limit=' + pageLink.limit + '&pageNum=' + pageNum;
//        if (angular.isDefined(pageLink.textSearch)) {
//            url += '&textSearch=' + pageLink.textSearch;
//        }
//        if (angular.isDefined(pageLink.idOffset)) {
//            url += '&idOffset=' + pageLink.idOffset;
//        }
//        if (angular.isDefined(pageLink.textOffset)) {
//            url += '&textOffset=' + pageLink.textOffset;
//        }
//        if (angular.isDefined(type) && type.length) {
//            url += '&type=' + type;
//        }
//        $http.get(url, config).then(function success(response) {
//
//                        deferred.resolve(response.data);
//                    },
//                    function fail() {
//                        deferred.reject();
//                    }
//                );
//        return deferred.promise;
//    }

//    function saveFile(file) {
//        var deferred = $q.defer();
//        var url = '/api/file';
//        $http.post(url, file).then(function success(response) {
//            deferred.resolve(response.data);
//        }, function fail() {
//            deferred.reject();
//        });
//        return deferred.promise;
//    }

    function uploadFile(file) {
        var deferred = $q.defer();
        var url = '/api/file';
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

    function getAllFile() {
        var deferred = $q.defer();
        var url = '/api/file';
        $http.get(url).then(function success(response) {
            deferred.resolve(response.data);
            $log.log(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }
    function deleteFile(fileName) {
        var deferred = $q.defer();
        var url = '/api/file' + fileName;
        $http.delete(url).then(function success() {
            deferred.resolve();
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

}
