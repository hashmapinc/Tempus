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
export function DataModelController($log, $mdDialog, $document, $stateParams, datamodelService) {
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
    loadDatamodel(plotDatamodel); // load data, plot in the callback

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
        },
        layout: {heirarchical: true}
    };

    // build the vis network and add assign event listeners
    var networkContainer = angular.element("#dataModelViewerContainer")[0];
    var network = new vis.Network(networkContainer, network_data, network_options);
    network.on('selectNode', onDatamodelObjectSelect);
    network.on('dragEnd', function (params) {
        params.nodes.forEach(nodeId => {
            vm.nodes.update({ id: nodeId, allowedToMoveX: false, allowedToMoveY: false });
        });
    });
    network.on('dragStart', function (params) {
        params.nodes.forEach(nodeId => {
            vm.nodes.update({ id: nodeId, allowedToMoveX: true, allowedToMoveY: true });
        });
    });
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
    // save the datamodel and datamodel objects
    function saveDatamodel() {
        $log.debug("saving data model and objects...");
        
        // save the datamodel
        var datamodelToSave = {
            id: {
                id: $stateParams.datamodelId,
                entityType: "DATA_MODEL"
            },
            name: vm.datamodelTitle
        };
        datamodelService.saveDatamodel(datamodelToSave).then(function success(response) {
            $log.debug("successfully saved datamodel..." + response);
        }, function fail(response) {
            $log.error("could not save datamodel..." + response);
        });

        // save the datamodel objects
        vm.datamodelObjects.forEach(dmo => {
            // create the saveable object
            var toSave          = {};
            toSave.dataModelId = { id: $stateParams.datamodelId, entityType: "DATA_MODEL"};
            toSave.id           = {id: dmo.id, entityType: "DATA_MODEL_OBJECT"};
            toSave.description  = dmo.desc;
            toSave.name         = dmo.name;
            toSave.type         = dmo.type;
            if (dmo.parent) {
                toSave.parentId = { id: dmo.parent.id };
            }
            if (dmo.attributes) {
                toSave.attributeDefinitions = dmo.attributes.map(attr => {
                    return {
                        "dataModelObjectId" : dmo.id,
                        "name"              : attr
                    }
                });
            }

            // save the datamodel object
            datamodelService.saveDatamodelObject(toSave, $stateParams.datamodelId).then(function success(response) {
                $log.debug("successfully saved datamodel object..." + response);
            }, function fail(response) {
                $log.error("could not save datamodel object..." + response);
            });
        });
    }

    /**
     * load the data model and call the callback when done
     *  @param callback - function to call when data is successfully loaded
     */
    function loadDatamodel(callback) {
        // TODO: load the data model
        $log.debug("loading data model...");

        // load datamodel
        datamodelService.getDatamodel($stateParams.datamodelId).
        then(function success(data) {
            vm.datamodelTitle = data.name;
        }, function fail(data) {
            $log.error("Could not load datamodel:" + data);
        });

        // load datamodel objects
        datamodelService.getDatamodelObjects($stateParams.datamodelId).
        then(function success(data) {
            $log.info("successfully loaded datamodel objects:" + data);
            
            // process the objects
            var idToIndex = {};         // hashmap to get arr index from id
            vm.datamodelObjects = [];   // clear the datamodelObjects array
            var currIndex = 0;          // keep track of current index
            data.forEach(dmObject => {  // iterate and process each object
                idToIndex[dmObject.id.id] = currIndex++; // increment after assignment
                var parentId = dmObject.parentId ? dmObject.parentId.id : null; // get parent ID if it exists

                // get attributes
                var attributes = dmObject.attributeDefinitions.map(attribute => {
                    return attribute.name;
                });
                
                // push the new object
                vm.datamodelObjects.push(createDatamodelObject(
                    dmObject.id.id,
                    dmObject.name,
                    dmObject.description,
                    dmObject.type,
                    parentId,
                    attributes
                ));
            });

            // convert parent IDs to parent objects with the hashmap
            vm.datamodelObjects.forEach(dmObject => {
                // if hte parent exists as an ID string, convert it to the actual object
                if (dmObject.parent) {
                    dmObject.parent = vm.datamodelObjects[idToIndex[dmObject.parent]];
                }
            });

            if (callback) {
                callback();
            }
        }, function fail(data) {
            $log.error("Could not load datamodel objects:" + data);
        });
    }

    // plot the current datamodel objects
    function plotDatamodel() {
        $log.debug("plotting data model...");

        // erase current plot
        vm.nodes.clear();
        vm.edges.clear();

        // create vis ID hashmap
        var visIDs = {}, currId = 1;
        vm.datamodelObjects.forEach(dmObj => {
            visIDs[dmObj.id] = currId++;
        });

        // plot the datamodel
        vm.datamodelObjects.forEach(dmObj => {
            vm.nodes.add({
                id:         visIDs[dmObj.id],
                label:      dmObj.name  
            });

            if (dmObj.parent) {
                vm.edges.add({ 
                    id:     visIDs[dmObj.id], 
                    from:   visIDs[dmObj.parent.id], 
                    to:     visIDs[dmObj.id]
                });
            }
        });

        // center the view after the plotting is finished
        network.once('afterDrawing', function (params) {
            // focus the camera on the new nodes
            var nodeIds = Array.from(vm.datamodelObjects.keys()).map(x => { return x + 1 });
            network.fit({
                nodes: nodeIds,
                animation: true
            })
        });
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
        $log.debug("creating data model object...");

        // create the object to get an id
        datamodelService.saveDatamodelObject(
            {"name": vm.stepperData.name}, 
            $stateParams.datamodelId
        ).then(function success(response) {
            $log.debug("successfully created datamodel object..." + response);

            // add the datamodelObject to the datamodelObjects array
            vm.datamodelObjects.push(
                createDatamodelObject(
                    response.data.id.id,
                    vm.stepperData.name,
                    vm.stepperData.desc,
                    vm.stepperData.type,
                    vm.stepperData.parent,
                    vm.stepperData.attributes
                )
            );

            // plot the data
            plotDatamodel();

            // hide the stepper and reset its state
            vm.cancel();
        }, function fail(response) {
            $log.error("could not create datamodel object..." + response);
        });
    };

    // add a datamodel object attribute to the stepper's current data
    vm.addDatamodelObjectAttribute = function () {
        $log.debug("adding data model object attribute...");
        // add the attribute if it exists
        if (vm.stepperData.currentAttribute) {
            vm.stepperData.attributes.push(vm.stepperData.currentAttribute); 
        }
        // reset the current attribute
        vm.stepperData.currentAttribute = ""; 
    };

    // persist the datamodel and exit edit mode
    vm.acceptDatamodelEdit = function () {
        $log.debug("accepting datamodel edit...");
        saveDatamodel();
        vm.toggleDMEditMode();
    };

    // discard changes and replot the datamodel
    vm.rejectDatamodelEdit = function() {
        $log.debug("rejecting datamodel edit...");
        loadDatamodel(plotDatamodel); // load data and plot in the callback
    };
    //=============================================================================
}
