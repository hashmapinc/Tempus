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
import addUserTemplate from './add-user.tpl.html';
import userCard from './user-card.tpl.html';
import activationLinkDialogTemplate from './activation-link.dialog.tpl.html';
import assignGroupsToUsers from './assign-to-user.tpl.html';
import unassignGroupsToUsers from './unassign-to-user.tpl.html';
//import unassignUserToGroups from './unassign-to-group.tpl.html';


/* eslint-enable import/no-unresolved, import/default */


/*@ngInject*/
export default function UserController(usergroupService, $log, userService, toast, $scope, $mdDialog, $document, $controller, $state, $stateParams, $translate, types) {

    var tenantId = $stateParams.tenantId;
    var customerId = $stateParams.customerId;
    var usersType = $state.$current.data.usersType;
    var userActionsList = [];

    userActionsList.push({
        onAction: function($event, item) {
            assignGroupToUsers($event, [item.id.id]);
        },
        name: function() {
            return $translate.instant('action.assign')
        },
        details: function() {
            return $translate.instant('user.assignToUsers')
        },
        icon: "assignment_ind"

    });

    userActionsList.push(

        {
            onAction: function($event, item) {
                unassignToUsers($event, [item.id.id]);
            },
            name: function() {
                return $translate.instant('action.unassign')
            },
            details: function() {
                return $translate.instant('user.unassignToUsers')
            },
            icon: "assignment_return"

        }
    );


    userActionsList.push({
        onAction: function($event, item) {
            vm.grid.deleteItem($event, item);
        },
        name: function() {
            return $translate.instant('action.delete')
        },
        details: function() {
            return $translate.instant('user.delete')
        },
        icon: "delete"
    });

    var vm = this;

    vm.types = types;
    vm.assignGroupToUsers = assignGroupToUsers;
    vm.unassignToUsers = unassignToUsers;
    vm.customerId = customerId;

    vm.userGridConfig = {
        deleteItemTitleFunc: deleteUserTitle,
        deleteItemContentFunc: deleteUserText,
        deleteItemsTitleFunc: deleteUsersTitle,
        deleteItemsActionTitleFunc: deleteUsersActionTitle,
        deleteItemsContentFunc: deleteUsersText,

        deleteItemFunc: deleteUser,

        getItemTitleFunc: getUserTitle,
        itemCardTemplateUrl: userCard,

        actionsList: userActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addUserTemplate,
        addItemController: 'AddUserController',

        addItemText: function() {
            return $translate.instant('user.add-user-text')
        },
        noItemsText: function() {
            return $translate.instant('user.no-users-text')
        },
        itemDetailsText: function() {
            return $translate.instant('user.user-details')
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.userGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.userGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.displayActivationLink = displayActivationLink;
    vm.resendActivation = resendActivation;

    initController();

    function initController() {
        var fetchUsersFunction = null;
        var saveUserFunction = null;
        var refreshUsersParamsFunction = null;

        if (usersType === 'tenant') {
            fetchUsersFunction = function(pageLink) {
                return userService.getTenantAdmins(tenantId, pageLink);
            };
            saveUserFunction = function(user) {
                user.authority = "TENANT_ADMIN";
                user.tenantId = {
                    entityType: types.entityType.tenant,
                    id: tenantId
                };
                return userService.saveUser(user);
            };
            refreshUsersParamsFunction = function() {
                return {
                    "tenantId": tenantId,
                    "topIndex": vm.topIndex
                };
            };

        } else if (usersType === 'customer') {
            fetchUsersFunction = function(pageLink) {
                return userService.getCustomerUsers(customerId, pageLink);
            };
            saveUserFunction = function(user) {
                user.authority = "CUSTOMER_USER";
                user.customerId = {
                    entityType: types.entityType.customer,
                    id: customerId
                };
                return userService.saveUser(user);
            };
            refreshUsersParamsFunction = function() {
                return {
                    "customerId": customerId,
                    "topIndex": vm.topIndex
                };
            };
        }

        vm.userGridConfig.refreshParamsFunc = refreshUsersParamsFunction;
        vm.userGridConfig.fetchItemsFunc = fetchUsersFunction;
        vm.userGridConfig.saveItemFunc = saveUserFunction;
    }

    function deleteUserTitle(user) {
        return $translate.instant('user.delete-user-title', {
            userEmail: user.email
        });
    }

    function deleteUserText() {
        return $translate.instant('user.delete-user-text');
    }

    function deleteUsersTitle(selectedCount) {
        return $translate.instant('user.delete-users-title', {
            count: selectedCount
        }, 'messageformat');
    }

    function deleteUsersActionTitle(selectedCount) {
        return $translate.instant('user.delete-users-action-title', {
            count: selectedCount
        }, 'messageformat');
    }

    function deleteUsersText() {
        return $translate.instant('user.delete-users-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function getUserTitle(user) {
        return user ? user.email : '';
    }

    function assignGroupToUsers($event, id) {

        if ($event) {
            $event.stopPropagation();
        }

        var pageSize = 10;

        usergroupService.getGroups(vm.customerId, {
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
                    controller: 'AddGroupsToUserController',
                    controllerAs: 'vm',
                    templateUrl: assignGroupsToUsers,
                    locals: {
                        userId: id,
                        users: users,
                        customerId: vm.customerId
                    },
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function() {
                    vm.grid.refreshList();
                }, function() {});


                $log.log(users, id);
            },
            function fail() {});


    }
    function unassignToUsers($event,id) {

         if ($event) {
             $event.stopPropagation();
         }



         var pageSize = 10;

         usergroupService.assignedGroups(id, {
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
                     controller: 'DeleteGroupsToUserController',
                     controllerAs: 'vm',
                     templateUrl: unassignGroupsToUsers,
                     locals: {
                         userId: id,
                         users: users,
                         customerId: vm.customerId
                     },
                     parent: angular.element($document[0].body),
                     fullscreen: true,
                     targetEvent: $event
                 }).then(function() {
                     vm.grid.refreshList();
                 }, function() {});


                 $log.log(users, id);
             },
             function fail() {});

    }

    function deleteUser(userId) {
        return userService.deleteUser(userId);
    }

    function displayActivationLink(event, user) {
        userService.getActivationLink(user.id.id).then(
            function success(activationLink) {
                openActivationLinkDialog(event, activationLink);
            }
        );
    }

    function openActivationLinkDialog(event, activationLink) {
        $mdDialog.show({
            controller: 'ActivationLinkDialogController',
            controllerAs: 'vm',
            templateUrl: activationLinkDialogTemplate,
            locals: {
                activationLink: activationLink
            },
            parent: angular.element($document[0].body),
            fullscreen: true,
            skipHide: true,
            targetEvent: event
        });
    }

    function resendActivation(user) {
        userService.sendActivationEmail(user.email).then(function success() {
            toast.showSuccess($translate.instant('user.activation-email-sent-message'));
        });
    }
}