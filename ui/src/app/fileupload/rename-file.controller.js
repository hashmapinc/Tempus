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

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function RenameFileController($mdDialog, fileUploadService, $scope, oldFileName, extension, $q, toast, $translate) {

	var vm = this;
	vm.newFileName = oldFileName;
	vm.oldFileName = oldFileName;

	vm.rename = function () {
		var deferred = $q.defer();
		fileUploadService.searchFile(vm.newFileName, extension).then(
			function success(searchItems) {
				var existFile = 0;
				if (searchItems.length == 0) {

					existFile = 0;
				} else {

					for (var i = 0; i < searchItems.length; i++) {
						if (searchItems[i].fileName == vm.newFileName) {
							existFile++;
						}
					}
					if (existFile > 0) {
						toast.showError($translate.instant('file-upload.fileNameError'));
						return false;
					}
				}

				if (existFile == 0) {
					fileUploadService.renameFile(vm.oldFileName, vm.newFileName, extension).then(function success(item) {
						vm.item = item;
						$scope.theForm.$setPristine();
						$mdDialog.hide();
					});

				}
			},
			function fail() {
				deferred.reject();
			}
		);
		return deferred.promise;
	}


	vm.cancel = function () {
		$mdDialog.cancel();
	}

}