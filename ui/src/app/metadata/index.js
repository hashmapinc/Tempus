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
import './metadata.scss';
import uiRouter from 'angular-ui-router';
import tempusApiMetadata from '../api/metadata.service';
import MetadataController from './metadata.controller';
import AddMetadataController from './add-metadata.controller';
import MetadataRoutes from './metadata.routes';
import MetadataSourceDirective from './metadata-source.directive';
import MetadataSinkDirective from './metadata-sink.directive';
import MetadataConfigDirective from './metadataConfig.directive';

import MetadataQueryDirective from './metadata-query.directive';
import QueryDialogController from './query-dialog.controller';


export default angular.module('tempus.metadata', [
    uiRouter,
    tempusApiMetadata
])
    .config(MetadataRoutes)
    .controller('MetadataController', MetadataController)
    .controller('AddMetadataController', AddMetadataController)
    .controller('QueryDialogController', QueryDialogController)
    .directive('tbMetadataSource', MetadataSourceDirective)
    .directive('tbMetadataSink', MetadataSinkDirective)
    .directive('tbMetadataConfig', MetadataConfigDirective)
    .directive('tbMetadataQuery', MetadataQueryDirective)
    .name;