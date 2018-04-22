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
import './ie.support';

import 'event-source-polyfill';

import angular from 'angular';
import ngMaterial from 'angular-material';
import ngMdIcons from 'angular-material-icons';
import ngCookies from 'angular-cookies';
import angularSocialshare from 'angular-socialshare';
import 'angular-translate';
import 'angular-translate-loader-static-files';
import 'angular-translate-storage-local';
import 'angular-translate-storage-cookie';
import 'angular-translate-handler-log';
import 'angular-translate-interpolation-messageformat';
import 'md-color-picker';
import mdPickers from 'mdPickers';
import ngSanitize from 'angular-sanitize';
import vAccordion from 'v-accordion';
import ngAnimate from 'angular-animate';
import 'angular-websocket';
import uiRouter from 'angular-ui-router';
import angularJwt from 'angular-jwt';
import 'angular-drag-and-drop-lists';
import mdDataTable from 'angular-material-data-table';
import 'angular-material-expansion-panel';
import ngTouch from 'angular-touch';
import 'angular-carousel';
import 'clipboard';
import 'ngclipboard';
import 'react';
import 'react-dom';
import 'material-ui';
import 'react-schema-form';
import react from 'ngreact';
import '@flowjs/ng-flow/dist/ng-flow-standalone.min';

import tempusLocales from './locale/locale.constant';
import tempusLogin from './login';
import tempusDialogs from './components/datakey-config-dialog.controller';
import tempusMenu from './services/menu.service';
import tempusRaf from './common/raf.provider';
import tempusUtils from './common/utils.service';
import tempusDashboardUtils from './common/dashboard-utils.service';
import tempusTypes from './common/types.constant';
import tempusApiTime from './api/time.service';
import tempusApiDepth from './api/depth.service';
import tempusKeyboardShortcut from './components/keyboard-shortcut.filter';
import tempusHelp from './help/help.directive';
import tempusToast from './services/toast';
import tempusClipboard from './services/clipboard.service';
import tempusHome from './layout';
import tempusApiLogin from './api/login.service';
import tempusApiDevice from './api/device.service';
import tempusApiUser from './api/user.service';
import tempusApiEntityRelation from './api/entity-relation.service';
import tempusApiAsset from './api/asset.service';
import tempusApiAttribute from './api/attribute.service';
import tempusApiEntity from './api/entity.service';
import tempusApiAlarm from './api/alarm.service';
import tempusApiUiConfiguration from './api/ui-configuration.service';
import tempusApiComputation from './api/computation.service';
import tempusApiComputationJob from './api/computation-job.service';
import tempusTempusboard from './tempusboard';
import tempusApplications from './applications';
import tempusApiAuditLog from './api/audit-log.service';

import 'typeface-roboto';
import 'font-awesome/css/font-awesome.min.css';
import 'angular-material/angular-material.min.css';
import 'angular-material-icons/angular-material-icons.css';
import 'angular-gridster/dist/angular-gridster.min.css';
import 'v-accordion/dist/v-accordion.min.css'
import 'md-color-picker/dist/mdColorPicker.min.css';
import 'mdPickers/dist/mdPickers.min.css';
import 'angular-hotkeys/build/hotkeys.min.css';
import 'angular-carousel/dist/angular-carousel.min.css';
import 'angular-material-expansion-panel/dist/md-expansion-panel.min.css';
import '../scss/main.scss';

import AppConfig from './app.config';
import GlobalInterceptor from './global-interceptor.service';
import AppRun from './app.run';

angular.module('tempus', [
    ngMaterial,
    ngMdIcons,
    ngCookies,
    angularSocialshare,
    'pascalprecht.translate',
    'mdColorPicker',
    mdPickers,
    ngSanitize,
    vAccordion,
    ngAnimate,
    'ngWebSocket',
    angularJwt,
    'dndLists',
    mdDataTable,
    'material.components.expansionPanels',
    ngTouch,
    'angular-carousel',
    'ngclipboard',
    react.name,
    'flow',
    tempusLocales,
    tempusLogin,
    tempusDialogs,
    tempusMenu,
    tempusRaf,
    tempusUtils,
    tempusDashboardUtils,
    tempusTypes,
    tempusApiTime,
    tempusApiDepth,
    tempusKeyboardShortcut,
    tempusHelp,
    tempusToast,
    tempusClipboard,
    tempusHome,
    tempusApiLogin,
    tempusApiDevice,
    tempusApiUser,
    tempusApiEntityRelation,
    tempusApiAsset,
    tempusApiAttribute,
    tempusApiEntity,
    tempusApiAlarm,
    tempusApiUiConfiguration,
    tempusApiComputation,
    tempusApiComputationJob,
    tempusTempusboard,
    tempusApplications,
    tempusApiAuditLog,
    uiRouter])
    .config(AppConfig)
    .factory('globalInterceptor', GlobalInterceptor)
    .run(AppRun);
