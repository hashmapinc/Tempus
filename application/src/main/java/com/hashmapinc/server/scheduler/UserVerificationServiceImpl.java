/**
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

package com.hashmapinc.server.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.exception.TempusException;
import com.hashmapinc.server.dao.mail.DefaultMailService;
import com.hashmapinc.server.dao.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserVerificationServiceImpl implements UserVerificationService {
    @Autowired
    private UserService userService;

    @Autowired
    DefaultMailService mailService;

    private final String IS_TRIAL_ACCOUNT = "trialAccount";
    private final String DATE = "date";
    private static final String TENANT_ADMIN = "TENANT_ADMIN";


    @Override
    public void disableAllExpiredUser(final int expiryTimeInMinutes, final int reminderMailTimeInMinutes) {
        try {
            List<User> tenantAdminUsers = userService.findTrialUserByClientIdAndAuthority("tempus",TENANT_ADMIN);

            for (User user : tenantAdminUsers) {
                if(user.isEnabled()) {
                    Map<String, String> additionalDetails = getAdditionalDetails(user);
                    boolean trialUser = getTrialUser(additionalDetails);
                    Long registeredTime = getRegisteredTime(additionalDetails);

                    if (trialUser && (registeredTime != null)) {
                        sendReminderMail(registeredTime, reminderMailTimeInMinutes, user);
                        if (isUserExpired(registeredTime, expiryTimeInMinutes)) {
                            user.setEnabled(false);
                            userService.saveUser(user);
                            disableExpiredTenantCustomers(user.getTenantId().getId().toString());
                            mailService.sendAccountExpiryMail(user.getEmail());
                        }
                    }
                }
            }
        }catch (Exception exp) {
            log.info("scheduler is failed to expiring the trial user");
            log.warn("Exception while expiring the trial user [{}]", exp.getMessage());
        }
    }

    private void sendReminderMail(long registeredTime, int reminderMailTimeInMinutes ,User user) throws TempusException {
        Date expiryDate = calculateExpiryDate(registeredTime,reminderMailTimeInMinutes);
        Calendar cal = Calendar.getInstance();
        if((expiryDate.getTime() - cal.getTime().getTime()) <= 0) {
            mailService.sendExpiryRemainderMailToUser(user.getEmail());
        }
    }

    private Map<String, String> getAdditionalDetails(User user) {
        JsonNode jsonNode = user.getAdditionalInfo();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(jsonNode, new TypeReference<Map<String, String>>(){});
    }

    private Long getRegisteredTime(Map<String, String> additionalDetails) {
        Long registeredTime = null;
        if(additionalDetails.get(DATE) != null)
            registeredTime = Long.parseLong(additionalDetails.get(DATE));
        return registeredTime;
    }

    private boolean getTrialUser(Map<String, String> additionalDetails) {
        if(additionalDetails.get(IS_TRIAL_ACCOUNT) != null)
            return Boolean.parseBoolean(additionalDetails.get(IS_TRIAL_ACCOUNT));
        return false;
    }

    private void disableExpiredTenantCustomers(String tenantId) {
        try {
            List<User> customerUsers = userService.findUsersTenantId(tenantId);
            for (User user : customerUsers) {
                user.setEnabled(false);
                userService.saveUser(user);
            }
        }catch (Exception exp){
            log.warn(exp.getMessage());
        }
    }

    private boolean isUserExpired(long registeredTime, final int expiryTimeInMinutes){
        Date expiryDate = calculateExpiryDate(registeredTime,expiryTimeInMinutes);
        Calendar cal = Calendar.getInstance();
        return (expiryDate.getTime() - cal.getTime().getTime()) <= 0;
    }

    private Date calculateExpiryDate(long registeredTime, final int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(registeredTime);
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}
