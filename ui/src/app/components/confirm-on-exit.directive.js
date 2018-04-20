/*
 * Copyright © 2016-2018 Hashmap, Inc
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
export default angular.module('thingsboard.directives.confirmOnExit', [])
    .directive('tbConfirmOnExit', ConfirmOnExit)
    .name;

/*@ngInject*/
function ConfirmOnExit($state, $mdDialog, $window, $filter, userService) {
    return {
        link: function ($scope) {

            $window.onbeforeunload = function () {
                if (userService.isAuthenticated() && (($scope.confirmForm && $scope.confirmForm.$dirty) || $scope.isDirty)) {
                    return $filter('translate')('confirm-on-exit.message');
                }
            }
            $scope.$on('$stateChangeStart', function (event, next, current, params) {
                if (userService.isAuthenticated() && (($scope.confirmForm && $scope.confirmForm.$dirty) || $scope.isDirty)) {
                    event.preventDefault();
                    var confirm = $mdDialog.confirm()
                        .title($filter('translate')('confirm-on-exit.title'))
                        .htmlContent($filter('translate')('confirm-on-exit.html-message'))
                        .ariaLabel($filter('translate')('confirm-on-exit.title'))
                        .cancel($filter('translate')('action.cancel'))
                        .ok($filter('translate')('action.ok'));
                    $mdDialog.show(confirm).then(function () {
                        if ($scope.confirmForm) {
                            $scope.confirmForm.$setPristine();
                        } else {
                            $scope.isDirty = false;
                        }
                        $state.go(next.name, params);
                    }, function () {
                    });
                }
            });
        },
        scope: {
            confirmForm: '=',
            isDirty: '='
        }
    };
}