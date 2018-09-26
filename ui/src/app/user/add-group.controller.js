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


/* eslint-enable import/no-unresolved, import/default */


/*@ngInject*/
export default function AddGroupModelController($state, $stateParams, $scope, types, $mdDialog, saveItemFunction, helpLinks) {

    var vm = this;
    vm.helpLinks = helpLinks;
    vm.item = {};
    var customerId = $stateParams.customerId;

    vm.add = add;
    vm.cancel = cancel;

    function cancel() {
        $mdDialog.cancel();
    }


    function add() {

          vm.item.customerId = {
               entityType: types.entityType.customer,
               id: customerId
           };

        saveItemFunction(vm.item).then(function success(item) {
            vm.item = item;
            $scope.theForm.$setPristine();
            $mdDialog.hide();
        });
    }
}
