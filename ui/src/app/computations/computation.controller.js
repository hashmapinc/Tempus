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

import addComputationTemplate from './add-computation.tpl.html';
import computationCard from './computation-card.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ComputationController(computationService, $log, userService, importExport, $state, $stateParams, $filter, $translate, types, helpLinks, $scope) {

    var computationActionsList = [
        {
            onAction: function ($event, item) {
                vm.grid.deleteItem($event, item);
            },
            name: function() { return $translate.instant('action.delete') },
            details: function() { return $translate.instant('computation.delete') },
            icon: "delete",
            isEnabled: isComputationEditable
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

    $scope.computation = "My computation";

    var vm = this;

    vm.types = types;

    vm.helpLinkIdForComputation = helpLinkIdForComputation;

    vm.computationGridConfig = {

        refreshParamsFunc: null,

        deleteItemTitleFunc: deleteComputationTitle,
        deleteItemContentFunc: deleteComputationText,
        deleteItemsTitleFunc: deleteComputationsTitle,
        deleteItemsActionTitleFunc: deleteComputationsActionTitle,
        deleteItemsContentFunc: deleteComputationsText,

        fetchItemsFunc: fetchComputations,
        saveItemFunc: saveComputation,
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
        itemDetailsText: function() { return $translate.instant('computation.computation-details') },
        isSelectionEnabled: isComputationEditable,
        isDetailsReadOnly: function(computation) {
            return !isComputationEditable(computation);
        }

    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.computationGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.computationGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.isComputationEditable = isComputationEditable;

    //vm.exportComputation = exportComputation;

    function helpLinkIdForComputation() {
        return helpLinks.getComputationLink(vm.grid.operatingItem());
    }

    function deleteComputationTitle(computation) {
        return $translate.instant('computation.delete-computation-title', {computationName: computation.name});
    }

    function deleteComputationText() {
        return $translate.instant('computation.delete-computation-text');
    }

    function deleteComputationsTitle(selectedCount) {
        return $translate.instant('computation.delete-computations-title', {count: selectedCount}, 'messageformat');
    }

    function deleteComputationsActionTitle(selectedCount) {
        return $translate.instant('computation.delete-computations-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteComputationsText() {
        return $translate.instant('computation.delete-computations-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function fetchComputations(pageLink) {
        $log.error("HMDC fetching computations")
        return computationService.getAllComputations(pageLink);
    }

    function saveComputation(computation) {
        return computationService.saveComputation(computation);
    }

    function deleteComputation(computationId) {
        return computationService.deleteComputation(computationId);
    }

    function getComputationTitle(computation) {
        return computation ? computation.name : '';
    }

    function isComputationEditable(computation) {
        if (userService.getAuthority() === 'TENANT_ADMIN') {
            return computation && computation.tenantId.id != types.id.nullUid;
        } else {
            return userService.getAuthority() === 'SYS_ADMIN';
        }
    }

    /*function exportComputation($event, computation) {
        $event.stopPropagation();
        importExport.exportComputation(computation.id.id);
    }*/
    
    function openComputation($event, computation) {
        if ($event) {
            $event.stopPropagation();
        }
        $log.log("Computaion json " + computation.id.id);
        //$state.go('home.dashboards.dashboard', {dashboardId: dashboard.id.id});
        //$state.go('home.computationJob', {jsonDescriptor: angular.toJson(computation.jsonDescriptor)});
        $state.go('home.computationJob',{computationId: computation.id.id});
    }

}
