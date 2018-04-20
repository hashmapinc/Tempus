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
import ManageDashboardStatesController from './manage-dashboard-states.controller';
import DashboardStateDialogController from './dashboard-state-dialog.controller';
import SelectTargetStateController from './select-target-state.controller';
import StatesComponentDirective from './states-component.directive';
import StatesControllerService from './states-controller.service';

export default angular.module('thingsboard.dashboard.states', [])
    .controller('ManageDashboardStatesController', ManageDashboardStatesController)
    .controller('DashboardStateDialogController', DashboardStateDialogController)
    .controller('SelectTargetStateController', SelectTargetStateController)
    .directive('tbStatesComponent', StatesComponentDirective)
    .factory('statesControllerService', StatesControllerService)
    .name;
