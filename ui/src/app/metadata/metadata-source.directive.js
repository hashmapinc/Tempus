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

import metadataSourceFieldsetTemplate from './metadata-source-fieldset.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function MetadataSourceDirective($compile, $templateCache, $translate, types, metadataService, toast) {
    var linker = function(scope, element) {
    
        var template = $templateCache.get(metadataSourceFieldsetTemplate);
        element.html(template);

        scope.types = types;
        scope.theForm = scope.$parent.theForm;

        $compile(element.contents())(scope);

        /**
        * Test database connection.
        * param : Metadata config id.
        */
        scope.testConnection = function(){
            metadataService.testMetadataConfig(scope.metadata.id.id).then(function success(response) {
                    if(response){
                        toast.showSuccess($translate.instant('metadataConfig.connection-sucessfull'));
                    }
                    else{
                        toast.showError($translate.instant('metadataConfig.connection-failed'));
                    }
                }, function fail() {
            });
        }
    };

    return {
        restrict: "A",
        link: linker,
        scope: {
            source: "=",
            metadata: "=",
            isEdit: '=?',
        }
    }
}


