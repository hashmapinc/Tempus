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
import ngMaterial from 'angular-material';
import ngMessages from 'angular-messages';
import tempusApiUser from '../api/user.service';
import tempusApiLogin from '../api/login.service';
import tempusConfirmOnExit from '../components/confirm-on-exit.directive';

import ProfileRoutes from './profile.routes';
import ProfileController from './profile.controller';
import ChangePasswordController from './change-password.controller';

export default angular.module('tempus.profile', [
    uiRouter,
    ngMaterial,
    ngMessages,
    tempusApiUser,
    tempusApiLogin,
    tempusConfirmOnExit
])
    .config(ProfileRoutes)
    .controller('ProfileController', ProfileController)
    .controller('ChangePasswordController', ChangePasswordController)
    .name;
