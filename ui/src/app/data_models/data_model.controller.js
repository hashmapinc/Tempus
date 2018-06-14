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
import vis from "vis";

/*@ngInject*/
export function DataModelController($scope, $log, $mdDialog) {
	var vm = this;
    vm.isEdit = false; // keeps track of whether the model is being edited
    vm.data_model = {title: "Dummy Data Model"};

    vm.cancel = function() {
        $mdDialog.cancel();
    }

    vm.toggleDMEditMode = function() {
        vm.isEdit = !vm.isEdit;
    }

    // do node stuff    
    $scope.nodes = new vis.DataSet();
    $scope.edges = new vis.DataSet();
    $scope.network_data = {
        nodes: $scope.nodes,
        edges: $scope.edges
    };
    $scope.network_options = {
        hierarchicalLayout: {
            direction: "UD"
        }

    };

    $scope.onNodeSelect = function (properties) {
        var selected = $scope.task_nodes.get(properties.nodes[0]);
        $log.debug(selected);
    };

    $scope.nodes.add([
        { id: 1, label: 'Node 1' },
        { id: 2, label: 'Node 2' },
        { id: 3, label: 'Node 3' },
        { id: 4, label: 'Node 4' },
        { id: 5, label: 'Node 5' }]);

    $scope.edges.add([
        { id: 1, from: 1, to: 2 },
        { id: 2, from: 3, to: 2 }
    ]);
}
