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
import './home.scss';

import uiRouter from 'angular-ui-router';
import ngSanitize from 'angular-sanitize';
import FBAngular from 'angular-fullscreen';
import 'angular-breadcrumb';

import tempusMenu from '../services/menu.service';
import tempusApiDevice from '../api/device.service';
import tempusApiLogin from '../api/login.service';
import tempusApiUser from '../api/user.service';

import tempusNoAnimate from '../components/no-animate.directive';
import tempusOnFinishRender from '../components/finish-render.directive';
import tempusSideMenu from '../components/side-menu.directive';
import tempusDashboardAutocomplete from '../components/dashboard-autocomplete.directive';

import tempusUserMenu from './user-menu.directive';

import tempusEntity from '../entity';
import tempusEvent from '../event';
import tempusAlarm from '../alarm';
import tempusAuditLog from '../audit';
import tempusExtension from '../extension';
import tempusTenant from '../tenant';
import tempusCustomer from '../customer';
import tempusUser from '../user';
import tempusHomeLinks from '../home';
import tempusAdmin from '../admin';
import tempusProfile from '../profile';
import tempusAsset from '../asset';
import tempusDevice from '../device';
import tempusWidgetLibrary from '../widget';
import tempusDashboard from '../dashboard';
import tempusPlugin from '../plugin';
import tempusRule from '../rule';
import tempusComputation from '../computations';

import tempusJsonForm from '../jsonform';

import HomeRoutes from './home.routes';
import HomeController from './home.controller';
import BreadcrumbLabel from './breadcrumb-label.filter';
import BreadcrumbIcon from './breadcrumb-icon.filter';
import BreadcrumbLink from './breadcrumb-link.filter';

export default angular.module('tempus.home', [
    uiRouter,
    ngSanitize,
    FBAngular.name,
    'ncy-angular-breadcrumb',
    tempusMenu,
    tempusHomeLinks,
    tempusUserMenu,
    tempusEntity,
    tempusEvent,
    tempusAlarm,
    tempusAuditLog,
    tempusExtension,
    tempusTenant,
    tempusCustomer,
    tempusUser,
    tempusAdmin,
    tempusProfile,
    tempusAsset,
    tempusDevice,
    tempusWidgetLibrary,
    tempusDashboard,
    tempusPlugin,
    tempusRule,
    tempusJsonForm,
    tempusApiDevice,
    tempusApiLogin,
    tempusApiUser,
    tempusNoAnimate,
    tempusOnFinishRender,
    tempusSideMenu,
    tempusDashboardAutocomplete,
    tempusComputation
])
    .config(HomeRoutes)
    .controller('HomeController', HomeController)
    .filter('breadcrumbLabel', BreadcrumbLabel)
    .filter('breadcrumbIcon', BreadcrumbIcon)
    .filter('breadcrumbLink', BreadcrumbLink)
    .name;
