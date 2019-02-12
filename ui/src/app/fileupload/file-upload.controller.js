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
export default function FileUploadController(toast, $scope, $document, fileUploadService, $q, $timeout, $translate, $mdDialog) {

	var vm = this;
	vm.files = [];

	function loadTableData() {
		var promise = fileUploadService.getAllFile();
		if (promise) {
			promise.then(function success(items) {
				vm.files = items;
			})
		}
	}

	loadTableData();

	vm.openFileDialog = function () {
		angular.element($document[0].getElementById('inputFile').click());
	};

	$scope.fileUpload = function (element) {
		checkFile(element);
	}

	function checkFile(element) {
		var fileToBeUploaded = element.files;
		var deferred = $q.defer();
		fileUploadService.searchFile(fileToBeUploaded[0].name, fileToBeUploaded[0].type).then(
			function success(searchItems) {

				if (searchItems.length == 0) {
					if (fileUploadService.fileValidation(fileToBeUploaded) === true) {
						saveFile(fileToBeUploaded[0]).then(

							function success() {

								angular.element(element).val(null);
							}
						);

					}
				}

				if (searchItems.length != 0) {
					replaceFile(element, fileToBeUploaded[0]);

				}
			},
			function fail() {
				deferred.reject();
			}
		);
		return deferred.promise;
	}

	function saveFile(file) {
		var deferred = $q.defer();
		fileUploadService.uploadFile(file).then(
			function success(savedFile) {
				loadTableData();
				toast.showSuccess($translate.instant('file-upload.fileSuccess'));
				return savedFile;
			},
			function fail() {
				deferred.reject();
			}
		);
		return deferred.promise;
	}

	vm.downloadFile = function ($event, file) {
		$event.stopPropagation();
		fileUploadService.exportFile(file.fileName, file.extension);
	}


	vm.deleteFile = function ($event, file) {
		var confirm = $mdDialog.confirm()
			.targetEvent($event)
			.title(deleteFileTitle(file))
			.htmlContent(deleteFileText())
			.ariaLabel($translate.instant('grid.delete-item'))
			.cancel($translate.instant('action.no'))
			.ok($translate.instant('action.yes'));
		$mdDialog.show(confirm).then(function () {
			fileUploadService.deleteFile(file.fileName, file.extension).then(function success() {
				vm.resetFilter();

			});
		});
	}

	vm.resetFilter = function () {
		loadTableData();
	}

	function deleteFileTitle(file) {
		return $translate.instant('file-upload.delete-file-title', {
			fileName: file.fileName
		});
	}

	function deleteFileText() {
		return $translate.instant('file-upload.delete-file-text');
	}

	vm.renameFileName = function ($event, file) {
		$mdDialog.show({
			controller: 'RenameFileController',
			controllerAs: 'vm',
			templateUrl: renameFileNameTemplate,
			parent: angular.element($document[0].body),
			locals: {
				oldFileName: file.fileName,
				extension: file.extension
			},
			fullscreen: true,
			targetEvent: $event
		}).then(function () {

			vm.resetFilter();

		});

	}

	function replaceFile($event, file) {
		var confirm = $mdDialog.confirm()
			.targetEvent($event)
			.title(replaceFileTitle(file))
			.htmlContent(replaceFileText())
			.ariaLabel($translate.instant('file-upload.replace-file'))
			.cancel($translate.instant('action.no'))
			.ok($translate.instant('action.yes'));
		$mdDialog.show(confirm).then(function () {
			saveFile(file).then(function success() {
				vm.resetFilter();
			});
		});
	}


	function replaceFileTitle(file) {
		return $translate.instant('file-upload.replace-file-title', {
			fileName: file.fileName
		});
	}

	function replaceFileText() {
		return $translate.instant('file-upload.replace-file-text');
	}

}
