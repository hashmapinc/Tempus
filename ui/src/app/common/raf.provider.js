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
export default angular.module('thingsboard.raf', [])
    .provider('tbRaf', TbRAFProvider)
    .name;

function TbRAFProvider() {
    /*@ngInject*/
    this.$get = function($window, $timeout) {
        var requestAnimationFrame = $window.requestAnimationFrame ||
            $window.webkitRequestAnimationFrame;

        var cancelAnimationFrame = $window.cancelAnimationFrame ||
            $window.webkitCancelAnimationFrame ||
            $window.webkitCancelRequestAnimationFrame;

        var rafSupported = !!requestAnimationFrame;
        var raf = rafSupported
            ? function(fn) {
            var id = requestAnimationFrame(fn);
            return function() {
                cancelAnimationFrame(id);
            };
        }
            : function(fn) {
            var timer = $timeout(fn, 16.66, false);
            return function() {
                $timeout.cancel(timer);
            };
        };

        raf.supported = rafSupported;

        return raf;
    };
}
