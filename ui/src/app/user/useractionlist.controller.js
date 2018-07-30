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

export default function UserActionListController($state, $stateParams) {

   // var tenantId = $stateParams.tenantId;
    var customerId = $stateParams.customerId;
   // var usersType = $state.$current.data.usersType;


    var vm = this;
    vm.addUserGroup = addUserGroup;

   // vm.types = types;

    vm.customerId = customerId;


    function addUserGroup($event,id) {
        if ($event) {
            $event.stopPropagation();
        }
        $state.go('home.customers.usergroups', {customerId: id});
    }


}
