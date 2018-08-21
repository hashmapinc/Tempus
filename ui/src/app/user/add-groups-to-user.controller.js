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
/*@ngInject*/
export default function AddGroupsToUserController($scope, toast, $translate, userService, usergroupService, $mdDialog, userId, users, customerId, $log) {

    var vm = this;

    vm.users = users;
    vm.users.selections = [];
    vm.searchText = '';
    vm.users.selectedCount = 0;

    vm.assign = assign;
    vm.cancel = cancel;
    vm.hasData = hasData;
    vm.noData = noData;
    vm.searchUserTextUpdated = searchUserTextUpdated;
    vm.toggleUserSelection = toggleUserSelection;
    vm.customerId = customerId;
    vm.groupId = userId;
    vm.groupName = [];
    //vm.email =[];
    $scope.assignedUsers = function() {
        var pageSize = 100;
        usergroupService.assignedGroups(vm.groupId, {
            limit: pageSize,
            textSearch: ''
        }).then(
            function success(assignusers) {
                $log.log(assignusers);
                if (assignusers.data.length > 0) {
                    angular.forEach(assignusers.data, function(value) {
                        $log.log(value);
                        vm.groupName.push(value.name);
                    });
                    vm.users.data = vm.users.data.filter(function(e) {
                        return vm.groupName.indexOf(e.name) == -1;
                    });
                    vm.users.selections = [];
                }
            },
            function fail() {

            });
    };


    $scope.assignedUsers();

    vm.theUsers = {
        getItemAtIndex: function(index) {
            if (index > vm.users.data.length) {
                vm.theUsers.fetchMoreItems_(index);
                return null;
            }
            var item = vm.users.data[index];
            if (item) {
                item.indexNumber = index + 1;
            }

            // $log.log(item);
            return item;
        },

        getLength: function() {
            if (vm.users.hasNext) {
                return vm.users.data.length + vm.users.nextPageLink.limit;
            } else {
                return vm.users.data.length;
            }
        },

        fetchMoreItems_: function() {
            if (vm.users.hasNext && !vm.users.pending) {
                vm.users.pending = true;

                userService.getCustomerUsers(vm.customerId, vm.users.nextPageLink).then(
                    function success(users) {
                        vm.users.data = vm.users.data.concat(users.data);
                        vm.users.nextPageLink = users.nextPageLink;
                        vm.users.hasNext = users.hasNext;
                        if (vm.users.hasNext) {
                            vm.users.nextPageLink.limit = vm.users.pageSize;
                        }
                        vm.users.pending = false;
                    },
                    function fail() {
                        vm.users.hasNext = false;
                        vm.users.pending = false;
                    });
            }
        }
    };

    function cancel() {
        $mdDialog.cancel();
    }

    function assign() {
        //var tasks = [];
        // $log.log(vm.users.selections);
        usergroupService.assignGroupToUser(vm.groupId, vm.users.selections).then(

            function success() {

                $mdDialog.cancel();
                toast.showSuccess($translate.instant('user.user-assigned-message'));

            },

            function fail() {});

    }

    // vm.assignedUsers();

    function noData() {
        return vm.users.data.length == 0 && !vm.users.hasNext;
    }

    function hasData() {
        return vm.users.data.length > 0;
    }

    function toggleUserSelection($event, user) {
        $log.log(user.selected);
        $event.stopPropagation();
        var selected = angular.isDefined(user.selected) && user.selected;

        user.selected = !selected;
        if (user.selected) {
            vm.users.selections.push(user.id.id);
            vm.users.selectedCount++;
        } else {

            var index = vm.users.selections.indexOf(user.id.id)
            vm.users.selections.splice(index, 1);
            vm.users.selectedCount--;
        }
    }
    $log.log(vm.users.selectedCount);

    function searchUserTextUpdated() {
        vm.users = {
            pageSize: vm.users.pageSize,
            data: [],
            nextPageLink: {
                limit: vm.users.pageSize,
                textSearch: vm.searchText
            },
            selections: {},
            selectedCount: 0,
            hasNext: true,
            pending: false
        };
    }

}