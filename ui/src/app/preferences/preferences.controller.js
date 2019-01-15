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

/*@ngInject*/
export default function PreferencesController($scope, preferencesService, userService, toast, $translate) {


    var vm = this;

    vm.displaySave = false;
    vm.selectedUnit = 'SI';
    vm.saveDetails = saveDetails;
    vm.setUnit = setUnit;
    vm.expandCallback = expandCallback;
    vm.collapseCallback = collapseCallback;

    var user = userService.getCurrentUser();


    initController();

    function initController(){
        preferencesService.getUnitSystem(user.tenantId).then(
           function success(info) {
                vm.selectedUnit = info.unit_system;
           }
        );
    }
    function setUnit (unit){
        vm.selectedUnit = unit;
    }

    function expandCallback(){
        vm.displaySave = true;
    }

    function collapseCallback(){
        vm.displaySave = false;
    }

    function saveDetails() {
        preferencesService.saveUnitSystem(user.tenantId, vm.selectedUnit).then(
           function success() {
                toast.showSuccess($translate.instant('preferences.save-preferences-success'));
           });
    }
}