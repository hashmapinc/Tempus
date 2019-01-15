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
export default angular.module('tempus.api.template', [])
    .factory('templateService', TemplateService)
    .name;

/*@ngInject*/
function TemplateService($q, $http) {


    var service = {
      saveTemplate: saveTemplate,
      getTemplates: getTemplates,
      deleteTemplate: deleteTemplate,
      getAllTemplates: getAllTemplates
    }

    return service;

    function saveTemplate(item) {

        var deferred = $q.defer();
        var url = '/api/template';
        $http.post(url, item).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }


    function getTemplates(pageLink, pageNum) {

        var deferred = $q.defer();
        var url = '/api/templates?limit=' + pageLink.limit + '&pageNum=' + pageNum;
        if (angular.isDefined(pageLink.textSearch)) {
            url += '&textSearch=' + pageLink.textSearch;
        }
        if (angular.isDefined(pageLink.idOffset)) {
            url += '&idOffset=' + pageLink.idOffset;
        }
        if (angular.isDefined(pageLink.textOffset)) {
            url += '&textOffset=' + pageLink.textOffset;
        }
        $http.get(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function deleteTemplate(templateId){

        var deferred = $q.defer();
        var url = '/api/template/'+templateId;
        $http.delete(url).then(function success() {
            deferred.resolve();
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;


    }

    function getAllTemplates(){

        var deferred = $q.defer();
        var url = '/api/templates';
        $http.get(url, null).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }



}
