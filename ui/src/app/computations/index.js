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
/* eslint-disable import/no-unresolved, import/default */
import uiRouter from 'angular-ui-router';
import tempusApiComputation from '../api/computation.service';
import tempusApiComputationJob from '../api/computation-job.service';
import ComputationController from './computation.controller';
import AddComputationController from './add-computation.controller';
import ComputationDirective from './computation.directive';
import ComputationJobDirective from './computation-job.directive';
import ComputationRoutes from './computation.routes';
import ComputationFormSparkDirective from './computation-forms/computation-form-spark.directive';
import ComputationFormKubelessDirective from './computation-forms/computation-form-kubeless.directive';
/* eslint-enable import/no-unresolved, import/default */

export default angular.module('tempus.computation', [
    uiRouter,
    tempusApiComputation,
    tempusApiComputationJob
])
    .config(ComputationRoutes)
    .controller('ComputationController', ComputationController)
    .controller('AddComputationController', AddComputationController)
    .directive('tbComputation',ComputationDirective)
    .directive('tbComputationFormSpark',ComputationFormSparkDirective)
    .directive('tbComputationFormKubeless',ComputationFormKubelessDirective)
    .directive('tbComputationJob',ComputationJobDirective)
    .name;
