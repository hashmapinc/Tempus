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
/*@ngInject*/
export default function ComponentDialogController($mdDialog, $q, templateService, $scope, $translate, componentDescriptorService, types, utils, helpLinks, isAdd, isReadOnly, componentInfo) {

    var vm = this;

    vm.isReadOnly = isReadOnly;
    vm.isAdd = isAdd;
    vm.componentInfo = componentInfo;
    vm.getTemplates = getTemplates;
    vm.getTemplateBody = getTemplateBody;
    vm.selectedTemplate = 'null';
    vm.templates = {};
    if (isAdd) {
        vm.componentInfo.component = {};
    }

    vm.componentHasSchema = false;
    vm.componentDescriptors = [];

    if (vm.componentInfo.component && !vm.componentInfo.component.configuration) {
        vm.componentInfo.component.configuration = {};
    }
    if(vm.componentInfo.title == "rule.plugin-action") {
        vm.getTemplates();
    }
    vm.helpLinkIdForComponent = helpLinkIdForComponent;
    vm.save = save;
    vm.cancel = cancel;
    $scope.$watch("vm.componentInfo.component.clazz", function (newValue, prevValue) {
        if (newValue != prevValue) {
            if (newValue && prevValue) {
                vm.componentInfo.component.configuration = {};
            }
            loadComponentDescriptor();
        }
    });

    var componentDescriptorsPromise =
        vm.componentInfo.type === types.componentType.action
            ? componentDescriptorService.getPluginActionsByPluginClazz(vm.componentInfo.pluginClazz)
            : componentDescriptorService.getComponentDescriptorsByType(vm.componentInfo.type);

    componentDescriptorsPromise.then(
        function success(componentDescriptors) {
            vm.componentDescriptors = componentDescriptors;
            if (vm.componentDescriptors.length === 1 && isAdd && !vm.componentInfo.component.clazz) {
                vm.componentInfo.component.clazz = vm.componentDescriptors[0].clazz;
            }
        },
        function fail() {
        }
    );

    loadComponentDescriptor();

    function loadComponentDescriptor () {
        if (vm.componentInfo.component.clazz) {
            componentDescriptorService.getComponentDescriptorByClazz(vm.componentInfo.component.clazz).then(
                function success(componentDescriptor) {

                    vm.componentDescriptor = componentDescriptor;
                    vm.componentHasSchema = utils.isDescriptorSchemaNotEmpty(vm.componentDescriptor.configurationDescriptor);
                },
                function fail() {
                }
            );
        } else {
            vm.componentHasSchema = false;
        }
    }

    function getTemplates() {
       vm.templates['null'] = $translate.instant('templateEditor.copyFrom');
       templateService.getAllTemplates().then(
           function success(templates) {
              templates.forEach(tempVal => { //
               if (vm.componentInfo.component) {
                 if(angular.isDefined(vm.componentInfo.component.configuration.deviceIdTemplate)) {
                       if(vm.componentInfo.component.configuration.rpcCallBodyTemplate == tempVal.body) {
                         vm.selectedTemplate = tempVal.id.id;
                         vm.template = tempVal.id.id;
                       }
                } else {
                      if(vm.componentInfo.component.configuration.template == tempVal.body) {
                        vm.selectedTemplate = tempVal.id.id;
                        vm.template = tempVal.id.id;
                      }
                 }

               }
                vm.templates[tempVal.id.id] = tempVal.name;
             });
           }
       );
    }

    function getTemplateBody(){
        if(angular.isDefined(vm.template) && vm.template !== 'null') {
           templateService.getTemplateDetail(vm.template).then(
               function success(template) {
                 vm.selectedTemplate = vm.template;
                 if(angular.isDefined(vm.componentInfo.component.configuration.deviceIdTemplate)) {
                        vm.componentInfo.component.configuration.rpcCallBodyTemplate = template.body;
                 } else {
                    vm.componentInfo.component.configuration.template = template.body;

                 }

               }
           );
        }
      }

    function helpLinkIdForComponent() {
        switch (vm.componentInfo.type) {
            case types.componentType.filter: {
                return helpLinks.getFilterLink(vm.componentInfo.component);
            }
            case types.componentType.processor: {
                return helpLinks.getProcessorLink(vm.componentInfo.component);
            }
            case types.componentType.action: {
                return helpLinks.getPluginActionLink(vm.componentInfo.component);
            }

        }
    }


    function cancel () {
        $mdDialog.cancel();
    }

    function save () {
        $mdDialog.hide(vm.componentInfo.component);
    }

}
