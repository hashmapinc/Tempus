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
import './gateway.scss';
import addTemplateEditor from './add-template-editor.tpl.html';

/*@ngInject*/
export function TemplateEditorController($scope,userGroupService, $translate, customerService, $mdDialog, $document, $stateParams, userService, $log) {

    var vm = this;
    vm.addEditor = addEditor;
    vm.AddTemplateModelController = 'AddTemplateModelController';
    var customerId = $stateParams.customerId;

    var editorActionsList = [];

     editorActionsList.push({
          onAction: function($event, item) {
              vm.grid.deleteItem($event, item);
          },
          name: function() {
              return $translate.instant('action.delete')
          },
          details: function() {
              return $translate.instant('templateEditor.copy')
          },
          icon: "content_copy"
      });


    editorActionsList.push({
        onAction: function($event, item) {
            vm.grid.deleteItem($event, item);
        },
        name: function() {
            return $translate.instant('action.delete')
        },
        details: function() {
            return $translate.instant('templateEditor.delete')
        },
        icon: "delete"
    });



    $scope.tableView = false;

    vm.templateEditorGridConfig = {

        deleteItemTitleFunc: deleteEditorTitle,
        deleteItemContentFunc: deleteEditorText,
        deleteItemsTitleFunc: deleteEditorsTitle,
        deleteItemsActionTitleFunc: deleteEditorsActionTitle,
        deleteItemsContentFunc: deleteEditorsText,
        addItemController: 'AddTemplateModelController',
        deleteItemFunc: deleteEditor,
        addItemTemplateUrl: addTemplateEditor,
        parentCtl: vm,
        addIcon:"add",
        getItemTitleFunc: getTemplateTitle,
        actionsList: editorActionsList,
        onGridInited: gridInited,
        noItemsText: function() {
            return $translate.instant('customer.no-groups-text')
        },
        itemDetailsText: function() {
            return $translate.instant('customer.usergroup')
        },
        addItemText: function() {
            return $translate.instant('templateEditor.add-template-text')
        },
        entType: "templateEditor"

    }

    initController();

     function initController() {
            var fetchTemplateFunction = null;
            var refreshUsersGroupParamsFunction = null;
            var saveUserGroupFunction = null;
             fetchTemplateFunction = function(pageLink) {
                    var currentUser = userService.getCurrentUser();
                    $log.log(currentUser);
                    return customerService.getCustomers(pageLink)
             };

            vm.templateEditorGridConfig.fetchItemsFunc = fetchTemplateFunction;

     }

    function deleteEditor(userGroupId) {
        return userGroupService.deleteUserGroup(userGroupId);
    }


    function getTemplateTitle(customer) {
            return customer ? customer.title : '';
    }


    function deleteEditorTitle(usergroup) {
        return $translate.instant('user.delete-usergroup-title', {
            groupName: usergroup.name
        });
    }

    function deleteEditorText() {
        return $translate.instant('user.delete-usergroup-text');
    }

    function deleteEditorsTitle(selectedCount) {
        return $translate.instant('user.delete-groups-title', {
            count: selectedCount
        }, 'messageformat');
    }

    function deleteEditorsActionTitle(selectedCount) {
        return $translate.instant('user.delete-groups-action-title', {
            count: selectedCount
        }, 'messageformat');
    }

    function addEditor($event, id) {

            $mdDialog.show({
                controller: vm.AddTemplateModelController,
                controllerAs: 'vm',
                templateUrl: addTemplateEditor,
                parent: angular.element($document[0].body),
                locals: {
                    saveItemFunction: vm.saveGroup,
                    customerId: id
                },
                fullscreen: true,
                targetEvent: $event
            }).then(function() {}, function() {});
     }


    function deleteEditorsText() {
        return $translate.instant('user.delete-groups-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }


}