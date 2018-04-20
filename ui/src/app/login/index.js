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
import './login.scss';

import uiRouter from 'angular-ui-router';
import thingsboardApiLogin from '../api/login.service';
import thingsboardApiUser from '../api/user.service';
import thingsboardToast from '../services/toast';

import LoginRoutes from './login.routes';
import LoginController from './login.controller';
import ResetPasswordRequestController from './reset-password-request.controller';
import ResetPasswordController from './reset-password.controller';
import CreatePasswordController from './create-password.controller';

export default angular.module('thingsboard.login', [
    uiRouter,
    thingsboardApiLogin,
    thingsboardApiUser,
    thingsboardToast
])
    .config(LoginRoutes)
    .controller('LoginController', LoginController)
    .controller('ResetPasswordRequestController', ResetPasswordRequestController)
    .controller('ResetPasswordController', ResetPasswordController)
    .controller('CreatePasswordController', CreatePasswordController)
    .name;
