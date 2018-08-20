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
import './side-menu.scss';
import tempusBranch from './branch.directive';


export default angular.module('tempus.directives.customMenu', [tempusBranch])
    .directive('tbCustomMenu', CustomMenu)
    .name;

/*@ngInject*/
function CustomMenu() {

  return {
    restrict: 'E', // tells Angular to apply this to only html tag that is <tree>
    replace: true, // tells Angular to replace <tree> by the whole template
    scope: {
        generatedSectionTree: '=src' // create an isolated scope variable 'generatedSectionTree' and pass 'src' to it.
    },
    template: '<ul class ="tb-custom-menu-toggle-list"><tb-branch ng-repeat="children in generatedSectionTree.children" src="children"></tb-branch></ul>'
  };
}
