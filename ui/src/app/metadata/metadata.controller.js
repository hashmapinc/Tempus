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


/* eslint-disable import/no-unresolved, import/default, no-unused-vars */
import addMetadataModel from './add-metadata.tpl.html';

/*@ngInject*/

export default function MetadataController(metadataService, $q,$mdDialog, $document, $state, $translate, types) {



    var vm = this;
    vm.types = types;
    var metadataActionsList =[],metadataGroupActionsList=[];
    vm.openMetadataDialog = openMetadataDialog;
    vm.cancel = cancel;
    vm.AddDataModelController = 'AddMetadataController';


    vm.metadataConfig = {
        deleteItemTitleFunc: deleteMetadataTitle,
        deleteItemContentFunc: deleteMetadataText,
        deleteItemsTitleFunc: deleteMetadataTitle,
        deleteItemsActionTitleFunc: deleteMetadataActionTitle,
        deleteItemsContentFunc: deleteMetadataText,
       
        getItemTitleFunc: getMetadataTitle,

        parentCtl: vm,
        saveItemFunc: saveMetadataConfig,


        actionsList: metadataActionsList,
        groupActionsList: metadataGroupActionsList,
        onGridInited: gridInited,

        addItemTemplateUrl: addMetadataModel,

        addItemText: function() { return $translate.instant('metadataConfig.add-metadata-text') },
        noItemsText: function() { return $translate.instant('metadataConfig.no-metadata-text') },
        itemDetailsText: function() { return $translate.instant('metadataConfig.metadata-details') }
    };

    function saveMetadataConfig(metadata) {
        var deferred = $q.defer();
               metadataService.saveMetadata(metadata).then(
                   function success() {
                   },
                   function fail() {
                       deferred.reject();
                   }
               );
               return deferred.promise;
    }

    function deleteMetadataTitle(metadata) {
        return $translate.instant('metadataConfig.delete-metadata-title', {metadataName: metadata.name});
    }

    function deleteMetadataText() {
        return $translate.instant('metadataConfig.delete-metadata-text');
    }


    function deleteMetadataActionTitle(selectedCount) {
        return $translate.instant('metadataConfig.delete-metadata-action-title', {count: selectedCount}, 'messageformat');
    }

    function getMetadataTitle(metadata) {
        return metadata ? metadata.name : '';
    }


    function gridInited(grid) {
        vm.grid = grid;
    }


    initController();

    function initController() {
        var fetchMetadataFunction = null;
        var deleteMetadataFunction = null;

        fetchMetadataFunction = function (pageLink) {
            return metadataService.getAllTenantMetadata(pageLink);
        };
        deleteMetadataFunction = function (metadataId) {
            return metadataService.deleteMetadata(metadataId);
        };

        vm.metadataConfig.fetchItemsFunc = fetchMetadataFunction;
        vm.metadataConfig.deleteItemFunc = deleteMetadataFunction;
        metadataActionsList.push(
            {
                onAction: function ($event, item) {
                    vm.grid.deleteItem($event, item);
                },
                name: function() { return $translate.instant('action.delete') },
                details: function() { return $translate.instant('metadataConfig.delete') },
                icon: "delete"
            }
        );
        metadataGroupActionsList.push(
            {
                onAction: function ($event) {
                    vm.grid.deleteItems($event);
                },
                name: function() { return $translate.instant('asset.delete-assets') },
                details: deleteMetadataActionTitle,
                icon: "delete"
            }
        );
    }

    function openMetadataDialog($event) {

        $mdDialog.show({
            controller: vm.AddDataModelController,
            controllerAs: 'vm',
            templateUrl: addMetadataModel,
            parent: angular.element($document[0].body),
            locals: {},
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

    function cancel() {
        $mdDialog.cancel();
    }
}

