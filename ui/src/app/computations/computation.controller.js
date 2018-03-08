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
export default function ComputationController(computationService, $log, userService, importExport, $state, $stateParams, $translate, types) {

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
        $log.log("Computaion json " + computation.id.id);
        $state.go('home.computations.computationJob',{computationId: computation.id.id});
    }

}
