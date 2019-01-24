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

import logoSvg from '../../svg/logo_title_white.svg';
import defaultLogoSvg from '../../svg/Tempus_Logo_E_TagLineExtended_vectorized.svg';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function SignUpController(toast, loginService, userService,$state,signUpService, $log) {
    var vm = this;

    vm.logoSvg = logoSvg;
    vm.defaultLogoSvg = defaultLogoSvg;

    vm.validateEmail = validateEmail;
    vm.signup = signup;


    function validateEmail(emailField){
            var reg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;

            if (reg.test(emailField.value) == false)
            {
                alert('Invalid Email Address');
                return false;
            }

            return true;
    }
    function signup(){
     //$state.go('activation-link');
     $log.log("vm.user")
     $log.log(vm.signupRequest)
     vm.signupRequest.authority = 'TENANT_ADMIN';
     signUpService.saveTrialUser(vm.signupRequest).then(
         function success(info) {
             $log.log(info)
         }
     );
    }




}