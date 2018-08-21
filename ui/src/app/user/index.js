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
import tempusGrid from '../components/grid.directive';
import tempusApiUser from '../api/user.service';
import tempusApiUserGroup from '../api/usergroup.service';
import tempusToast from '../services/toast';

import UserRoutes from './user.routes';
import UserController from './user.controller';
import UserActionListController from './useractionlist.controller';
import UserGroupController from './usergroup.controller';
import AddGroupModelController from './add-group.controller'
import AddUserController from './add-user.controller';
import ActivationLinkDialogController from './activation-link.controller';
import UserDirective from './user.directive';
import UsergroupDirective from './usergroup.directive';
import AddUsersToGroupController from './add-users-to-group.controller';
import AddGroupsToUserController from './add-groups-to-user.controller';
import DeleteUsersToGroupController from './delete-users-to-group.controller';
import DeleteGroupsToUserController from './delete-groups-to-user.controller'

export default angular.module('tempus.user', [
    uiRouter,
    tempusGrid,
    tempusApiUser,
    tempusApiUserGroup,
    tempusToast
])
    .config(UserRoutes)
    .controller('UserController', UserController)
    .controller('UserActionListController', UserActionListController)
    .controller('AddUserController', AddUserController)
    .controller('UserGroupController', UserGroupController)
    .controller('AddGroupModelController', AddGroupModelController)
    .controller('ActivationLinkDialogController', ActivationLinkDialogController)
    .controller('AddUsersToGroupController', AddUsersToGroupController)
    .controller('DeleteUsersToGroupController', DeleteUsersToGroupController)
    .controller('AddGroupsToUserController', AddGroupsToUserController)
    .controller('DeleteGroupsToUserController', DeleteGroupsToUserController)
    .directive('tbUser', UserDirective)
    .directive('tbUsergroup', UsergroupDirective)
    .name;
