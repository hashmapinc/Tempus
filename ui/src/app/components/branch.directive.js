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
import $ from 'jquery';

export default angular.module('tempus.directives.branch', [])
    .directive('tbBranch', Branch)
    .name;

function Branch($compile) {
return {
    restrict: 'E', // tells Angular to apply this to only html tag that is <branch>
    replace: true, // tells Angular to replace <branch> by the whole template
    scope: {
      b: '=src' // create an isolated scope variable 'b' and pass 'src' to it.
    },
    template: '<li><a ui-sref-active-eq="tb-active1" ui-sref="{{b.state}}" ><md-icon ng-if="b.link" ng-show="{{b.link != null}}"  md-svg-src="{{b.link}}" class="material-icons"></md-icon>{{b.name | translate}}</a></li>',
    link: function(scope, element) {
      //// Check if there are any children, otherwise we'll have infinite execution

      var has_children = angular.isArray(scope.b.children);

      //// Manipulate HTML in DOM
      if (has_children) {
        element.append('<tb-custom-menu src="b"></tb-custom-menu>');
        //this is for the dynamically adding the padding left  custom-side-menu sections
        element.find('a').css('padding-left', 16*scope.b.level +'px' );

        element.addClass("collapsed");
        // recompile Angular because of manual appending
        $compile(element.contents())(scope);
      }

      //// Bind events
      element.on('click', function(event) {
          event.stopPropagation();


          //remove the tb-active menu in custom-side-menu sections
          $("ul.test").find('.tb-active').removeClass('tb-active');
          $(event.target).addClass('tb-active');


          if (has_children) {
            element.toggleClass('collapsed');
          }
      });
    }
  };

}
