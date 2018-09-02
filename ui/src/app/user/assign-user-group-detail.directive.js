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
//mport './audit-log.scss';
import './assign-detail.scss';
import './user-fieldset.scss';
/* eslint-disable import/no-unresolved, import/default */
import assignDetailTemplate from './assign-user-group-detail.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function AssignUseGrouprDetailDirective($compile, $templateCache, $rootScope, $translate, types, userGroupService) {

    var linker = function(scope, element) {

        var template = $templateCache.get(assignDetailTemplate);

        element.html(template);

        scope.types = types;

        scope.assignedUsers = '';
        scope.assignedGroups = '';

        function prepareData(data) {
            if (scope.entType == "group") {

                scope.assignedUsers = data.data;
            } else if (scope.entType == "user") {

                scope.assignedGroups = data.data;
            }


        }


        scope.$watch("groupId", function(newVal, prevVal) {
            if (newVal && !angular.equals(newVal, prevVal)) {
                scope.reload();
            }
        });

        scope.$watch("entType", function(newVal, prevVal) {
            if (newVal && !angular.equals(newVal, prevVal)) {
                scope.reload();
            }
        });

        scope.$watch("userId", function(newVal, prevVal) {
            if (newVal && !angular.equals(newVal, prevVal)) {
                scope.reload();
            }
        });

        function getAssignedUserPromise(pageLink) {
            if (scope.groupId) {
                var promise = userGroupService.assignedUsers(scope.groupId, pageLink);
                if (promise) {

                    promise.then(
                        function success(userDetails) {

                            prepareData(userDetails);
                        },
                        function fail() {});

                }
            } else {
                return null;
            }

        }


        function getGroupsPromise(pageLink) {

            if (scope.userId) {
                var promise = userGroupService.assignedGroups(scope.userId, pageLink);
                if (promise) {

                    promise.then(
                        function success(groupDetails) {

                            prepareData(groupDetails);
                        },
                        function fail() {});

                }
            } else {
                return null;
            }


        }

        scope.reload = function() {
            scope.query = {
                order: 'name',
                limit: 100,
                page: 1,
                search: null
            };
            if (scope.entType == "group") {

                getAssignedUserPromise(scope.query);
            } else if (scope.entType == "user") {

                getGroupsPromise(scope.query);
            }

        }


        scope.loading = function() {
            return $rootScope.loading;
        }


        scope.reload();


        $compile(element.contents())(scope);
    }

    return {
        restrict: "E",
        link: linker,
        scope: {
            groupId: '=?',
            pageMode: '@?',
            userId: '=?',
            entType: '@?'
        }
    };
}