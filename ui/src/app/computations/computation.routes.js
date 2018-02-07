/*
 * Copyright © 2016-2017 The Thingsboard Authors
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

//import computationsTemplate from './computations.tpl.html';
import computationsTemplateTest from './computationsTest.tpl.html';
import computationJobTemplate from './computation-job.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ComputationRoutes($stateProvider) {
    $stateProvider
        .state('home.computations', {
            url: '/computations',
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: computationsTemplateTest,
                    controller: 'ComputationTestController',
                    controllerAs: 'vm'
                }
            },
            data: {
                pageTitle: 'computation.computations'
            },
            ncyBreadcrumb: {
                label: '{"icon": "dashboards", "label": "computation.computations", "link": "/static/svg/computationslightgray.svg"}'
            }
        })
        .state('home.computationJob', {
            url: '/computationJob/:computationId',
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: computationJobTemplate,
                    controller: 'ComputationJobController',
                    controllerAs: 'vm'
                }
            },
            data: {
                pageTitle: 'computationJob.computationJobs'
            },
            ncyBreadcrumb: {
                label: '{"icon": "dashboards", "label": "computationJob.computationJobs", "link": "/static/svg/computationslightgray.svg"}'
            }
        });

}
