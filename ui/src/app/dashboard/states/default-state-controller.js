/*
 * Copyright © 2016-2018 Hashmap, Inc
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
import './default-state-controller.scss';

/*@ngInject*/
export default function DefaultStateController($scope, $timeout, $location, $state,
                                               $stateParams, utils, types, dashboardUtils, preservedState) {

    var vm = this;

    vm.inited = false;

    vm.skipStateChange = false;

    vm.openState = openState;
    vm.updateState = updateState;
    vm.resetState = resetState;
    vm.getStateObject = getStateObject;
    vm.navigatePrevState = navigatePrevState;
    vm.getStateId = getStateId;
    vm.getStateIndex = getStateIndex;
    vm.getStateIdAtIndex = getStateIdAtIndex;
    vm.getStateParams = getStateParams;
    vm.getStateParamsByStateId = getStateParamsByStateId;
    vm.getEntityId = getEntityId;

    vm.getStateName = getStateName;

    vm.displayStateSelection = displayStateSelection;

    function openState(id, params, openRightLayout) {
        if (vm.states && vm.states[id]) {
            if (!params) {
                params = {};
            }
            var newState = {
                id: id,
                params: params
            }
            //append new state
            vm.stateObject[0] = newState;
            gotoState(vm.stateObject[0].id, true, openRightLayout);
            vm.skipStateChange = true;
        }
    }

    function updateState(id, params, openRightLayout) {
        if (!id) {
            id = getStateId();
        }
        if (vm.states && vm.states[id]) {
            if (!params) {
                params = {};
            }
            var newState = {
                id: id,
                params: params
            }
            //replace with new state
            vm.stateObject[0] = newState;
            gotoState(vm.stateObject[0].id, true, openRightLayout);
            vm.skipStateChange = true;
        }
    }

    function resetState() {
        var rootStateId = dashboardUtils.getRootStateId(vm.states);
        vm.stateObject = [ { id: rootStateId, params: {} } ];
        gotoState(rootStateId, true);
    }

    function getStateObject() {
        return vm.stateObject;
    }

    function navigatePrevState(index) {
        if (index < vm.stateObject.length-1) {
            vm.stateObject.splice(index+1, vm.stateObject.length-index-1);
            gotoState(vm.stateObject[vm.stateObject.length-1].id, true);
            vm.skipStateChange = true;
        }
    }

    function getStateId() {
        if (vm.stateObject && vm.stateObject.length) {
            return vm.stateObject[vm.stateObject.length-1].id;
        } else {
            return '';
        }
    }

    function getStateIndex() {
        if (vm.stateObject && vm.stateObject.length) {
            return vm.stateObject.length-1;
        } else {
            return -1;
        }
    }

    function getStateIdAtIndex(index) {
        if (vm.stateObject && vm.stateObject[index]) {
            return vm.stateObject[index].id;
        } else {
            return '';
        }
    }

    function getStateParams() {
        if (vm.stateObject && vm.stateObject.length) {
            return vm.stateObject[vm.stateObject.length - 1].params;
        } else {
            return {};
        }
    }

    function getStateParamsByStateId(stateId) {
        var stateObj = getStateObjById(stateId);
        if (stateObj) {
            return stateObj.params;
        } else {
            return null;
        }
    }

    function getEntityId() {
        return null;
    }

    function getStateObjById(id) {
        for (var i=0; i < vm.stateObject.length; i++) {
            if (vm.stateObject[i].id === id) {
                return vm.stateObject[i];
            }
        }
        return null;
    }

    function getStateName(id, state) {
        return utils.customTranslation(state.name, id);
    }

    function parseState(stateBase64) {
        var result;
        if (stateBase64) {
            try {
                result = utils.base64toObj(stateBase64);
            } catch (e) {
                result = [ { id: null, params: {} } ];
            }
        }
        if (!result) {
            result = [];
        }
        if (!result.length) {
            result[0] = { id: null, params: {} }
        } else if (result.length > 1) {
            var newResult = [];
            newResult.push(result[result.length-1]);
            result = newResult;
        }

        var rootStateId = dashboardUtils.getRootStateId(vm.states);
        if (!result[0].id) {
            result[0].id = rootStateId;
        }
        if (!vm.states[result[0].id]) {
            result[0].id = rootStateId;
        }
        var i = result.length;
        while (i--) {
            if (!result[i].id || !vm.states[result[i].id]) {
                result.splice(i, 1);
            }
        }
        return result;
    }

    $scope.$watch('vm.states', function() {
        if (vm.states) {
            if (!vm.inited) {
                vm.inited = true;
                init();
            }
        }
    });

    function displayStateSelection() {
        return vm.states && Object.keys(vm.states).length > 1;
    }

    function init() {
        if (preservedState) {
            vm.stateObject = preservedState;
            gotoState(vm.stateObject[0].id, true);
        } else {
            var initialState = $stateParams.state;
            vm.stateObject = parseState(initialState);
            gotoState(vm.stateObject[0].id, false);
        }

        $timeout(() => {
            $scope.$watchCollection(function () {
                return $state.params;
            }, function () {
                var currentState = $state.params.state;
                vm.stateObject = parseState(currentState);
            });

            $scope.$watch('vm.dashboardCtrl.dashboardCtx.state', function () {
                if (vm.stateObject[0].id !== vm.dashboardCtrl.dashboardCtx.state) {
                    vm.stateObject[0].id = vm.dashboardCtrl.dashboardCtx.state;
                    updateLocation();
                    vm.skipStateChange = true;
                }
            });
            $scope.$watch('vm.stateObject', function(newVal, prevVal) {
                if (!angular.equals(newVal, prevVal) && newVal) {
                    if (vm.skipStateChange) {
                        vm.skipStateChange = false;
                    } else {
                        gotoState(vm.stateObject[0].id, true);
                    }
                }
            }, true);
        });
    }

    function gotoState(stateId, update, openRightLayout) {
        if (vm.dashboardCtrl.dashboardCtx.state != stateId) {
            vm.dashboardCtrl.openDashboardState(stateId, openRightLayout);
            if (update) {
                updateLocation();
            }
        }
    }

    function updateLocation() {
        if (vm.stateObject[0].id) {
            $location.search('state', utils.objToBase64(vm.stateObject));
        }
    }
}
