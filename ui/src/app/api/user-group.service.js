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
export default angular.module('tempus.api.userGroup', [])
    .factory('userGroupService', userGroupService)
    .name;

/*@ngInject*/
function userGroupService($q, $http) {


    var service = {
      saveUserGroup: saveUserGroup,
      getGroups:getGroups,
      deleteUserGroup:deleteUserGroup,
      assignUserToGroup:assignUserToGroup,
      assignedUsers:assignedUsers,
      unassignUserToGroup:unassignUserToGroup,
      assignedGroups:assignedGroups,
      assignGroupToUser:assignGroupToUser,
      unassignGroupToUser:unassignGroupToUser

    }

    return service;

    function saveUserGroup(item) {

        var deferred = $q.defer();
        var url = '/api/customer/group';
        $http.post(url, item).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function deleteUserGroup(userGroupId) {
        var deferred = $q.defer();
        var url = '/api/customer/group/' + userGroupId;
        $http.delete(url).then(function success() {
            deferred.resolve();
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function assignUserToGroup(groupId, userIds) {
        var deferred = $q.defer();
        var url = '/api/customer/group/' + groupId + '/users';
        $http.post(url, userIds).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function assignGroupToUser(userId, groupIds) {

        var deferred = $q.defer();
        var url = '/api/user/' + userId + '/groups';
        $http.post(url, groupIds).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;

    }

    function unassignUserToGroup(groupId, userIds) {
        var deferred = $q.defer();
        var url = '/api/customer/group/' + groupId + '/users';
        $http.put(url,userIds).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }


    function unassignGroupToUser(userId, groupIds) {
        var deferred = $q.defer();
        var url = '/api/user/' + userId + '/groups';
        $http.put(url,groupIds).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function assignedUsers(groupId, pageLink) {
        var deferred = $q.defer();
        var url = '/api/customer/group/' + groupId + '/users?limit=' + pageLink.limit;

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


    function assignedGroups(userId, pageLink) {
        var deferred = $q.defer();
        var url = '/api/user/' + userId + '/groups?limit=' + pageLink.limit;

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



    function getGroups(customerId, pageLink) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/groups?limit=' + pageLink.limit;
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


}
