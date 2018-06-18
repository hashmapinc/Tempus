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
import objectStepper from './datamodel-object-stepper.tpl.html';

/*@ngInject*/
export function DataModelController($scope, $log, $mdDialog, $document) {
    //=============================================================================
    // Controller state and functionality
    //=============================================================================
	var vm = this;
    vm.isEdit = false; // keeps track of whether the model is being edited

    // create nodes and edges and load the datamodel
    $scope.nodes = new vis.DataSet();
    $scope.edges = new vis.DataSet();
    loadDatamodel();

    // Configure graph data + options
    var network_data = {
        nodes: $scope.nodes,
        edges: $scope.edges
    };
    var network_options = {
        hierarchicalLayout: {
            direction: "UD"
        }
    };

    // build network, add assign event listeners
    var networkContainer = angular.element("#dataModelViewerContainer")[0];
    var network = new vis.Network(networkContainer, network_data, network_options);
    network.on('selectNode', onDatamodelObjectSelect);

    vm.toggleDMEditMode = function() {
        vm.isEdit = !vm.isEdit;
    }
    //=============================================================================

    //=============================================================================
    // Datamodel manipulation functionality
    //=============================================================================
    // allow dialog to close
    $scope.close = function() {
        $mdDialog.close();
    };

    function saveDatamodel() {
        // TODO: save the data model
        $log.debug("saving data model...");
    }

    function loadDatamodel() {
        // TODO: load the data model
        $log.debug("loading data model...");

        // erase current data
        $scope.nodes.clear();
        $scope.edges.clear();

        // TODO: load this for real
        vm.datamodelTitle = "Dummy Data Model"; 
        var currId = $scope.nodes.length;
        $scope.nodes.add([
            { id: currId + 1, label: 'Node ' + (currId + 1) },
            { id: currId + 2, label: 'Node ' + (currId + 2) },
            { id: currId + 3, label: 'Node ' + (currId + 3) },
            { id: currId + 4, label: 'Node ' + (currId + 4) },
            { id: currId + 5, label: 'Node ' + (currId + 5) }
        ]);
        currId = $scope.edges.length;
        $scope.edges.add([
            { id: currId + 1, from: currId + 1, to: currId + 2 },
            { id: currId + 2, from: currId + 3, to: currId + 2 }
        ]);
    }

    function onDatamodelObjectSelect(properties) {
        $log.debug(properties);

        if (vm.isEdit) {
            // TODO: handle object editing
            alert("editing selected datamodel object:" + properties);
        } else {
            // TODO: handle object reading
            alert("viewing selected datamodel object:" + properties);
        }
    }

    vm.addDatamodelObject = function(ev) {
        // TODO: start the datamodel creation stepper
        $log.debug("adding data model object...");

        $mdDialog.show({
            controller: function(){ return vm},
            controllerAs: 'vm',
            templateUrl: objectStepper,
            parent: angular.element($document[0].body),
            fullscreen: true,
            targetEvent: ev
        }).then(function () {
        }, function () {
        });

        var newNodeLabel = prompt("Label for new node:");
        var id = $scope.nodes.length + 1;
        $scope.nodes.add([{
            id: id, 
            label: newNodeLabel 
        }]);
    };

    vm.acceptDatamodelEdit = function () {
        // save the datamodel and exit edit mode
        $log.debug("accepting datamodel edit...");
        saveDatamodel();
        vm.toggleDMEditMode();
    };

    vm.cancelDatamodelEdit = function() {
        // TODO: reload the graph and discard unsaved changes
        $log.debug("canceling datamodel edit...");
        loadDatamodel();
    };
    //=============================================================================
}
