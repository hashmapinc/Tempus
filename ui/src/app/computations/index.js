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
/* eslint-disable import/no-unresolved, import/default */
import uiRouter from 'angular-ui-router';
import TempusApiComputation from '../api/computation.service';
import TempusApiComputationJob from '../api/computation-job.service';
import ComputationController from './computation.controller';
//import ComputationTestController from './computationsTest.controller';
import ComputationJobController from './computation-job.controller';
//import ComputationUploadDirective from './computation-upload.directive';
import ComputationDirective from './computation.directive';
import ComputationJobDirective from './computation-job.directive';
import ComputationRoutes from './computation.routes';
/* eslint-enable import/no-unresolved, import/default */

export default angular.module('tempus.computation', [
    uiRouter,
    TempusApiComputation,
    TempusApiComputationJob
])
    .config(ComputationRoutes)
    .controller('ComputationController', ComputationController)
    //.controller('ComputationTestController', ComputationTestController)
    .controller('ComputationJobController', ComputationJobController)
    //.directive('fileModel', ComputationUploadDirective)
    .directive('tbComputation',ComputationDirective)
    .directive('tbComputationJob',ComputationJobDirective)
    .name;