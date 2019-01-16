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
/* eslint-disable import/no-unresolved, import/default, no-unused-vars */
import './template.scss';
import addTemplateEditor from './add-template.tpl.html';

/*@ngInject*/
export function TemplateController($scope, userGroupService, $filter, $rootScope, $translate, templateService, $mdDialog, $document, $stateParams) {

    var vm = this;
    vm.addTemplate = addTemplate;
    vm.AddTemplateModelController = 'AddTemplateModelController';
    var customerId = $stateParams.customerId;
    vm.copyEditor = copyEditor;
    vm.deletetemplate = deletetemplate;

    var editorActionsList = [];

     editorActionsList.push({
          onAction: function($event, item) {
              copyEditor($event, item);
          },
          name: function() {
              return $translate.instant('action.delete')
          },
          details: function() {
              return $translate.instant('templateEditor.copy')
          },
          icon: "content_copy"
      });


    editorActionsList.push({
        onAction: function($event, item) {
            vm.grid.deleteItem($event, item);
        },
        name: function() {
            return $translate.instant('action.delete')
        },
        details: function() {
            return $translate.instant('templateEditor.delete')
        },
        icon: "delete"
    });



    $scope.tableView = false;

    $scope.templates = {
            count: 0,
            data: []
        };

    $scope.query = {
            order: 'name',
            limit: 15,
            page: 1,
            search: null
     };



    vm.templateEditorGridConfig = {

        deleteItemTitleFunc: deleteEditorTitle,
        deleteItemContentFunc: deleteEditorText,
        deleteItemsTitleFunc: deleteEditorsTitle,
        deleteItemsActionTitleFunc: deleteEditorsActionTitle,
        deleteItemsContentFunc: deleteEditorsText,
        addItemController: 'AddTemplateModelController',
        deleteItemFunc: deleteEditor,
        addItemTemplateUrl: addTemplateEditor,
        parentCtl: vm,
        addIcon:"add",
        getItemTitleFunc: getTemplateTitle,
        actionsList: editorActionsList,
        onGridInited: gridInited,
        noItemsText: function() {
            return $translate.instant('templateEditor.no-template-text')
        },
        itemDetailsText: function() {
            return $translate.instant('templateEditor.templateDetails')
        },
        addItemText: function() {
            return $translate.instant('templateEditor.add-template-text')
        },
        entType: "templateEditor"

    }

    initController();

     function initController() {
            var fetchTemplateFunction = null;
            var refreshUsersGroupParamsFunction = null;
            var saveTemplateFunction = null;
            var refreshTemplateParamsFunction = null;
             fetchTemplateFunction = function(pageLink) {

                if($scope.query.page == 1){

                    return templateService.getTemplates(pageLink, 0);
                } else {

                    return templateService.getTemplates(pageLink, $scope.query.page - 1);
                }

             };

            saveTemplateFunction = function(template) {

                return templateService.saveTemplate(template);
            };

           refreshTemplateParamsFunction = function() {
                return {"topIndex": vm.topIndex};
            };


            vm.templateEditorGridConfig.fetchItemsFunc = fetchTemplateFunction;
            vm.templateEditorGridConfig.saveItemFunc = saveTemplateFunction;
            vm.templateEditorGridConfig.refreshParamsFunc = refreshTemplateParamsFunction;

     }

    loadTableData();

    function loadTableData() {
        var promise = vm.templateEditorGridConfig.fetchItemsFunc({limit: $scope.query.limit, textSearch: ''}, false);
        if(promise) {
            promise.then(function success(items) {
                var templateSortList = $filter('orderBy')(items.data, $scope.query.order);

                if ($scope.query.search != null) {

                    templateSortList = $filter('filter')(items.data, function(data) {
                        if ($scope.query.search) {
                            return data.name.toLowerCase().indexOf($scope.query.search.toLowerCase()) > -1;
                        } else {
                            return true;
                        }
                    });
                    templateSortList = $filter('orderBy')(templateSortList, $scope.query.order);
                }

                var templatePaginatedata = templateSortList;

                $scope.templates = {
                    count: items.totalElements,
                    data: templatePaginatedata
                };

                },
            );

        }
    }


    $scope.enterFilterMode = function() {

        $scope.query.search = '';
        //loadTableData();
    }

    $scope.exitFilterMode = function() {

        $scope.query.search = null;
        loadTableData();
    }

    $scope.resetFilter = function() {

        $scope.query = {
            order: 'name',
            limit: $scope.query.limit,
            page: 1,
            search: null
        };

        loadTableData();
        vm.grid.refreshList();
    }

    vm.loadTableData = loadTableData;
    $scope.$watch("query.search", function(newVal, prevVal) {
        if (!angular.equals(newVal, prevVal) && $scope.query.search != null) {

            loadTableData();
        }
    });

    $scope.onReorder = function() {
        loadTableData();
    }

    $scope.onPaginate = function(page) {
        $scope.query.page = page;
        loadTableData();
    }

    function deleteEditor(templateId) {
        return templateService.deleteTemplate(templateId);
    }

    function copyEditor($event,template) {

        if ($event) {
            $event.stopPropagation();
        }

        vm.item = {};
        vm.templateName =[];
        var copyName = '';
        templateService.getAllTemplates().then(function (response) {
            response.forEach(tempVal => { //
                vm.templateName.push(tempVal.name);
           });

           var count = copyCount(template, vm.templateName);
            copyName = "("+(count.highestcopy+1)+")";
             vm.item ={
               name:template.name.replace(/\(.*\)/, '')+copyName,
               body:template.body
             }

              templateService.saveTemplate(vm.item).then(function () {
                    vm.grid.refreshList();
                     loadTableData();
              });

         });
    }


    function copyCount(template, templateList) {
        var count = 0;
        vm.countDetail ={};
        vm.copyCountHighest = [];
        var regExp = /\(([^)]+)\)/;
        var regex = /\([^)]*\)/g;

        for (var i = 0; i < templateList.length; i++) {
            if (templateList[i].replace(/\(.*\)/, '') === template.name.replace(/\(.*\)/, '')) {
                count++;
                var matches = regExp.exec(templateList[i]);
                if(matches == null){
                    vm.copyCountHighest.push(0);
                } else {
                    vm.copyCountHighest.push(Number(matches[1]));
                }
            }
        }
        var largestCopy = vm.copyCountHighest.sort((a,b)=>a-b).reverse()[0];
        vm.countDetail = {count:count,highestcopy:largestCopy};

        return vm.countDetail;
    }

    function deletetemplate($event,item) {

        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(deleteEditorTitle(item))
            .htmlContent(deleteEditorText(item))
            .ariaLabel($translate.instant('grid.delete-item'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            vm.templateEditorGridConfig.deleteItemFunc(item.id.id).then(function success() {
                $scope.resetFilter();
                vm.grid.refreshList();

            });
        });

    }


    function getTemplateTitle(template) {
            return template ? template.name : '';
    }


    function deleteEditorTitle(template) {
        return $translate.instant('templateEditor.delete-template-title', {
            templateName: template.name
        });
    }

    function deleteEditorText() {
        return $translate.instant('templateEditor.delete-template-text');
    }

    function deleteEditorsTitle(selectedCount) {
        return $translate.instant('templateEditor.delete-templates-title', {
            count: selectedCount
        }, 'messageformat');
    }

    function deleteEditorsActionTitle(selectedCount) {
        return $translate.instant('templateEditor.delete-templates-action-title', {
            count: selectedCount
        }, 'messageformat');
    }

    function addTemplate($event) {

            $mdDialog.show({
                controller: vm.AddTemplateModelController,
                controllerAs: 'vm',
                templateUrl: addTemplateEditor,
                parent: angular.element($document[0].body),
                locals: {saveItemFunction: vm.templateEditorGridConfig.saveItemFunc},
                fullscreen: true,
                targetEvent: $event
            }).then(function() {$scope.resetFilter();}, function() {});
     }


    $scope.templateDetailFunc = function($event,template) {
        $rootScope.$emit("CallTableDetailTemplate", [$event, template]);
    }


    function deleteEditorsText() {
        return $translate.instant('templateEditor.delete-templates-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }


}