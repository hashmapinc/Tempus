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

/* eslint-disable angular/log */

import computationFormSparkTemplate from './computation-form-kubeless.tpl.html';
import sha256 from 'crypto-js/sha256';
import base64 from 'crypto-js/enc-base64';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ComputationFormKubelessDirective($compile, $templateCache, $translate, types) {
    var linker = function(scope, element) {

        var template = $templateCache.get(computationFormSparkTemplate);
        element.html(template);

        scope.types = types;
        scope.theForm = scope.$parent.theForm;

        $compile(element.contents())(scope);

        if(!scope.model.computationMetadata)
            scope.model.computationMetadata = {}

        scope.model.computationMetadata.type = types.computationType.kubeless;

        scope.fileAdded = function($file, model, fileType) {
            let reader = new FileReader();
            reader.onload = function(event) {
                scope.$apply(function() {
                    if(event.target.result) {
                        scope.theForm.$setDirty();
                        let addedFile = event.target.result;

                        if (addedFile && addedFile.length > 0) {
                            if(fileType == "function"){
                                model.computationMetadata.functionFileName = $file.name;
                                model.computationMetadata.functionContent = addedFile.replace(/^data.*base64,/, "");
                                model.computationMetadata.checksum = base64.stringify(sha256(model.computationMetadata.functionContent));
                                model.computationMetadata.functionContentType = 'base64';
                                if($file.getExtension() === 'jar' || $file.getExtension() === 'zip'){
                                    model.computationMetadata.functionContentType = model.computationMetadata.functionContentType+'+zip';
                                }
                            }
                            if(fileType == "dependencies"){
                                model.computationMetadata.dependenciesFileName = $file.name;
                                model.computationMetadata.dependencies = addedFile.replace(/^data.*base64,/, "");
                            }
                        }
                    }
                });
            };
            reader.readAsDataURL($file.file);
        };

        scope.clearFile = function(model, fileType) {
            scope.theForm.$setDirty();
            if(fileType == "dependencies"){
                model.computationMetadata.dependenciesFileName = null;
                model.computationMetadata.dependencies = null;
            }
            if(fileType == "function"){
                model.computationMetadata.functionFileName = null;
                model.computationMetadata.functionContent = null;
            }
        };

    };

    return {
        restrict: "A",
        link: linker,
        scope: {
            model: "="
        }
    }
}