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
import 'vis/dist/vis-network.min.css';

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

    // create nodes and edges for dummy graph
    var nodes = new vis.DataSet();
    var edges = new vis.DataSet();
    nodes.add([
        { id: 1, label: 'Node 1' },
        { id: 2, label: 'Node 2' },
        { id: 3, label: 'Node 3' },
        { id: 4, label: 'Node 4' },
        { id: 5, label: 'Node 5' }]);
    edges.add([
        { id: 1, from: 1, to: 2 },
        { id: 2, from: 3, to: 2 }
    ]);

    // Configure dummy graph data + options
    var network_data = {
        nodes: nodes,
        edges: edges
    };
    var network_options = {
        hierarchicalLayout: {
            direction: "UD"
        }
    };

    // build network, add select listener
    var networkContainer = angular.element("#dataModelViewerContainer")[0];
    var network = new vis.Network(networkContainer, network_data, network_options);
    network.on('select', function (properties) {
        $log.debug(properties);
    });
}
