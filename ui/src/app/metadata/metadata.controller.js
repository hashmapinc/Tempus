/*
 * Copyright © 2017-2018 Hashmap, Inc
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

/*@ngInject*/

export function AddMetadataController($scope, $mdDialog, saveItemFunction, helpLinks, $state) {

    var vm = this;
    vm.helpLinks = helpLinks;
    vm.item = {};

    vm.add = add;
    vm.cancel = cancel;

    function cancel() {
        $mdDialog.cancel();
    }

    function add() {

        saveItemFunction(vm.item).then(function success(item) {
            vm.item = item;
            $scope.theForm.$setPristine();
            $mdDialog.hide();

            // go to the newly created data model
            $state.go('home.data_models.data_model', { dashboardId: 123 });
        });
    }
}

/*@ngInject*/
export function MetadataController($scope, $rootScope, $state, $stateParams, userService, datamodelService, deviceService, types, attributeService, $q, dashboardService, applicationService, entityService, tempusboardService, utils, $filter, dashboardUtils, $mdDialog, $document, $translate) {

    var vm = this;

    vm.openDataModelDialog = openDataModelDialog;
    vm.cancel = cancel;
    vm.saveDataModelFunc = saveDataModelFunc;
    vm.AddDataModelController = AddMetadataController;
    vm.listDataModel = listDataModel;


    function openDataModelDialog($event) {

        $mdDialog.show({
            controller: vm.AddDataModelController,
            controllerAs: 'vm',
            templateUrl: '',
            parent: angular.element($document[0].body),
            locals: {saveItemFunction: vm.saveDataModelFunc},
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
        }, function () {
        });
    }


    function saveDataModelFunc(item) {

        var deferred = $q.defer();
        datamodelService.saveDataModel(item).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;

    }

    function cancel() {
        $mdDialog.cancel();
    }

    function listDataModel() {

        var deferred = $q.defer();
        datamodelService.listDataModel().then(function success(response) {
            deferred.resolve(response.data);
        }, function fail(response) {
            deferred.reject(response.data);
        });
        return deferred.promise;
    }

}
