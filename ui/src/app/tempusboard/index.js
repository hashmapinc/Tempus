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
import tempusApiDashboard from '../api/dashboard.service';
import tempusApiWidget from '../api/widget.service';
import tempusWidgetConfig from '../components/widget/widget-config.directive';
import dashboardStates from '../dashboard/states';


import TempusboardRoutes from './tempusboard.routes';
import {TempusboardController} from './tempusboard.controller';

export default angular.module('tempus.tempusboard', [
    uiRouter,
    tempusGrid,
    tempusApiUser,
    tempusApiDevice,
    tempusApiCustomer,
    tempusApiDashboard,
    tempusApiWidget,
    tempusWidgetConfig,
    dashboardStates
])
    .config(TempusboardRoutes)
    .controller('TempusboardController', TempusboardController)
    .name;
