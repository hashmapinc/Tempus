/*
 * Copyright © 2017-2018 Hashmap, Inc
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

import addComputationTemplate from './add-computation.tpl.html';
import computationCard from './computation-card.tpl.html';
import addComputationJobTemplate from './add-computation-job.tpl.html';
import computationJobCard from './computation-job-card.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ComputationController(computationService, computationJobService, userService, importExport, $state, $stateParams, $translate, types) {

    var computationActionsList = [
        {
            onAction: function ($event, item) {
                vm.grid.deleteItem($event, item);
            },
            name: function() { return $translate.instant('action.delete') },
            details: function() { return $translate.instant('computation.delete') },
            icon: "delete",
        }
    ];

    var computationAddItemActionsList = [
        {
            onAction: function ($event) {
                importExport.importComputation($event).then(
                    function() {
                        vm.grid.refreshList();
                    }
                );
            },
            name: function() { return $translate.instant('action.import') },
            details: function() { return $translate.instant('computation.import') },
            icon: "file_upload"
        }
    ];

    var vm = this;
    vm.types = types;

    vm.viewComputations = true;
    vm.viewComputationJobs = false;

    vm.computationGridConfig = {

        refreshParamsFunc: null,

        fetchItemsFunc: fetchComputations,
        deleteItemFunc: deleteComputation,

        clickItemFunc: openComputation,

        getItemTitleFunc: getComputationTitle,
        itemCardTemplateUrl: computationCard,
        parentCtl: vm,

        actionsList: computationActionsList,
        addItemActions: computationAddItemActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addComputationTemplate,

        addItemText: function() { return $translate.instant('computation.add-computation-text') },
        noItemsText: function() { return $translate.instant('computation.no-computations-text') },

    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.computationGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.computationGridConfig.topIndex = $stateParams.topIndex;
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function fetchComputations(pageLink) {

        return computationService.getAllComputations(pageLink);
    }

    function deleteComputation(computationId) {
        return computationService.deleteComputation(computationId);
    }

    function getComputationTitle(computation) {
        return computation ? computation.name : '';
    }
    
    function openComputation($event, computation) {
        if ($event) {
            $event.stopPropagation();
        }

        $state.transitionTo ('.', {computationId: computation.id.id}, { location: false, relative: $state.$current, reload: false, notify: false }) 

                vm.computation = computation;
                vm.viewComputations = false;
                vm.viewComputationJobs = true;
    }

    vm.backtoComputation = function (){
        vm.viewComputations = true;
        vm.viewComputationJobs = false;
    }

    if($stateParams.computationId != null){
        computationService.getComputation($stateParams.computationId).then(
            function success(computation) {
                vm.computation = computation;
                vm.viewComputations = false;
                vm.viewComputationJobs = true;
                openComputation(null, vm.computation);
            },
            function fail() {
            }
        );
       
    }

// Computation Job starts

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

    //var vm = this;
    

    //vm.types = types;

    //vm.helpLinkIdForComputationJob = helpLinkIdForComputationJob;


    vm.computationJobGridConfig = {

        refreshParamsFunc: null,

        deleteItemTitleFunc: deleteComputationJobTitle,
        deleteItemContentFunc: deleteComputationJobText,
        deleteItemsTitleFunc: deleteComputationJobsTitle,
        //deleteItemsActionTitleFunc: deleteComputationJobsActionTitle,
        deleteItemsContentFunc: deleteComputationJobsText,

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

    /*function helpLinkIdForComputationJob() {
        return helpLinks.getComputationJobLink(vm.grid.operatingItem());
    }*/

    function deleteComputationJobTitle(computationJob) {
        return $translate.instant('computationJob.delete-computationJob-title', {computationJobName: computationJob.name});
    }

    function deleteComputationJobText() {
        return $translate.instant('computationJob.delete-computationJob-text');
    }

    function deleteComputationJobsTitle(selectedCount) {
        return $translate.instant('computationJob.delete-computationJobs-title', {count: selectedCount}, 'messageformat');
    }

    /*function deleteComputationJobsActionTitle(selectedCount) {
        return $translate.instant('computationJob.delete-computationJobs-action-title', {count: selectedCount}, 'messageformat');
    }*/

    function deleteComputationJobsText() {
        return $translate.instant('computationJob.delete-computationJobs-text');
    }

    // function gridInited(grid) {
    //     vm.grid = grid;
    // }

    function fetchComputationJobs(pageLink) {
        if(vm.computation != null){
                    return computationJobService.getAllComputationJobs(pageLink, vm.computation.id.id);
        }
        else {
                computationService.getComputation($stateParams.computationId).then(
                function success(computation) {
                    //vm.computation = computation;
                    return computationJobService.getAllComputationJobs(pageLink, computation.id.id)
                },
                function fail() {
                }
            );
        }
    }

    function saveComputationJob(computationJob) {
        return computationJobService.saveComputationJob(computationJob, vm.computation.id.id);
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

    function activateComputationJob(event,computationJob) {
        computationJobService.activateComputationJob(vm.computation.id.id, computationJob.id.id).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

    function suspendComputationJob(event, computationJob) {
        computationJobService.suspendComputationJob(vm.computation.id.id, computationJob.id.id).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

}
