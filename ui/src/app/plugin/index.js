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
import uiRouter from 'angular-ui-router';
import tempusGrid from '../components/grid.directive';
import tempusJsonForm from '../components/json-form.directive';
import tempusApiPlugin from '../api/plugin.service';
import tempusApiComponentDescriptor from '../api/component-descriptor.service';

import PluginRoutes from './plugin.routes';
import PluginController from './plugin.controller';
import PluginDirective from './plugin.directive';

export default angular.module('tempus.plugin', [
    uiRouter,
    tempusGrid,
    tempusJsonForm,
    tempusApiPlugin,
    tempusApiComponentDescriptor
])
    .config(PluginRoutes)
    .controller('PluginController', PluginController)
    .directive('tbPlugin', PluginDirective)
    .name;
