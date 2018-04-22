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
import './widget-editor.scss';

import 'angular-hotkeys';
import 'angular-ui-ace';

import uiRouter from 'angular-ui-router';
import tempusApiUser from '../api/user.service';
import tempusApiWidget from '../api/widget.service';
import tempusTypes from '../common/types.constant';
import tempusToast from '../services/toast';
import tempusConfirmOnExit from '../components/confirm-on-exit.directive';
import tempusDashboard from '../components/dashboard.directive';
import tempusExpandFullscreen from '../components/expand-fullscreen.directive';
import tempusCircularProgress from '../components/circular-progress.directive';

import WidgetLibraryRoutes from './widget-library.routes';
import WidgetLibraryController from './widget-library.controller';
import SelectWidgetTypeController from './select-widget-type.controller';
import WidgetEditorController from './widget-editor.controller';
import WidgetsBundleController from './widgets-bundle.controller';
import WidgetsBundleDirective from './widgets-bundle.directive';
import SaveWidgetTypeAsController from './save-widget-type-as.controller';

export default angular.module('tempus.widget-library', [
    uiRouter,
    tempusApiWidget,
    tempusApiUser,
    tempusTypes,
    tempusToast,
    tempusConfirmOnExit,
    tempusDashboard,
    tempusExpandFullscreen,
    tempusCircularProgress,
    'cfp.hotkeys',
    'ui.ace'
])
    .config(WidgetLibraryRoutes)
    .controller('WidgetLibraryController', WidgetLibraryController)
    .controller('SelectWidgetTypeController', SelectWidgetTypeController)
    .controller('WidgetEditorController', WidgetEditorController)
    .controller('WidgetsBundleController', WidgetsBundleController)
    .controller('SaveWidgetTypeAsController', SaveWidgetTypeAsController)
    .directive('tbWidgetsBundle', WidgetsBundleDirective)
    .name;
