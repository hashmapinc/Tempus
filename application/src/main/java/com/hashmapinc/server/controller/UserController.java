/**
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
package com.hashmapinc.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.common.data.security.UserCredentials;
import com.hashmapinc.server.exception.TempusErrorCode;
import com.hashmapinc.server.exception.TempusException;
import com.hashmapinc.server.service.mail.MailService;
import com.hashmapinc.server.service.security.model.SecurityUser;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Slf4j
public class UserController extends BaseController {

    public static final String USER_ID = "userId";
    public static final String YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION = "You don't have permission to perform this operation!";
    public static final String ACTIVATE_URL_PATTERN = "%s/api/noauth/activate?activateToken=%s";
    @Autowired
    private MailService mailService;

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public User getUserById(@PathVariable(USER_ID) String strUserId) throws TempusException {
        checkParameter(USER_ID, strUserId);
        try {
            UserId userId = new UserId(toUUID(strUserId));
            SecurityUser authUser = getCurrentUser();
            if (authUser.getAuthority() == Authority.CUSTOMER_USER && !authUser.getId().equals(userId)) {
                throw new TempusException(YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION,
                        TempusErrorCode.PERMISSION_DENIED);
            }
            return checkUserId(userId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @ResponseBody
    public User saveUser(@RequestBody User user,
                         @RequestParam String activationType,
                         HttpServletRequest request) throws TempusException {

        try {
            // activationType = "mail", "link", "external"
            SecurityUser authUser = getCurrentUser();
            if (authUser.getAuthority() == Authority.CUSTOMER_USER && !authUser.getId().equals(user.getId())) {
                throw new TempusException(YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION,
                        TempusErrorCode.PERMISSION_DENIED);
            }
            boolean sendEmail = user.getId() == null && activationType.equals("mail");
            boolean isNewExternal = user.getId() == null && activationType.equals("external");
            if (getCurrentUser().getAuthority() == Authority.TENANT_ADMIN) {
                user.setTenantId(getCurrentUser().getTenantId());
            }
            User savedUser;
            if (sendEmail) {
                savedUser = checkNotNull(userService.saveUser(user));
                UserCredentials userCredentials = userService.findUserCredentialsByUserId(savedUser.getId());
                String baseUrl = constructBaseUrl(request);
                String activateUrl = String.format(ACTIVATE_URL_PATTERN, baseUrl,
                        userCredentials.getActivateToken());
                String email = savedUser.getEmail();
                try {
                    mailService.sendActivationEmail(activateUrl, email);
                } catch (TempusException e) {
                    userService.deleteUser(savedUser.getId());
                    throw e;
                }
            } else if(isNewExternal) {
                savedUser = userService.saveExternalUser(user);
            } else {
                savedUser = checkNotNull(userService.saveUser(user));
            }

            logEntityAction(savedUser.getId(), savedUser,
                    savedUser.getCustomerId(),
                    user.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return savedUser;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.USER), user,
                    null, user.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/user/sendActivationMail", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void sendActivationEmail(
            @RequestParam(value = "email") String email,
            HttpServletRequest request) throws TempusException {
        try {
            User user = checkNotNull(userService.findUserByEmail(email));
            UserCredentials userCredentials = userService.findUserCredentialsByUserId(user.getId());
            if (!userCredentials.isEnabled()) {
                String baseUrl = constructBaseUrl(request);
                String activateUrl = String.format(ACTIVATE_URL_PATTERN, baseUrl,
                        userCredentials.getActivateToken());
                mailService.sendActivationEmail(activateUrl, email);
            } else {
                throw new TempusException("User is already active!", TempusErrorCode.BAD_REQUEST_PARAMS);
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/user/{userId}/activationLink", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getActivationLink(
            @PathVariable(USER_ID) String strUserId,
            HttpServletRequest request) throws TempusException {
        checkParameter(USER_ID, strUserId);
        try {
            UserId userId = new UserId(toUUID(strUserId));
            SecurityUser authUser = getCurrentUser();
            if (authUser.getAuthority() == Authority.CUSTOMER_USER && !authUser.getId().equals(userId)) {
                throw new TempusException(YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION,
                        TempusErrorCode.PERMISSION_DENIED);
            }
            User user = checkUserId(userId);
            UserCredentials userCredentials = userService.findUserCredentialsByUserId(user.getId());
            if (!userCredentials.isEnabled()) {
                String baseUrl = constructBaseUrl(request);
                String activateUrl = String.format(ACTIVATE_URL_PATTERN, baseUrl,
                        userCredentials.getActivateToken());
                return activateUrl;
            } else {
                throw new TempusException("User is already active!", TempusErrorCode.BAD_REQUEST_PARAMS);
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteUser(@PathVariable(USER_ID) String strUserId) throws TempusException {
        checkParameter(USER_ID, strUserId);
        try {
            UserId userId = new UserId(toUUID(strUserId));
            User user = checkUserId(userId);
            userService.deleteUser(userId);

            logEntityAction(userId, user,
                    user.getCustomerId(),
                    ActionType.DELETED, null, strUserId);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.USER),
                    null,
                    null,
                    ActionType.DELETED, e, strUserId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenant/{tenantId}/users", params = { "limit" }, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<User> getTenantAdmins(
            @PathVariable("tenantId") String strTenantId,
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
        checkParameter("tenantId", strTenantId);
        try {
            TenantId tenantId = new TenantId(toUUID(strTenantId));
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return checkNotNull(userService.findTenantAdmins(tenantId, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/{customerId}/users", params = { "limit" }, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<User> getCustomerUsers(
            @PathVariable("customerId") String strCustomerId,
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
        checkParameter("customerId", strCustomerId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            checkCustomerId(customerId);
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(userService.findCustomerUsers(tenantId, customerId, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    
}
