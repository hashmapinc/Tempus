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
//mport './audit-log.scss';
/* eslint-disable import/no-unresolved, import/default */
import policyTableTemplate from './policy-table.tpl.html';
import policyDialogTemplate from './policy-dialog.tpl.html';

import PolicyDialogController from './policy-dialog.controller'

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function PolicyTblDirective() {

return {
        restrict: "E",
         scope: true,
        bindToController: {
            groupId: '=?',
            pageMode: '@?',
            userId: '=?',
            entType: '@?',
            customerId: '=?'
        },
        controller: PolicyTableController,
        controllerAs: 'vm',
        templateUrl: policyTableTemplate

    };
}

/*@ngInject*/
function PolicyTableController($mdDialog, $document) {

  let vm = this;

   vm.addPolicy = addPolicy;


   function addPolicy($event) {
        if ($event) {
            $event.stopPropagation();
        }
        openPolicyDialog($event);
    }


    function openPolicyDialog($event, policy) {
        if ($event) {
            $event.stopPropagation();
        }
        var isAdd = false;
        if(!policy) {
            isAdd = true;
        }
        $mdDialog.show({
            controller: PolicyDialogController,
            controllerAs: 'vm',
            templateUrl: policyDialogTemplate,
            parent: angular.element($document[0].body),
            locals: {
                isAdd: isAdd,
                groupId: vm.groupId,
                customerId: vm.customerId
            },
            bindToController: true,
            targetEvent: $event,
            fullscreen: true,
            skipHide: true
        }).then(function() {
           // reloadExtensions();
        }, function () {
        });
    }


}