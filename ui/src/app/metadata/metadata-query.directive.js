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

import metadataQueryTemplate from './metadata-query.tpl.html';
import queryDialogTemplate from './query-dialog.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function MetadataQueryDirective($compile, $templateCache, $mdDialog, metadataService, $translate, $filter) {
    var linker = function(scope, element) {
        var template = $templateCache.get(metadataQueryTemplate);
        element.html(template);
        scope.queries = [];
        scope.selectedQueries = [];
        scope.paginationDetails = {
            order: 'name',
            limit: 15,
            page: 1,
            search: null
        };

        /**
        * Fetch all queries from metadata_query table..
        *
        */
        scope.getAllQuery = function() {
            metadataService.getAllMetadataQuery(scope.metadata.id.id,scope.paginationDetails.limit).then(function success(response) {
                scope.queries = response.data;
                var querySortList = $filter('orderBy')(response.data, scope.paginationDetails.order);
                var startIndex = scope.paginationDetails.limit * (scope.paginationDetails.page - 1);

                var queryPaginatedata = querySortList.slice(startIndex, startIndex + scope.paginationDetails.limit);

                scope.queries = {
                    count: response.data.length,
                    data: queryPaginatedata
                };
            }, function fail() {
            });
        }

        /**
        * open query dialog to add query.
        *
        * param :metadata.
        */
        scope.addQuery = function($event) {
            $mdDialog.show({
                controller: "QueryDialogController",
                controllerAs: 'vm',
                templateUrl: queryDialogTemplate,
                locals: {
                    metadata: scope.metadata,
                    metadataQuery:null,
                    isAdd: true,
                    selectedIndex:0,
                    isReadOnly:false
                },
                targetEvent: $event,
                fullscreen: true,
                skipHide: true
            }).then(function() {
                scope.getAllQuery();
            }, function() {});
        }

        /**
        * Get all query and displyed in table. .
        *
        * param : query and metadata.
        */
        scope.$watch("metadata", function(newVal) {
            if(newVal){
                scope.getAllQuery();
            }
        });

        /**
        * Open query dialog box .
        *
        * param : query and metadata.
        */
        scope.openQuery = function($event,query) {
            $mdDialog.show({
                controller: "QueryDialogController",
                controllerAs: 'vm',
                templateUrl: queryDialogTemplate,
                locals: {
                    metadata: scope.metadata,
                    metadataQuery: query,
                    selectedIndex: 1,
                    isAdd: false,
                    isReadOnly:false
                },
                targetEvent: $event,
                fullscreen: true,
                skipHide: true
            }).then(function() {
            }, function() {});
        }

        /**
        * Delete query from metadata_query table.
        *
        * param : List of queries.
        */
        scope.deleteQuery = function($event,query) {
            var confirm = $mdDialog.confirm()
                        .targetEvent($event)
                        .title($translate.instant('metadataConfig.delete-query'))
                        .htmlContent($translate.instant('grid.delete-items-text'))
                        .ariaLabel($translate.instant('grid.delete-items'))
                        .cancel($translate.instant('action.no'))
                        .ok($translate.instant('action.yes'));
                    $mdDialog.show(confirm).then(function () {
                        var i, len=query.length ;
                        for(i=0; i<len ;i++){
                            metadataService.deleteMetadataQuery(query[i].id.id).then(function success() {
                                if (i == len) {
                                    scope.getAllQuery();
                                    scope.selectedQueries =[];
                                }
                                }, function fail() {
                            });
                        }
                        },
                    function () {
                    });
        }
        $compile(element.contents())(scope);

    };

    return {
        restrict: "E",
        link: linker,
        scope: {
            metadata: '=',
            isEdit: '=',
            theForm: '=',
            onDeleteMetadata: '&'
        }
    }
}


