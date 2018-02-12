/*
 * Copyright © 2016-2017 The Thingsboard Authors
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

import addComputationJobTemplate from './add-computation-job.tpl.html';
import computationJobCard from './computation-job-card.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export function ComputationJobController(computationJobService, $log, userService, importExport, $state, $stateParams, $filter, $translate, types, helpLinks) {

    var computationJobActionsList = [
        {
            onAction: function ($event, item) {
                activateComputationJob($event, item);
            },
            name: function() { return $translate.instant('action.activate') },
            details: function() { return $translate.instant('computationJob.activate') },
            icon: "play_arrow",
            isEnabled: function(computationJob) {
                return isComputationJobEditable(computationJob) && computationJob && computationJob.state === 'SUSPENDED';
            }
        },
        {
            onAction: function ($event, item) {
                suspendComputationJob($event, item);
            },
            name: function() { return $translate.instant('action.suspend') },
            details: function() { return $translate.instant('computationJob.suspend') },
            icon: "pause",
            isEnabled: function(computationJob) {
                return isComputationJobEditable(computationJob) && computationJob && computationJob.state === 'ACTIVE';
            }
        },
        {
            onAction: function ($event, item) {
                vm.grid.deleteItem($event, item);
            },
            name: function() { return $translate.instant('action.delete') },
            details: function() { return $translate.instant('computationJob.delete') },
            icon: "delete",
            isEnabled: isComputationJobEditable
        }
    ];

    $log.log("HMDC computation Descriptor ", $stateParams.computationId);

    var computationJobAddItemActionsList = [
        {
            onAction: function ($event) {
                vm.grid.addItem($event);
            },
            name: function() { return $translate.instant('action.create') },
            details: function() { return $translate.instant('computationJob.create-new-computationJob') },
            icon: "insert_drive_file"
        }
    ];

    var vm = this;


    //vm.computationDescriptor = $stateParams.jsonDescriptor;
    //$scope.computation = vm.computation;
    //$scope;
    vm.types = types;

    vm.helpLinkIdForComputationJob = helpLinkIdForComputationJob;


    vm.computationJobGridConfig = {

        refreshParamsFunc: null,

        deleteItemTitleFunc: deleteComputationJobTitle,
        deleteItemContentFunc: deleteComputationJobText,
        deleteItemsTitleFunc: deleteComputationJobsTitle,
        deleteItemsActionTitleFunc: deleteComputationJobsActionTitle,
        deleteItemsContentFunc: deleteComputationJobsText,

        addItemController: 'AddComputationJobController',
        fetchItemsFunc: fetchComputationJobs,
        saveItemFunc: saveComputationJob,
        deleteItemFunc: deleteComputationJob,

        getItemTitleFunc: getComputationJobTitle,
        itemCardTemplateUrl: computationJobCard,
        parentCtl: vm,

        actionsList: computationJobActionsList,
        addItemActions: computationJobAddItemActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addComputationJobTemplate,

        addItemText: function() { return $translate.instant('computationJob.add-computationJob-text') },
        noItemsText: function() { return $translate.instant('computationJob.no-computationJobs-text') },
        itemDetailsText: function() { return $translate.instant('computationJob.computationJob-details') },
        isSelectionEnabled: isComputationJobEditable,
        isDetailsReadOnly: function(computationJob) {
            return !isComputationJobEditable(computationJob);
        }

    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.computationJobGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.computationJobGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.isComputationJobEditable = isComputationJobEditable;

    vm.activateComputationJob = activateComputationJob;
    vm.suspendComputationJob = suspendComputationJob;
    //vm.exportComputationJob = exportComputationJob;

    function helpLinkIdForComputationJob() {
        return helpLinks.getComputationJobLink(vm.grid.operatingItem());
    }

    function deleteComputationJobTitle(computationJob) {
        return $translate.instant('computationJob.delete-computationJob-title', {computationJobName: computationJob.name});
    }

    function deleteComputationJobText() {
        return $translate.instant('computationJob.delete-computationJob-text');
    }

    function deleteComputationJobsTitle(selectedCount) {
        return $translate.instant('computationJob.delete-computationJobs-title', {count: selectedCount}, 'messageformat');
    }

    function deleteComputationJobsActionTitle(selectedCount) {
        return $translate.instant('computationJob.delete-computationJobs-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteComputationJobsText() {
        return $translate.instant('computationJob.delete-computationJobs-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function fetchComputationJobs(pageLink) {
        return computationJobService.getAllComputationJobs(pageLink, $stateParams.computationId);
    }

    function saveComputationJob(computationJob) {
        return computationJobService.saveComputationJob(computationJob, $stateParams.computationId);
    }

    function deleteComputationJob(computationJobId) {
        return computationJobService.deleteComputationJob(computationJobId);
    }

    function getComputationJobTitle(computationJob) {
        return computationJob ? computationJob.name : '';
    }

    function isComputationJobEditable(computationJob) {
        if (userService.getAuthority() === 'TENANT_ADMIN') {
            return computationJob && computationJob.tenantId.id != types.id.nullUid;
        } else {
            return userService.getAuthority() === 'SYS_ADMIN';
        }
    }

    /*function exportComputationJob($event, computationJob) {
        $event.stopPropagation();
        importExport.exportComputationJob(computationJob.id.id);
    }*/

    function activateComputationJob(event, computationJob) {
        computationJobService.activateComputationJob(computationJob.id.id).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

    function suspendComputationJob(event, computationJob) {
        computationJobService.suspendComputationJob(computationJob.id.id).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

}


/*@ngInject*/
export function AddComputationJobController(computationService, $stateParams, $log, $scope, $mdDialog, saveItemFunction, helpLinks) {

    var vm = this;

    vm.helpLinks = helpLinks;
    vm.item = {};

    vm.add = add;
    vm.cancel = cancel;

    vm.computation = {};
    vm.computationId = $stateParams.computationId;

    computationService.getComputation($stateParams.computationId).then(
       function success(computation) {
           vm.computation = computation;
           $log.log("Computation success: " + angular.toJson(vm.computation));
       },
       function fail() {
       }
    );

    $log.log("Computation : " + vm.computation);


    function cancel() {
        $mdDialog.cancel();
    }

    function add() {
        saveItemFunction(vm.item).then(function success(item) {
            vm.item = item;
            $scope.theForm.$setPristine();
            $mdDialog.hide();
        });
    }
}