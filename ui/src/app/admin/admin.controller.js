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
import changeThemeTemplate from './change-theme.tpl.html';

/*@ngInject*/

export default function AdminController(adminService, userService, toast, $scope, $rootScope, $state, $translate, $mdDialog, $document,importExport) {

    var vm = this;
    vm.save = save;
    vm.sendTestMail = sendTestMail;
    vm.changeTheme = changeTheme;
    vm.cancel = cancel;
    vm.changeDefaultTheme = changeDefaultTheme;
    vm.changeLogo = changeLogo;


    $scope.selectedTheme = { };

    vm.smtpProtocols = ('smtp smtps').split(' ').map(function (protocol) {
        return protocol;
    });

    $translate('admin.test-mail-sent').then(function (translation) {
        vm.testMailSent = translation;
    }, function (translationId) {
        vm.testMailSent = translationId;
    });


    loadSettings();
    loadthemes();


    function loadthemes() {

        adminService.getAllThemes().then(function success(themes) {
            vm.themeForm = themes;
            $scope.themes = themes;
            $scope.themeName = $rootScope.themeName;
        });

    }

    function loadSettings() {
        adminService.getAdminSettings($state.$current.data.key).then(function success(settings) {
            vm.settings = settings;
        });
    }

    function save() {
        adminService.saveAdminSettings(vm.settings).then(function success(settings) {
            vm.settings = settings;
            vm.settingsForm.$setPristine();
        });
    }

    function sendTestMail() {
        adminService.sendTestMail(vm.settings).then(function success() {
            toast.showSuccess($translate.instant('admin.test-mail-sent'));
        });
    }

    function changeTheme($event) {

        $mdDialog.show({
            controller: 'AdminController',
            controllerAs: 'vm',
            templateUrl: changeThemeTemplate,
            parent: angular.element($document[0].body),
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
        }, function () {
        });
    }


    function cancel() {
        $mdDialog.cancel();
    }

    function changeDefaultTheme () {


        if(angular.isDefined($scope.selectedTheme.themeValue) == false) {

            toast.showError($translate.instant('admin.bad-request'));
            return false;
        }
        vm.themeData = {value:$scope.selectedTheme.themeValue};
        adminService.saveThemeSettings(vm.themeData).then(function success(theme) {
            $rootScope.themeValue = theme.themeValue;
            $rootScope.themeName =  theme.themeName;
            $scope.themeName = theme.themeName;
            $rootScope.theme = theme.themeValue;
            $mdDialog.cancel();
        });

    }

    function changeLogo($event) {

         importExport.importLogo($event).then(function success(logo) {
             $rootScope.logo = logo.file;
             var promise =  userService.getLogo();
             if(promise) {
                 promise.then(function success(logo) {
                         $rootScope.logo = logo.file;
                         $rootScope.fileType = angular.lowercase(logo.name.substr(logo.name.lastIndexOf('.')+1));

                     },
                 )
             }
         });


    }


}