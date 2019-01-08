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

import uiRouter from 'angular-ui-router';
import tempusApiUserGroup from '../api/user-group.service';
import tempusApiUser from '../api/user.service';
import tempusApiCustomer from '../api/customer.service';
import tempusGrid from '../components/grid.directive';

import TemplateeditorRoutes from './templateeditor.routes';
import {TemplateEditorController} from './template_editor.controller';
import AddTemplateModelController from './add-template.controller';
import EditorDirective from './editor.directive';

//import AddTemplateEditorController from './add_template_editor.controller';


export default angular.module('tempus.gateway', [
    uiRouter,
    tempusApiUserGroup,
    tempusApiUser,
    tempusApiCustomer,
    tempusGrid
])
    .config(TemplateeditorRoutes)
    .controller('TemplateEditorController',TemplateEditorController)
    .controller('AddTemplateModelController', AddTemplateModelController)
    .directive('tbEditor', EditorDirective)
    //.config('AddTemplateEditorController',AddTemplateEditorController)
    .name;
