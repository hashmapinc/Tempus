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
export function DataModelController($log, $mdDialog, $document, datamodelService) {
    //=============================================================================
    // Main
    //=============================================================================
    // create the controller
	var vm = this;
    vm.isEdit = false; // keeps track of whether the model is being edited

    // create the stepper
    vm.stepperState = 0; // keeps track of the current stepper step
    vm.stepperData = {}; // keeps track of the in-progress data model object and is bound to the stepper
    resetStepperState(); // instantiate the stepper model and structure the stepperData object

    // load the datamodel
    vm.datamodelObjects = [];
    loadDatamodel();

    // Create the graph that will be plotted
    vm.nodes = new vis.DataSet();
    vm.edges = new vis.DataSet();
    var network_data = {
        nodes: vm.nodes,
        edges: vm.edges
    };
    var network_options = {
        "edges": {
            "smooth": {
                "type": "cubicBezier",
                "forceDirection": "vertical",
                "roundness": 1
            }
        }
    };

    // build the vis network and add assign event listeners
    var networkContainer = angular.element("#dataModelViewerContainer")[0];
    var network = new vis.Network(networkContainer, network_data, network_options);
    network.on('selectNode', onDatamodelObjectSelect);

    // plot the datamodel!
    plotDatamodel();
    //=============================================================================

    // toggle between edit mode and view mode
    vm.toggleDMEditMode = function () {
        vm.isEdit = !vm.isEdit;
    };

    // reset the stepper state and clear its current form data
    function resetStepperState() {
        vm.stepperState = 1; // keeps track of the current stepper step (1-3)
        vm.stepperData = { // keeps track of the in-progress data model object and is bound to the stepper
            id: null,
            name: "",
            desc: "",
            type: "",
            parent: null, // should be {name: parentName, id: parentId}
            currentAttribute: "",
            attributes: [] // array attributes
        }
    }

    // structure for a datamodel object
    function createDatamodelObject(id, name, desc, obj_type, parent, attributes) {
        return {
            id: id,
            name: name,
            desc: desc,
            type: obj_type,
            parent: parent,
            attributes: attributes
        }
    }

    // close the stepper and reset its state
    vm.cancel = function () {
        // hide the dialog
        $mdDialog.hide();

        // reset the stepper
        resetStepperState();
    };

    //=============================================================================
    // Datamodel functionality
    //=============================================================================
    function saveDatamodel() {
        // TODO: save the data model
        $log.debug("saving data model...");
    }

    function loadDatamodel() {
        // TODO: load the data model
        $log.debug("loading data model...");

        // TODO: load this for real
        vm.datamodelTitle = "Dummy Data Model";
        var node_a = createDatamodelObject(3, "Vendor", "A rig", "Asset", null, []);
        var node_b = createDatamodelObject(1, "Rig", "A rig", "Asset", node_a, ["location"]);
        var node_c = createDatamodelObject(2, "Well", "A rig", "Device", node_b, ["location"]);
        vm.datamodelObjects = [node_a, node_b, node_c];
    }

    // plot the current datamodel objects
    function plotDatamodel() {
        $log.debug("plotting data model...");

        // erase current plot
        vm.nodes.clear();
        vm.edges.clear();

        // plot the datamodel
        vm.datamodelObjects.forEach(dmObj => {
            vm.nodes.add({
                id:         dmObj.id,
                label:      dmObj.name  
            });

            if (dmObj.parent) {
                vm.edges.add({ 
                    id:     dmObj.id, 
                    from:   dmObj.parent.id, 
                    to:     dmObj.id
                });
            }
        });

        // fit the datamodel to the screen and redraw
        network.fit();
        network.redraw();
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

    // start the mdDialog for adding a datamodel object
    vm.showDatamodelObjectStepper = function (targetEvent) {
        $log.debug("starting datamodel object stepper...");

        // reset stepper state
        resetStepperState();

        // show the mdDialog
        $mdDialog.show({
            controller: function () { return vm },
            controllerAs: 'vm',
            templateUrl: objectStepper,
            parent: angular.element($document[0].body),
            fullscreen: true,
            targetEvent: targetEvent
        }).then(
        function () {
        }, 
        function () {
        });
    };

    // add the datamodel object to the object list and replot
    vm.addDatamodelObject = function() {
        $log.debug("adding data model object...");

        // add the datamodelObject
        var id = vm.nodes.length + 1; // TODO: get a real ID here
        vm.datamodelObjects.push(
            createDatamodelObject(
                id,
                vm.stepperData.name,
                vm.stepperData.desc,
                vm.stepperData.type,
                vm.stepperData.parent
            )
        );

        // plot the data
        plotDatamodel();

        // hide the stepper and reset its state
        vm.cancel();
    };

    // add a datamodel object attribute to the stepper's current data
    vm.addDatamodelObjectAttribute = function () {
        $log.debug("adding data model object attribute...");

        if (vm.stepperData.currentAttribute) {
            vm.stepperData.attributes.push(vm.stepperData.currentAttribute); // add the attribute if it exists
        }

        vm.stepperData.currentAttribute = ""; // reset the current attribute
    };

    // persist the datamodel and exit edit mode
    vm.acceptDatamodelEdit = function () {
        // save the datamodel and exit edit mode
        $log.debug("accepting datamodel edit...");
        saveDatamodel();
        vm.toggleDMEditMode();
    };

    // discard changes and replot the datamodel
    vm.rejectDatamodelEdit = function() {
        // TODO: reload the graph and discard unsaved changes
        $log.debug("rejecting datamodel edit...");
        loadDatamodel();
        plotDatamodel();
    };
    //=============================================================================
}
