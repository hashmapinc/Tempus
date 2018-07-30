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

//import addUserTemplate from './add-user.tpl.html';
//import userCard from './user-card.tpl.html';
//import activationLinkDialogTemplate from './activation-link.dialog.tpl.html';

/* eslint-enable import/no-unresolved, import/default */


/*@ngInject*/

import addDataModel from './add-group.tpl.html';

/*@ngInject*/

export default function UserGroupController($state, $stateParams, $log, $mdDialog, $document) {

   // var tenantId = $stateParams.tenantId;
    var customerId = $stateParams.customerId;
   // var usersType = $state.$current.data.usersType;


    var vm = this;
    vm.addUserGroup = addUserGroup;
    vm.customerId = customerId;
    vm.AddGroupModelController = "AddGroupModelController";


    function addUserGroup($event,id) {
      $log.log(id);
        $mdDialog.show({
            controller: vm.AddGroupModelController,
            controllerAs: 'vm',
            templateUrl: addDataModel,
            parent: angular.element($document[0].body),
            locals: {saveItemFunction: vm.saveDataModelFunc, customerId: id},
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
        }, function () {
        });
    }


}
