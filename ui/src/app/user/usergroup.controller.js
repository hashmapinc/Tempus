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
/* eslint-disable import/no-unresolved, import/default */
//import addUserTemplate from './add-user.tpl.html';
//import userCard from './user-card.tpl.html';
//import activationLinkDialogTemplate from './activation-link.dialog.tpl.html';
/* eslint-enable import/no-unresolved, import/default */
/*@ngInject*/
import addDataModel from './add-group.tpl.html';
import userCard from './user-card.tpl.html';
import assignUserToGroups from './assign-to-group.tpl.html';
import unassignUserToGroups from './unassign-to-group.tpl.html';

/*@ngInject*/

export default function UserGroupController($scope, $state, types, $stateParams, $mdDialog, $document, usergroupService, $q, userService, $translate) {

    var tenantId = $stateParams.tenantId;
    var customerId = $stateParams.customerId;
    var usersType = $state.$current.data.usersType;
    var groupActionsList = [];


    groupActionsList.push({
        onAction: function($event, item) {
            assignToGroup($event, [item.id.id]);
        },
        name: function() {
            return $translate.instant('action.assign')
        },
        details: function() {
            return $translate.instant('user.assignToGroups')
        },
        icon: "assignment_ind"

    });

    groupActionsList.push({
        onAction: function($event, item) {
            unassignToGroup($event, [item.id.id]);
        },
        name: function() {
            return $translate.instant('action.assign')
        },
        details: function() {
            return $translate.instant('user.unassignToGroups')
        },
        icon: "assignment_return"

    });

    groupActionsList.push({
        onAction: function($event, item) {
            vm.grid.deleteItem($event, item);
        },
        name: function() {
            return $translate.instant('action.delete')
        },
        details: function() {
            return $translate.instant('customer.usergroupdelete')
        },
        icon: "delete"
    });

    var vm = this;

    vm.addUserGroup = addUserGroup;
    vm.customerId = customerId;
    vm.AddGroupModelController = "AddGroupModelController";
    vm.saveGroup = saveGroup;
    vm.assignToGroup = assignToGroup;
    vm.unassignToGroup = unassignToGroup;
    vm.types = types;

    vm.userGroupGridConfig = {
        deleteItemTitleFunc: deleteUserGroupTitle,
        deleteItemContentFunc: deleteUserGroupText,
        deleteItemsTitleFunc: deleteGroupsTitle,
        deleteItemsActionTitleFunc: deleteGroupsActionTitle,
        deleteItemsContentFunc: deleteGroupsText,

        deleteItemFunc: deleteUserGroup,

        getItemTitleFunc: getUserGroupTitle,
        itemCardTemplateUrl: userCard,
        addItemController: 'AddGroupModelController',
        addItemTemplateUrl: addDataModel,
        parentCtl: vm,

        actionsList: groupActionsList,

        onGridInited: gridInited,
        noItemsText: function() {
            return $translate.instant('customer.no-groups-text')
        },
        itemDetailsText: function() {
            return $translate.instant('customer.usergroup')
        },
        addItemText: function() {
            return $translate.instant('customer.add-group-text')
        },
        entType: "usergroup"
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.userGroupGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.userGroupGridConfig.topIndex = $stateParams.topIndex;
    }

    initController();

    function initController() {
        var fetchUsersGroupFunction = null;
        var refreshUsersGroupParamsFunction = null;
        var saveUserGroupFunction = null;

        if (usersType === 'tenant') {
            fetchUsersGroupFunction = function(pageLink) {
                return userService.getTenantAdmins(tenantId, pageLink);
            };


        } else if (usersType === 'customer') {
            fetchUsersGroupFunction = function(pageLink) {
                return usergroupService.getGroups(vm.customerId, pageLink);
            };

            saveUserGroupFunction = function(usergroup) {

                usergroup.customerId = {
                    entityType: "CUSTOMER",
                    id: customerId
                };

                return usergroupService.saveUserGroup(usergroup);
            };

            refreshUsersGroupParamsFunction = function() {
                return {
                    "customerId": customerId,
                    "topIndex": vm.topIndex
                };
            };
        }

        vm.userGroupGridConfig.refreshParamsFunc = refreshUsersGroupParamsFunction;
        vm.userGroupGridConfig.fetchItemsFunc = fetchUsersGroupFunction;
        vm.userGroupGridConfig.saveItemFunc = saveUserGroupFunction;

    }

    function deleteUserGroup(userGroupId) {
        return usergroupService.deleteUserGroup(userGroupId);
    }

    function deleteUserGroupTitle(usergroup) {
        return $translate.instant('user.delete-usergroup-title', {
            groupName: usergroup.name
        });
    }

    function deleteUserGroupText() {
        return $translate.instant('user.delete-usergroup-text');
    }

    function deleteGroupsTitle(selectedCount) {
        return $translate.instant('user.delete-groups-title', {
            count: selectedCount
        }, 'messageformat');
    }

    function deleteGroupsActionTitle(selectedCount) {
        return $translate.instant('user.delete-groups-action-title', {
            count: selectedCount
        }, 'messageformat');
    }

    function deleteGroupsText() {
        return $translate.instant('user.delete-groups-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function getUserGroupTitle(userGroup) {
        return userGroup ? userGroup.name : '';
    }


    function addUserGroup($event, id) {

        $mdDialog.show({
            controller: vm.AddGroupModelController,
            controllerAs: 'vm',
            templateUrl: addDataModel,
            parent: angular.element($document[0].body),
            locals: {
                saveItemFunction: vm.saveGroup,
                customerId: id
            },
            fullscreen: true,
            targetEvent: $event
        }).then(function() {}, function() {});
    }

    function saveGroup(item) {

        var deferred = $q.defer();
        usergroupService.saveUserGroup(item).then(function success(response) {
            initController();
            deferred.resolve(response);
        }, function fail(response) {
            deferred.reject(response);
        });
        return deferred.promise;
    }

    function assignToGroup($event, id) {

        if ($event) {
            $event.stopPropagation();
        }

        var pageSize = 10;

        userService.getCustomerUsers(vm.customerId, {
            limit: pageSize,
            textSearch: ''
        }).then(
            function success(_users) {
                var users = {
                    pageSize: pageSize,
                    data: _users.data,
                    nextPageLink: _users.nextPageLink,
                    selection: null,
                    hasNext: _users.hasNext,
                    pending: false
                };
                if (users.hasNext) {
                    users.nextPageLink.limit = pageSize;
                }

                $mdDialog.show({
                    controller: 'AddUsersToGroupController',
                    controllerAs: 'vm',
                    templateUrl: assignUserToGroups,
                    locals: {
                        groupId: id,
                        users: users,
                        customerId: vm.customerId
                    },
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function() {
                    vm.grid.refreshList();
                }, function() {});
            },
            function fail() {});
    }


    function unassignToGroup($event, id) {

        if ($event) {
            $event.stopPropagation();
        }

        var pageSize = 10;

        usergroupService.assignedUsers(id, {
            limit: pageSize,
            textSearch: ''
        }).then(
            function success(_users) {
                var users = {
                    pageSize: pageSize,
                    data: _users.data,
                    nextPageLink: _users.nextPageLink,
                    selection: null,
                    hasNext: _users.hasNext,
                    pending: false
                };
                if (users.hasNext) {
                    users.nextPageLink.limit = pageSize;
                }

                $mdDialog.show({
                    controller: 'DeleteUsersToGroupController',
                    controllerAs: 'vm',
                    templateUrl: unassignUserToGroups,
                    locals: {
                        groupId: id,
                        users: users,
                        customerId: vm.customerId
                    },
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function() {
                    vm.grid.refreshList();
                }, function() {});
            },
            function fail() {});
    }


}