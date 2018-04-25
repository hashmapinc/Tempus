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
import './dashboard.scss';

import uiRouter from 'angular-ui-router';

import tempusGrid from '../components/grid.directive';
import tempusApiWidget from '../api/widget.service';
import tempusApiUser from '../api/user.service';
import tempusApiDashboard from '../api/dashboard.service';
import tempusApiCustomer from '../api/customer.service';
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
import dashboardLayouts from './layouts';
import dashboardStates from './states';


import DashboardRoutes from './dashboard.routes';
import {DashboardsController, DashboardCardController, MakeDashboardPublicDialogController} from './dashboards.controller';
import DashboardController from './dashboard.controller';
import DashboardSettingsController from './dashboard-settings.controller';
import AddDashboardsToCustomerController from './add-dashboards-to-customer.controller';
import ManageAssignedCustomersController from './manage-assigned-customers.controller';
import AddWidgetController from './add-widget.controller';
import DashboardDirective from './dashboard.directive';
import EditWidgetDirective from './edit-widget.directive';
import DashboardToolbar from './dashboard-toolbar.directive';

export default angular.module('tempus.dashboard', [
    uiRouter,
    tempusTypes,
    tempusItemBuffer,
    tempusImportExport,
    tempusGrid,
    tempusApiWidget,
    tempusApiUser,
    tempusApiDashboard,
    tempusApiCustomer,
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
    .config(DashboardRoutes)
    .controller('DashboardsController', DashboardsController)
    .controller('DashboardCardController', DashboardCardController)
    .controller('MakeDashboardPublicDialogController', MakeDashboardPublicDialogController)
    .controller('DashboardController', DashboardController)
    .controller('DashboardSettingsController', DashboardSettingsController)
    .controller('AddDashboardsToCustomerController', AddDashboardsToCustomerController)
    .controller('ManageAssignedCustomersController', ManageAssignedCustomersController)
    .controller('AddWidgetController', AddWidgetController)
    .directive('tbDashboardDetails', DashboardDirective)
    .directive('tbEditWidget', EditWidgetDirective)
    .directive('tbDashboardToolbar', DashboardToolbar)
    .name;