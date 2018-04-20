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
export default angular.module('thingsboard.directives.mousepointMenu', [])
    .directive('tbMousepointMenu', MousepointMenu)
    .name;

/*@ngInject*/
function MousepointMenu() {

    var linker = function ($scope, $element, $attrs, RightClickContextMenu) {

        $scope.$mdOpenMousepointMenu = function($event){
            RightClickContextMenu.offsets = function(){
                var offset = $element.offset();
                var x = $event.pageX - offset.left;
                var y = $event.pageY - offset.top;

                var offsets = {
                    left: x,
                    top: y
                }
                return offsets;
            }
            RightClickContextMenu.open($event);
        };

        $scope.$mdCloseMousepointMenu = function() {
            RightClickContextMenu.close();
        }
    }

    return {
        restrict: "A",
        link: linker,
        require: 'mdMenu'
    };
}
