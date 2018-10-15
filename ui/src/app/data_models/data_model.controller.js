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
import vis from "vis";
import 'vis/dist/vis-network.min.css';
import objectStepper from './datamodel-object-stepper.tpl.html';
import objectInformation from './object_info.tpl.html';

/*@ngInject*/
export function DataModelController($scope, $mdDialog, $document, $stateParams, $timeout, $q, datamodelService, toast, $translate) {
    //=============================================================================
    // Main
    //=============================================================================
    // create the controller
	var vm = this;
    vm.isEdit = false; // keeps track of whether the model is being edited

    // create the stepper
    vm.stepperState = 0;        // keeps track of the current stepper step
    vm.stepperMode = "";        // keeps track of the current stepper mode ("CREATE" or "EDIT")
    vm.stepperData = {};        // keeps track of the in-progress data model object and is bound to the stepper
    resetStepperState();        // instantiate the stepper model and structure the stepperData object

    // manage persistence states
    var objectDeleteList = [];  // list of datamodel object ID's to delete when changes are confirmed
    var objectDeleteNameList = [];
    vm.fileAdded = fileAdded;
    vm.clearFile = clearFile;
    vm.getNodes = getNodes;
    vm.dataModelName =[];
    vm.dataModelSavedName = [];
    $scope.editing = false;


    // Create the graph that will be plotted
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
        vm.stepperState = 0;    // keeps track of the current stepper step (0-3)
        vm.stepperMode = "";    // either CREATE or EDIT. Usefull for hiding/showing the delete option
        vm.stepperData = {      // keeps track of the in-progress data model object and is bound to the stepper
            id: null,           // datamodel object id
            node_id: null,      // visjs node id
            name: "",
            desc: "",
            type: "",
            parent_node_id: null,   // visjs node id of the parent
            currentAttribute: "",
            attributes: []
        }
    }

    // structure for a datamodel object
    function createDatamodelObject(id, name, desc, obj_type, attributes, logoFile) {
        return {
            id: id,
            name: name,
            desc: desc,
            type: obj_type,
            attributes: attributes,
            logoFile:logoFile
        }
    }

    // close the stepper and reset its state
    vm.cancel = function () {
        // hide the dialog
        $mdDialog.hide();

        // reset the stepper
        resetStepperState();
    };

     function getNodes() {
        var new_nodes = vm.nodes.get();
        if(vm.stepperData.id !== null && vm.stepperData.parent_node_id !== null) {
//            let edge = vm.edges.get().pop();
//             if (edge) {
//                  var child_id = edge.to;
//              }
            new_nodes = new_nodes.filter(node => {
                   return vm.stepperData.id !== node.datamodelObject.id; // true if id does not exist
             });
        } else if(vm.stepperData.id !== null && vm.stepperData.parent_node_id == null) {
            new_nodes = new_nodes.filter(node => {
                   return vm.stepperData.name !== node.label; // true if id does not exist
             });
        }
        return new_nodes;
     }
    //=============================================================================
    // Datamodel functionality
    //=============================================================================
    // save the datamodel and datamodel objects
    function saveDatamodel() {
        // save the datamodel
        var datamodelToSave = {
            id: {
                id: $stateParams.datamodelId,
                entityType: "DATA_MODEL"
            },
            name: vm.datamodelTitle,
            additionalInfo:vm.additionalInfo

        };
        datamodelService.saveDatamodel(datamodelToSave);
        var new_nodes = vm.nodes.get().filter(node => {
            return !node.datamodelObject.id; // true if id does not exist
        });

        // create an array of promises for each create call
        var promises = new_nodes.map(node => {  // return promises for each node
            // get ID's

            return datamodelService.saveDatamodelObject(
                { "name": node.datamodelObject.name }, // name doesn't matter now, just need the ID
                $stateParams.datamodelId
            );
        });

        // once all promises resolve, we'll have enough IDs to save the model
       // vm.dataModelName =[];
        $q.all(promises).then(function success(response) {

            // save the new id in the new nodes
            response.forEach(r => {
                let node = new_nodes.pop();
                node.datamodelObject.id = r.data.id.id;
                vm.nodes.update(node);
            });

            // create a map from each dmo ID to it's parent dmo ID
            var parentMap = {};
            vm.edges.forEach(edge => {
                let child_id = vm.nodes.get(edge.to).datamodelObject.id;
                let parent_id = vm.nodes.get(edge.from).datamodelObject.id;
                parentMap[child_id] = parent_id;
            });

            // save the datamodel objects
            vm.nodes.forEach(node => {
                // get the datamodel object associated with this node
                let dmo = node.datamodelObject;

                // create the saveable object
                let toSave = {};
                toSave.dataModelId = { id: $stateParams.datamodelId, entityType: "DATA_MODEL" };
                toSave.id = { id: dmo.id, entityType: "DATA_MODEL_OBJECT" };
                toSave.description = dmo.desc;
                toSave.name = dmo.name;
                toSave.type = dmo.type;
                toSave.logoFile = dmo.logoFile;
                if (parentMap[dmo.id]) { // get parent dmo ID, if it exists
                    toSave.parentId = { id: parentMap[dmo.id], entityType: "DATA_MODEL_OBJECT" };
                }
                if (dmo.attributes) { // get attributes, if any
                    toSave.attributeDefinitions = dmo.attributes.map(attr => {
                        return {
                            "dataModelObjectId": toSave.id,
                            "name": attr,
                            "valueType": "STRING"
                        }
                    });
                }
                // save the datamodel object
                datamodelService.saveDatamodelObject(toSave, $stateParams.datamodelId).then(function success(response) {
                    vm.dataModelSavedName.push(response.data.name);
                });
            });
        });
    }

    /**
     * load the data model
     */
    function loadDatamodel() {
        // erase current plot
        vm.nodes.clear();
        vm.edges.clear();

        // reset data persistence state
        objectDeleteList = [];
        objectDeleteNameList = [];
        datamodelService.getDatamodel($stateParams.datamodelId).
        then(function success(data) {
            vm.datamodelTitle = data.name;
            vm.additionalInfo = data.additionalInfo;
        });

        // load datamodel objects
        datamodelService.getDatamodelObjects($stateParams.datamodelId).
        then(function success(data) {

            // process the nodes, gather the raw edges
            var dmo_to_node = {}    // hashmap of dmo id strings -> visjs node ids
            var edges = [];         // array of {to: child_dmo_id, from: parent_dmo_id} edges
            var currId = 1;         // keeps track of current node id
            data.forEach(dmo => {   // iterate and process each object
                // get attributes
                var attributes = dmo.attributeDefinitions.map(attribute => {
                    return attribute.name;
                });

                // add edge if parent exists
                if (dmo.parentId) {
                    edges.push({ to: dmo.id.id, from: dmo.parentId.id});
                }

                var logoFile = dmo.logoFile ? dmo.logoFile : null;
                // create node
                var node = {
                    id:     currId++,
                    label:  dmo.name,
                    group:  dmo.type,
                    datamodelObject: createDatamodelObject(
                        dmo.id.id,
                        dmo.name,
                        dmo.description,
                        dmo.type,
                        attributes,
                        logoFile
                    )
                };

                // store id in hashmap
                dmo_to_node[dmo.id.id] = node.id;
                vm.dataModelSavedName.push(angular.lowercase(dmo.name));

                // add the node to the nodes set
                vm.nodes.add(node);
            });

            // process the raw edges
            currId = 1; // reuse this counter for keeping track of current edge id
            edges.forEach(edge => {
                vm.edges.add({
                    id:     currId++,
                    to:     dmo_to_node[edge.to],
                    from:   dmo_to_node[edge.from]
                });
            });

            plotDatamodel();

        });
    }

    /**
     * plot the datamodel
     */
    function plotDatamodel() {
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

    function fileAdded($file) {
        var reader = new FileReader();
        reader.onload = function(event) {
            $scope.$apply(function() {
                if(event.target.result) {
                    var addedFile = event.target.result;
                    if (addedFile && addedFile.length > 0) {
                        if($file.getExtension() === 'png' || $file.getExtension() === 'jpeg' || $file.getExtension() === 'svg' || $file.getExtension() === 'jpg'){
                            vm.stepperData.logoFile = addedFile;
                            vm.stepperData.logoFileName = $file.name;
                        }
                    }
                }
            });
        };
        reader.readAsDataURL($file.file);
    }

    function clearFile() {
       // $scope.theForm.$setDirty();
        vm.stepperData.logoFile = null;
        vm.stepperData.logoFileName = null;
    }

    // handle the selection of a visjs node
    function onDatamodelObjectSelect(properties) {
        // immediately deselect everything
        network.unselectAll();

        // get the node that was selected
        var nodeId = properties.nodes[0];
        var node = vm.nodes.get(nodeId);
        vm.nodeValue = node;

        let edge = vm.edges.get().filter(e => {
                return e.to === node.id;
          }).pop();
          if (edge) {
                vm.nodeValue.datamodelObject.parent_id = edge.from;
         }


        if(vm.nodeValue.datamodelObject.parent_id) {
            var parentNode = vm.nodes.get(vm.nodeValue.datamodelObject.parent_id);
            vm.nodeValue.datamodelObject.parent = parentNode.datamodelObject.name;

         }

        if (vm.isEdit) {
            vm.showDatamodelObjectStepper(null, node);
        } else {
            $timeout( function(){
               $mdDialog.show({
                    controller: function () { return vm }, // use the current controller (this) as the mdDialog controller
                    controllerAs: 'vm',
                    templateUrl: objectInformation,
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: nodeId
                });
         }  , 0 );

        }
    }
    /**
     * start the mdDialog for adding a datamodel object
     *
     * @param targetEvent - event, if any, that triggered this show
     * @param nodeToEdit - visjs node to edit if a node is being edited, otherwise null for new node
     */
    vm.showDatamodelObjectStepper = function (targetEvent, nodeToEdit) {

        // reset stepper state
        resetStepperState();

        // load datamodel object into stepper data if one is being edited
        if (nodeToEdit) {
            // sanity check
            if(!nodeToEdit.datamodelObject) {
                return;
            }

            // set stepper mode
            vm.stepperMode = "EDIT"

            // process the object into stepper data
            var dmo = nodeToEdit.datamodelObject;
            vm.stepperData.id = dmo.id;
            vm.stepperData.node_id = nodeToEdit.id;
            vm.stepperData.name = dmo.name;
            vm.stepperData.desc = dmo.desc;
            vm.stepperData.type = dmo.type;
            vm.stepperData.attributes = dmo.attributes;
            vm.stepperData.logoFile = dmo.logoFile;
            vm.stepperState = 3; // go straight to review page

            // get the parent ID if it exists
            let edge = vm.edges.get().filter(e => {
                return e.to === nodeToEdit.id;
            }).pop();
            if (edge) {
                vm.stepperData.parent_node_id = edge.from;
            }

        } else {
            // create a new node id for a new object creation stepper
            vm.stepperData.node_id = vm.nodes.length + 1;
        }

        // show the mdDialog
        $mdDialog.show({
            controller: function () { return vm }, // use the current controller (this) as the mdDialog controller
            controllerAs: 'vm',
            templateUrl: objectStepper,
            parent: angular.element($document[0].body),
            fullscreen: true,
            targetEvent: targetEvent
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

    // process new and edited objects from a stepper submit click
    vm.onStepperSubmit = function() {
        // create the data model object to be submitted
        var dmo = createDatamodelObject(
            vm.stepperData.id,
            vm.stepperData.name.trim(),
            vm.stepperData.desc,
            vm.stepperData.type,
            vm.stepperData.attributes,
            vm.stepperData.logoFile
        );

        // get the nodeId and the node (if it exists)
        var nodeId = vm.stepperData.node_id;
        var node = vm.nodes.get(nodeId); // this is null if no node exists with this id
        if(node == null) {
            if(vm.dataModelName.indexOf(angular.lowercase(vm.stepperData.name.trim())) !== -1) {
                vm.stepperState = 0;    // keeps track of the current stepper step (0-3)
                vm.stepperMode = "";    // either CREATE or EDIT. Usefull for hiding/showing the delete option
                toast.showError($translate.instant('Datamodel object already present of same name.'));
                return false;
             } else {
                vm.dataModelName.push(angular.lowercase(vm.stepperData.name.trim()));
             }

            if(vm.dataModelSavedName.length > 0 ) {
                if(vm.dataModelSavedName.indexOf(angular.lowercase(vm.stepperData.name.trim())) !== -1) {
                    vm.stepperState = 0;    // keeps track of the current stepper step (0-3)
                    vm.stepperMode = "";    // either CREATE or EDIT. Usefull for hiding/showing the delete option
                    toast.showError($translate.instant('Datamodel object already present of same name.'));
                    return false;
                }
            }
        }

        if(node !== null) {
          if(angular.lowercase(node.label) !== angular.lowercase(vm.stepperData.name.trim())) {
            if(vm.dataModelSavedName.indexOf(angular.lowercase(vm.stepperData.name.trim())) !== -1) {
                vm.stepperState = 0;    // keeps track of the current stepper step (0-3)
                vm.stepperMode = "";    // either CREATE or EDIT. Usefull for hiding/showing the delete option
                toast.showError($translate.instant('Datamodel object already present of same name.'));
                return false;
             } else {
                vm.dataModelName.push(angular.lowercase(vm.stepperData.name.trim()));
             }

            }
        }

        if (node) { // handle an existing node
            // update the node
            node.datamodelObject = dmo
            node.label = vm.stepperData.name;
            node.group = vm.stepperData.type;

            // merge the node changes back into nodes
            vm.nodes.update(node);

        } else { // handle a new node
            // add a new node into the nodes list
            vm.nodes.add({
                id: nodeId,
                label: dmo.name,
                group: dmo.type,
                datamodelObject: dmo
            });
        }

        // update the parent relationship
        var parent_nodeId = vm.stepperData.parent_node_id;
        var edge = vm.edges.get().filter(function (edge) { // get the existing edge if it exists, otherwise get empty object
            return edge.to === nodeId;
        }).pop() || {};
        if (parent_nodeId) { // handle a parent that exists
            // update endpoints
            edge.to = nodeId;
            edge.from = parent_nodeId;

            // add edge to edges
            if (edge.id) {  // update existing edge
                vm.edges.update(edge);
            } else {        // create new edge
                edge.id = vm.edges.length + 1;
                vm.edges.add(edge);
            }
        } else { // handle a parent that does not exist
            if (edge.id) { // delete an existing edge
                vm.edges.remove(edge.id);
            }
        }

        // plot the data
        plotDatamodel();

        // hide the stepper and reset its state
        vm.cancel();
    };

    // delete the object and reload the datamodel
    vm.onStepperDelete = function () {
        // confirm delete
        var confirm = $mdDialog.confirm()
            .title('Delete Object')
            .htmlContent("Are you sure you want to delete this object?")
            .cancel("Cancel")
            .ok("Submit");
        $mdDialog.show(confirm).then(function () {

            // remove the node
            var nodeId = vm.stepperData.node_id;
            vm.nodes.remove(nodeId);

            // remove any edge utilizing this node
            var old_edges = vm.edges.get().filter(edge => {
                return edge.to === nodeId || edge.from === nodeId;
            });
            vm.edges.remove(old_edges);

            // queue the object up for deletion if it has an ID
            if (vm.stepperData.id) {
                objectDeleteList.push(vm.stepperData.id);
                objectDeleteNameList.push(vm.stepperData.name);
            }

            // close the dialog
            vm.cancel();
        });
    };

    // add a datamodel object attribute to the stepper's current data
    vm.addDatamodelObjectAttribute = function () {
        // add the attribute if it exists
        if (vm.stepperData.currentAttribute) {
            vm.stepperData.attributes.push(vm.stepperData.currentAttribute);
        }
        // reset the current attribute
        vm.stepperData.currentAttribute = "";
    };

    // update the datamodel and exit edit mode
    vm.acceptDatamodelEdit = function () {

       vm.dataModelName = vm.dataModelName.filter(val => !objectDeleteNameList.includes(val));
       vm.dataModelSavedName = vm.dataModelSavedName.filter(val => !objectDeleteNameList.includes(val));

        objectDeleteList.forEach(id_to_delete => {
            // delete the object by ID
            datamodelService.deleteDatamodelObject(
                id_to_delete
            );
        });

        saveDatamodel();
        vm.toggleDMEditMode();
    };

    vm.rejectDatamodelEdit = function() {
        loadDatamodel();
    };

    vm.updateAttribute =  function (attributeName, index){
        $scope.editing = false;
        vm.stepperData.attributes[index] = attributeName;
    }
    vm.deleteAttribute =  function (index){
        vm.stepperData.attributes.splice(index, 1);
    }
}
