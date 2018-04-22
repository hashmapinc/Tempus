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
import '../dashboard/dashboard.scss';

import uiRouter from 'angular-ui-router';
import tempusGrid from '../components/grid.directive';
import tempusApiUser from '../api/user.service';
import tempusApiDevice from '../api/device.service';
import tempusApiCustomer from '../api/customer.service';
import tempusApiApplication from '../api/application.service';

import tempusApiWidget from '../api/widget.service';
import tempusApiDashboard from '../api/dashboard.service';
import tempusDetailsSidenav from '../components/details-sidenav.directive';
import tempusWidgetConfig from '../components/widget/widget-config.directive';
import tempusDashboardSelect from '../components/dashboard-select.directive';
import tempusRelatedEntityAutocomplete from '../components/related-entity-autocomplete.directive';
import tempusDashboard from '../components/dashboard.directive';
import tempusExpandFullscreen from '../components/expand-fullscreen.directive';
import tempusWidgetsBundleSelect from '../components/widgets-bundle-select.directive';
import tempusSocialsharePanel from '../components/socialshare-panel.directive';
import tempusTypes from '../common/types.constant';
import tempusItemBuffer from '../services/item-buffer.service';
import tempusImportExport from '../import-export';
import dashboardLayouts from '../dashboard/layouts';
import dashboardStates from '../dashboard/states';

import ApplicationRoutes from './application.routes';

import {ApplicationController} from './application.controller';
import ApplicationDirective from './application.directive';

import tempusPluginSelect from '../components/plugin-select.directive';
import tempusComponent from '../component';
import tempusApiRule from '../api/rule.service';
import tempusApiPlugin from '../api/plugin.service';
import tempusApiComponentDescriptor from '../api/component-descriptor.service';

import DashboardController from '../dashboard/dashboard.controller';
import DashboardSettingsController from '../dashboard/dashboard-settings.controller';
import AddWidgetController from '../dashboard/add-widget.controller';

import TempusApiComputation from '../api/computation.service';
import TempusApiComputationJob from '../api/computation-job.service';
//import ComputationJobController from '../computations/computation-job.controller';

//import DashboardDirective from '../dashboard/dashboard.directive';
//import EditWidgetDirective from '../dashboard/edit-widget.directive';
//import DashboardToolbar from '../dashboard/dashboard-toolbar.directive';

//import RuleRoutes from '../rule/rule.routes';
//import RuleController from '../rule/rule.controller';
//import RuleDirective from '../rule/rule.directive';

export default angular.module('tempus.application', [
    uiRouter,
    tempusGrid,
    tempusApiUser,
    tempusApiDevice,
    tempusApiApplication,
    tempusApiCustomer,
    tempusPluginSelect,
    tempusComponent,
    tempusApiRule,
    tempusApiPlugin,
    tempusApiComponentDescriptor,
    TempusApiComputation,
    TempusApiComputationJob,
    tempusTypes,
    tempusItemBuffer,
    tempusImportExport,
    tempusApiWidget,
    tempusApiDashboard,
    tempusDetailsSidenav,
    tempusWidgetConfig,
    tempusDashboardSelect,
    tempusRelatedEntityAutocomplete,
    tempusDashboard,
    tempusExpandFullscreen,
    tempusWidgetsBundleSelect,
    tempusSocialsharePanel,
    dashboardLayouts,
    dashboardStates
])
    .config(ApplicationRoutes)
    .controller('ApplicationController', ApplicationController)
    .controller('DashboardController', DashboardController)
    .controller('DashboardSettingsController', DashboardSettingsController)
    .controller('AddWidgetController', AddWidgetController)
    //.controller('ComputationJobController', ComputationJobController)
    .directive('tbApplication', ApplicationDirective)
  //  .directive('tbRule', RuleDirective)
  //  .directive('tbDashboardDetails', DashboardDirective)
   // .directive('tbEditWidget', EditWidgetDirective)
   // .directive('tbDashboardToolbar', DashboardToolbar)
    .name;
