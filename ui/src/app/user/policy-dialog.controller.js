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

export default function PolicyDialogController($scope, userService, groupId, isAdd, $mdDialog, types, userGroupService, customerId, dataModelId, groupObject, datamodelService) {

  var vm = this;

  vm.cancel = cancel;
  vm.types = types;
  vm.dataModelId = dataModelId;

  vm.entityTypeChange = entityTypeChange;
  vm.dataModelObjectChange = dataModelObjectChange;
  vm.groupObject = groupObject;
  vm.save = save;
  vm.entityChange = entityChange;
  vm.permissions = '';
  vm.isShown = false;


  function cancel() {
    $mdDialog.cancel();
  }


  function dataModelObjectChange() {

    vm.entityValues = {};
    vm.entityValues['ALL'] = "ALL";
    if (vm.dataModelObject != "ALL") {
      if(vm.entity.type == "DEVICE") {
       datamodelService.getDatamodelObjectAttributesDeviceType(vm.dataModelObject).
       then(function success(data) {
         data.forEach(entVal => { //
           vm.entityValues[entVal.id.id] = entVal.name;

         });
       });

      } else {

       datamodelService.getDatamodelObjectAttributes(vm.dataModelObject).
       then(function success(data) {
         data.forEach(entVal => { //
           vm.entityValues[entVal.id.id] = entVal.name;

         });
       });
      }
    } else {

      vm.permissions = '';
      vm.permissions = vm.types.permissionType;
      vm.entity.values = "ALL";


    }
  }

  function entityTypeChange() {

    vm.dataModelObjectValues = {};
    vm.dataModelObjectValues['ALL'] = "ALL";
    if (vm.entity.type != 'ALL') {
      datamodelService.getDatamodelObjects(vm.dataModelId).
      then(function success(data) {
        data.forEach(dmo => { //
          if (dmo.type.toLowerCase() == vm.entity.type.toLowerCase()) {
            vm.dataModelObjectValues[dmo.id.id] = dmo.name;
          }

        });
      }, function fail() {

      });

    } else {
      vm.dataModelObject = "ALL";
      vm.dataModelObjectChange();
      vm.permissions = '';
      vm.permissions = vm.types.permissionType;


    }
  }


  function entityChange() {

    if (vm.entity.values !== '' || vm.entity.values !== null) {

      vm.permissions = angular.copy(vm.types.permissionType);

      if (vm.entity.values !== 'ALL') {
        delete vm.permissions['CREATE'];
      } else {
        vm.permissions = '';
        vm.permissions = vm.types.permissionType;


      }
    }

  }


  function save() {

    var userString = '';
    var policyString = '';
    if(userService.getAuthority() == 'TENANT_ADMIN') {

        userString = vm.types.userTypes['CUSTOMER_USER'];

    } else if(userService.getAuthority() == 'SYS_ADMIN') {

        userString = vm.types.userTypes['TENANT_ADMIN'];
    }

    let $errorElement = angular.element('[name=theForm]').find('.ng-invalid');

    if ($errorElement.length) {

      let $mdDialogScroll = angular.element('md-dialog-content').scrollTop();
      let $mdDialogTop = angular.element('md-dialog-content').offset().top;
      let $errorElementTop = angular.element('[name=theForm]').find('.ng-invalid').eq(0).offset().top;


      if ($errorElementTop !== $mdDialogTop) {
        angular.element('md-dialog-content').animate({
          scrollTop: $mdDialogScroll + ($errorElementTop - $mdDialogTop) - 50
        }, 500);
        $errorElement.eq(0).focus();
      }
    } else {

      if (vm.entity.type == 'ALL') {

        policyString = userString + ':' + '*:' + vm.permission.type;
      } else {

        if (vm.dataModelObject == 'ALL') {

          policyString = userString + ':' + vm.entity.type + ':' + vm.permission.type;

        } else {

          if (vm.entity.values == 'ALL') {

            policyString = userString + ':' + vm.entity.type + '?dataModelId=' + vm.dataModelObject + ':' + vm.permission.type;

          } else {


            policyString = userString + ':' + vm.entity.type + '?dataModelId=' + vm.dataModelObject + '&id=' + vm.entity.values + ':' + vm.permission.type;

          }

        }

      }
      if (vm.groupObject.policies == null || vm.groupObject.policies.length == 0) {

        var policyArr = [];
        policyArr.push(policyString);
        vm.groupObject.policies = policyArr;


      } else {

        vm.groupObject.policies.push(policyString);


      }

      userGroupService.saveUserGroup(vm.groupObject).
      then(function success() {

        $mdDialog.hide();
      }, function fail() {

      });

    }


  }


}