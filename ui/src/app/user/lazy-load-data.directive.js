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
//mport './audit-log.scss';
/* eslint-disable import/no-unresolved, import/default */


export default function LazyLoadDataDirective($compile) {

    return {
            link: function(scope, el, attrs) {
            var now = new Date().getTime();
            var rep = angular.element(document.getElementsByClassName("md-virtual-repeat-scroller"));
            rep.on('scroll', function(evt){
            if (rep[0].scrollTop + rep[0].offsetHeight >= rep[0].scrollHeight)
               if (new Date().getTime() - now > 100)
                    {
                        now = new Date().getTime();
                        scope.$apply(function() {
                         scope.$eval(attrs.lazyLoadData);
                       });
                    }
                 });
             }
  }