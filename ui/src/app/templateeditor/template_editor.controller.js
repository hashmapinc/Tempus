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

/*@ngInject*/
export function TemplateEditorController($scope,userGroupService, $translate) {

    var vm = this;

    var editorActionsList = [];

    editorActionsList.push({
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

    vm.templateEditorGridConfig = {

        deleteItemTitleFunc: deleteEditorTitle,
        deleteItemContentFunc: deleteEditorText,
        deleteItemsTitleFunc: deleteEditorsTitle,
        deleteItemsActionTitleFunc: deleteEditorsActionTitle,
        deleteItemsContentFunc: deleteEditorsText,

        deleteItemFunc: deleteEditor,
        parentCtl: vm,
        addIcon:"add",

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


    function deleteEditor(userGroupId) {
        return userGroupService.deleteUserGroup(userGroupId);
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

    function deleteEditorsText() {
        return $translate.instant('user.delete-groups-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }


}