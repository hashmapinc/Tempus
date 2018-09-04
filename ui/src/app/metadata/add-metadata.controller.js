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

/*@ngInject*/
export default function AddMetadataController($scope, $mdDialog, types, helpLinks, metadataService) {


    var vm = this;
    vm.helpLinks = helpLinks;
    vm.item = {};

    vm.types = types;
ta
    vm.add = add;
    vm.cancel = cancel;

    function cancel() {
        $mdDialog.cancel();
    }

    vm.metadataSourceTypeChange = function () {
        if (vm.metadata.source.type === 'JDBC') {
            vm.item.importData = null;
            vm.item.fileName = null;
        }
    };

    vm.metadataSinkTypeChange = function () {
        if (vm.metadata.sink.type === 'REST') {
            vm.item.importData = null;
            vm.item.fileName = null;
        }
    };

    function add() {
        if (vm.metadata.source.type === 'JDBC' && vm.metadata.sink.type === 'REST') {
            metadataService.saveMetadata(vm.metadata).then(function success(response){
                $scope.theForm.$setPristine();
                $mdDialog.hide();
            },function fail(){});
        }
    }
}