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
/* eslint-disable import/no-unresolved, import/default */

import policyTableTemplate from './policy-table.tpl.html';
import policyDialogTemplate from './policy-dialog.tpl.html';

import PolicyDialogController from './policy-dialog.controller'

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function PolicyTblDirective() {

    return {
        restrict: "E",
        scope: true,
        bindToController: {
            groupId: '=?',
            pageMode: '@?',
            userId: '=?',
            entType: '@?',
            customerId: '=?',
            datamodelId: '=?',
            groupObject: '=?'
        },
        controller: PolicyTableController,
        controllerAs: 'vm',
        templateUrl: policyTableTemplate

    };
}

/*@ngInject*/
function PolicyTableController($mdDialog, $document, $scope, userGroupService, $translate) {

    let vm = this;

    vm.addPolicy = addPolicy;
    vm.deletePolicies = deletePolicies;
    vm.deletePolicy = deletePolicy;
    vm.selectedPolicy = [];



    //vm.policyTable = policyTable;


    //vm.policyTable();

    $scope.$watch('vm.selectedPolicy.length', function(newLength) {
        var selectionMode = newLength ? true : false;
        if (vm.ctx) {
            if (selectionMode) {
                vm.ctx.hideTitlePanel = true;
                $scope.$emit("selectedPolicy", true);
            } else if (vm.query.search == null) {
                vm.ctx.hideTitlePanel = false;
                $scope.$emit("selectedPolicy", false);
            }
        }
    });


    function deletePolicies($event) {

        if ($event) {
            $event.stopPropagation();
        }
        if (vm.selectedPolicy && vm.selectedPolicy.length > 0) {
            var title = $translate.instant('policy.delete-policies-title', {
                count: vm.selectedPolicy.length
            }, 'messageformat');
            var content = $translate.instant('policy.delete-policies-text');

            var confirm = $mdDialog.confirm()
                .targetEvent($event)
                .title(title)
                .htmlContent(content)
                .ariaLabel(title)
                .cancel($translate.instant('action.no'))
                .ok($translate.instant('action.yes'));
            $mdDialog.show(confirm).then(function() {

                vm.groupObject.policies = vm.groupObject.policies.filter((i) => (vm.selectedPolicy.indexOf(i) === -1));


                userGroupService.saveUserGroup(vm.groupObject).
                then(function success() {
                    vm.selectedPolicy = [];
                    // vm.ctx.hideTitlePanel = true;

                    reloadPolicyTable();
                }, function fail() {

                });


            });
        }




    }


    function deletePolicy($event, policy) {

        if ($event) {
            $event.stopPropagation();
        }
        if (policy) {
            var title = $translate.instant('policy.delete-policy-title');
            var content = $translate.instant('policy.delete-policy-text');

            var confirm = $mdDialog.confirm()
                .targetEvent($event)
                .title(title)
                .htmlContent(content)
                .ariaLabel(title)
                .cancel($translate.instant('action.no'))
                .ok($translate.instant('action.yes'));
            $mdDialog.show(confirm).then(function() {
                var index = vm.groupObject.policies.indexOf(policy);

                if (index > -1) {
                    vm.groupObject.policies.splice(index, 1);

                }

                vm.groupObject.policies = vm.groupObject.policies;

                userGroupService.saveUserGroup(vm.groupObject).
                then(function success() {
                    reloadPolicyTable();
                }, function fail() {

                });

            });
        }

    }

    function reloadPolicyTable() {
        // vm.policyValues = {};
        vm.policies = [];
        userGroupService.getPolicyList(vm.groupId).
        then(function success(data) {
            angular.forEach(data, function(value, key) {
                var dataModelString = 'ALL';
                var assetString = 'ALL';
                var entTypeStr = 'ALL';
                var permissionStr = 'ALL';
                var lastPos = key.split(":");

                if (key.indexOf('ASSET') != -1) {

                    entTypeStr = 'ASSET';

                } else if (key.indexOf('DEVICE') != -1) {

                    entTypeStr = 'DEVICE';

                }

                if (lastPos[lastPos.length - 1] !== '*') {

                    permissionStr = lastPos[lastPos.length - 1];
                }

                if (angular.isDefined(value.dataModelId) && value.dataModelId !== null) {
                    dataModelString = value.dataModelId;
                }

                if (angular.isDefined(value.id) && value.id !== null) {
                    assetString = value.id;
                }

                vm.policies.push({
                    key: key,
                    dmoStr: dataModelString,
                    assStr: assetString,
                    entStr: entTypeStr,
                    perStr: permissionStr
                });

            });
        });
    }

    $scope.$watch("vm.groupId", function(newVal) {
        if (newVal) {

            reloadPolicyTable();
        }
    });


    function addPolicy($event) {
        if ($event) {
            $event.stopPropagation();
        }
        openPolicyDialog($event);
    }


    function openPolicyDialog($event, policy) {
        if ($event) {
            $event.stopPropagation();
        }
        var isAdd = false;
        if (!policy) {
            isAdd = true;
        }
        $mdDialog.show({
            controller: PolicyDialogController,
            controllerAs: 'vm',
            templateUrl: policyDialogTemplate,
            parent: angular.element($document[0].body),
            locals: {
                isAdd: isAdd,
                groupId: vm.groupId,
                customerId: vm.customerId,
                dataModelId: vm.datamodelId,
                groupObject: vm.groupObject
            },
            bindToController: true,
            targetEvent: $event,
            fullscreen: true,
            skipHide: true
        }).then(function() {
            reloadPolicyTable();
        }, function() {});
    }


}