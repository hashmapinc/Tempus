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
export function DataModelController($log, $mdDialog, $document, $stateParams, $timeout, datamodelService) {
    //=============================================================================
    // Main
    //=============================================================================
    // create the controller
	var vm = this;
    vm.isEdit = false; // keeps track of whether the model is being edited

    // create the stepper
    vm.stepperState = 0;        // keeps track of the current stepper step
    vm.stepperData = {};        // keeps track of the in-progress data model object and is bound to the stepper
    resetStepperState();        // instantiate the stepper model and structure the stepperData object

    // Create the graph that will be plotted
    vm.visIDs = {} // hashmap of object id strings -> visjs ids
    vm.nodes = new vis.DataSet();
    vm.edges = new vis.DataSet();
    var network_data = {
        nodes: vm.nodes,
        edges: vm.edges
    };
    var network_options = {
        layout: {
            hierarchical: {
                enabled: true,
                nodeSpacing: 100,
                direction: "UD",
                sortMethod: "directed"
            }
        },
        groups: {
            Device: {
                shape: 'icon',
                icon: {
                    face: 'FontAwesome',
                    code: '\uf1b2',
                    size: 50,
                    color: '#607D8B'
                }
            },
            Asset: {
                shape: 'icon',
                icon: {
                    face: 'FontAwesome',
                    code: '\uf1b3',
                    size: 50,
                    color: '#FF5722'
                }
            }
        }
    };

    // build the vis network and add assign event listeners
    var networkContainer = angular.element("#dataModelViewerContainer")[0];
    var network = new vis.Network(networkContainer, network_data, network_options);
    network.on('selectNode', onDatamodelObjectSelect);

    // load the datamodel
    loadDatamodel();
    //=============================================================================

    // toggle between edit mode and view mode
    vm.toggleDMEditMode = function () {
        vm.isEdit = !vm.isEdit;
    };

    // reset the stepper state and clear its current form data
    function resetStepperState() {
        vm.stepperState = 0; // keeps track of the current stepper step (0-3)
        vm.stepperData = {   // keeps track of the in-progress data model object and is bound to the stepper
            id: null,
            name: "",
            desc: "",
            type: "",
            parent: null,      // should be {name: parentName, id: parentId}
            currentAttribute: "",
            attributes: [],    // array attributes
            editingIndex: null // when editing an existing node, this is the index of the node in the local array
        }
    }

    // structure for a datamodel object
    function createDatamodelObject(id, name, desc, obj_type, parent_id, attributes) {
        return {
            id: id,
            name: name,
            desc: desc,
            type: obj_type,
            parent_id: parent_id,
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
        vm.nodes.forEach(node => {
            // get the datamodel object associated with this node
            var dmo = node.datamodelObject;

            // create the saveable object
            var toSave          = {};
            toSave.dataModelId = { id: $stateParams.datamodelId, entityType: "DATA_MODEL"};
            toSave.id           = {id: dmo.id, entityType: "DATA_MODEL_OBJECT"};
            toSave.description  = dmo.desc;
            toSave.name         = dmo.name;
            toSave.type         = dmo.type;
            if (dmo.parent_id) {
                toSave.parentId = { id: dmo.parent_id, entityType: "DATA_MODEL_OBJECT"};
            }
            if (dmo.attributes) {
                toSave.attributeDefinitions = dmo.attributes.map(attr => {
                    return {
                        "dataModelObjectId" : toSave.id,
                        "name"              : attr,
                        "valueType"         : "STRING"
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
     * load the data model 
     */
    function loadDatamodel() {
        $log.debug("loading data model...");

        // erase current plot
        vm.nodes.clear();
        vm.edges.clear();

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
            var datamodelObjects = [];  // array of processed dm objects
            vm.visIDs = {};             // clear the hashmap
            var currId = 1;             // keeps track of current visjs ID
            data.forEach(dmo => {       // iterate and process each object
                // record object ID into hashmap
                vm.visIDs[dmo.id.id] = currId++;

                // get parent ID if it exists
                var parentId = dmo.parentId ? dmo.parentId.id : null; 

                // get attributes
                var attributes = dmo.attributeDefinitions.map(attribute => {
                    return attribute.name;
                });
                
                // push the new object
                datamodelObjects.push(createDatamodelObject(
                    dmo.id.id,
                    dmo.name,
                    dmo.description,
                    dmo.type,
                    parentId,
                    attributes
                ));
            });

            // add the objects to the nodes array
            var currEdgeId = 1; // current ID of an edge
            datamodelObjects.forEach(dmo => {
                vm.nodes.add({
                    id: vm.visIDs[dmo.id], // get visjs ID from dmo ID string using visIDs hashmap
                    label: dmo.name,
                    group: dmo.type,
                    datamodelObject: dmo
                });
            });

            plotDatamodel();
            
        }, function fail(data) {
            $log.error("Could not load datamodel objects:" + data);
        });
    }

    /**
     * plot the datamodel
     */
    function plotDatamodel() {
        // add new edges if necessary to the edges list
        vm.edges.clear();
        var currEdgeId = 1;
        vm.nodes.forEach(node => {
            var dmo = node.datamodelObject;
            if (dmo.parent_id) {
                vm.edges.add({
                    id: currEdgeId++,
                    from: vm.visIDs[dmo.parent_id],
                    to: vm.visIDs[dmo.id]
                });
            }
        });

        // center the view after the drawing is finished
        network.once('afterDrawing', function (params) {
            // focus the camera on the new nodes
            network.fit({
                nodes: vm.nodes.getIds(),
                animation: true
            });
        });

        // turn off physics after the graph is finished settling
        network.once('stabilized', function (params) {
            network.setOptions({ nodes: { physics: false } });
        });
    }

    // handle the selection of a visjs node
    function onDatamodelObjectSelect(properties) {
        $log.debug(properties);

        // get the node that was selected
        var nodeId = properties.nodes[0];
        var node = vm.nodes.get(nodeId);

        if (vm.isEdit) {
            // handle object editing
            vm.showDatamodelObjectStepper(null, node);
        } else {
            // handle object viewing
            var content = // generate prettified json html
                '<h5>' + 
                angular.toJson(node.datamodelObject, true).replace(/\n/g, '<br/>').replace(/[\,\{\}]/g, '') + 
                '</h5>';
            $mdDialog.show($mdDialog.alert({
                title: 'Object Information',
                htmlContent: content,
                ok: 'Close'
            }));
        }
    }

    /**
     * start the mdDialog for adding a datamodel object
     * 
     * @param targetEvent - event, if any, that triggered this show
     * @param nodeToEdit - visjs node to edit if a node is being edited, otherwise null for new node
     */
    vm.showDatamodelObjectStepper = function (targetEvent, nodeToEdit) {
        $log.debug("starting datamodel object stepper...");
        // reset stepper state
        resetStepperState();

        // load datamodel object into stepper data if one is being edited
        if (nodeToEdit) {
            // sanity check
            if(!nodeToEdit.datamodelObject) {
                return;
            }
            var dmo = nodeToEdit.datamodelObject;
            vm.stepperData.id = dmo.id;
            vm.stepperData.name = dmo.name;
            vm.stepperData.desc = dmo.desc;
            vm.stepperData.type = dmo.type;
            vm.stepperData.attribute = dmo.attribute;
            vm.stepperState = 3; // go straight to review page

            // get parent if it exists
            vm.stepperData.parent = null;

            if(dmo.parent_id) {
                var parentNode = vm.nodes.get(vm.visIDs[dmo.parent_id]);
                vm.stepperData.parent = parentNode.datamodelObject;
            }
        }

        // show the mdDialog
        $mdDialog.show({
            controller: function () { return vm }, // use the current controller (this) as the mdDialog controller
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

    // listen for datamodel stepper enter keypresses
    vm.onStepperEnter = function() {
        $timeout(function() {
            // handle object info tab
            if (vm.stepperState === 0) {
                angular.element('#stepperNext').click();

                // handle attributes tab
            } else if (vm.stepperState === 1) {
                vm.stepperData.currentAttribute.length === 0 ? angular.element('#stepperNext').click() : angular.element('#stepperAddAttrButton').click();

                // handle relationships tab
            } else if (vm.stepperState === 2) {
                angular.element('#stepperNext').click();

                // handle review tab
            } else if (vm.stepperState === 3) {
                angular.element('#stepperSubmit').click();
            }
        });
    } 

    // add the datamodel object to the object list and replot
    vm.onStepperSubmit = function() {
        // process an object that alread has an ID
        if (vm.stepperData.id) {
            $log.debug("updating data model object...");

            // get the visjs node associated with this ID
            var nodeId = vm.visIDs[vm.stepperData.id];
            var node = vm.nodes.get(nodeId);

            // update the node
            node.datamodelObject = createDatamodelObject(
                vm.stepperData.id,
                vm.stepperData.name,
                vm.stepperData.desc,
                vm.stepperData.type,
                vm.stepperData.parent ? vm.stepperData.parent.id : null, // parent ID if it exists
                vm.stepperData.attributes
            );
            node.label = vm.stepperData.name;
            node.group = vm.stepperData.type;

            // merge the node changes back into nodes
            vm.nodes.update(node);

            // plot the data
            plotDatamodel();

            // hide the stepper and reset its state
            vm.cancel();

        // process an object that does not exist already
        } else {
            $log.debug("creating data model object...");
            // create the object to get an id
            datamodelService.saveDatamodelObject(
                { "name": vm.stepperData.name },
                $stateParams.datamodelId
            ).then(function success(response) {
                $log.debug("successfully created datamodel object..." + response);

                // parse the response into a datamodelObject 
                var dmo = createDatamodelObject(
                    response.data.id.id,
                    vm.stepperData.name,
                    vm.stepperData.desc,
                    vm.stepperData.type,
                    vm.stepperData.parent ? vm.stepperData.parent.id : null, // parent ID if it exists,
                    vm.stepperData.attributes
                );

                // record the ID into the hashmap
                vm.visIDs[dmo.id] = vm.nodes.length + 1; // increase node ID by 1

                // add a new node into the nodes list
                vm.nodes.add({
                    id: vm.visIDs[dmo.id], // get visjs ID from dmo ID string using visIDs hashmap
                    label: dmo.name,
                    group: dmo.type,
                    datamodelObject: dmo
                });

                // plot the data
                plotDatamodel();

                // hide the stepper and reset its state
                vm.cancel();
            }, function fail(response) {
                $log.error("could not create datamodel object..." + response);
            });
        }
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
        loadDatamodel(); // reload the data
    };
    //=============================================================================
}
