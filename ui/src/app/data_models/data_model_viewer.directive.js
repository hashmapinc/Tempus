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
/* eslint-disable import/no-unresolved, import/default, no-unused-vars */
import vis from "vis";

/*@ngInject*/
export default function DataModelViewerDirective() {
  function link($scope, $element, $attrs, ngModel) {
    $element.append("<h1>RANDY</h1>")
    var network = new vis.Network($element[0], $scope.ngModel, $scope.options || {});
    alert(network);
    var onSelect = $scope.onSelect() || function (prop) { };
    network.on('select', function (properties) {
      onSelect(properties);
    });
  }

  return {
    restrict: 'E',
    require: '^ngModel',
    scope: {
      ngModel: '=',
      onSelect: '&',
      options: '='
    },
    link: link
  }
}