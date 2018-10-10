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
export default function AddComputationController($scope, $mdDialog, types, helpLinks, computationService, $q, $window) {
    var vm = this;

    vm.helpLinks = helpLinks;
    vm.item = {};

    vm.types = types;
    vm.add = add;
    vm.cancel = cancel;

    function cancel() {
        $mdDialog.cancel();
    }

    vm.computationTypeChange = function () {
        if (vm.item.type === vm.types.computationType.spark) {
            vm.item.importData = null;
            vm.item.fileName = null;
        }else if (vm.item.type === vm.types.computationType.kubeless) {
            vm.item = {
                type: vm.types.computationType.kubeless
            };
        }
    };

    function saveComputation() {
        var deferred = $q.defer();
        if (vm.item.type === 'SPARK') {

            computationService.upload(vm.item.importData.file).then(
                function success() {
                    deferred.resolve();
                    $window.localStorage.setItem('currentTab', 4);
                },
                function fail() {
                    deferred.reject();
                }
            );
        }else if(vm.item.type === vm.types.computationType.kubeless){
            computationService.saveComputation(vm.item).then(
                function success() {
                    deferred.resolve();
                },
                function fail() {
                    deferred.reject();
                }
            );
        }
        return deferred.promise;
    }

    function add() {
        saveComputation().then(function success(item) {
            vm.item = item;
            $scope.theForm.$setPristine();
            $mdDialog.hide();
        });
    }
}