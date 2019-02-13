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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.mail.DefaultMailService;
import com.hashmapinc.server.dao.user.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.*;

import static com.hashmapinc.server.dao.model.ModelConstants.NULL_UUID;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserVerificationServiceTest {

    public static final String TENANT_ID = NULL_UUID.toString();
    private final String TENANT_ADMIN = "TENANT_ADMIN";

    @Mock
    private UserService userService;

    @InjectMocks
    private UserVerificationServiceImpl userVerificationService;

    @Mock
    private DefaultMailService mailService;


    @Test
    public void shouldDisableAllExpiredTrialUser() throws Exception {

        List<User> foundTenantAdminsUsers = new ArrayList<>();
        User user = createUser("trialUser@hashmapinc.com",new TenantId(NULL_UUID),Authority.TENANT_ADMIN,true);
        foundTenantAdminsUsers.add(user);

        when(userService.findTrialUserByClientIdAndAuthority("tempus",Authority.TENANT_ADMIN.toString())).thenReturn(foundTenantAdminsUsers);

        User disabledUser = new User(user) ;
        disabledUser.setEnabled(false);
        when(userService.saveUser(disabledUser)).thenReturn(disabledUser);

        when(userService.findUsersTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        doNothing().when(mailService).sendExpiryRemainderMailToUser(Mockito.anyString());
        doNothing().when(mailService).sendAccountExpiryMail(Mockito.anyString());

        userVerificationService.disableAllExpiredTrialUser(0,0);

        Mockito.verify(userService,times(1)).saveUser(disabledUser);
        Mockito.verify(userService,times(1)).findUsersTenantId(TENANT_ID);
    }

    @Test
    public void shouldNotDisableNonTrialUser() throws Exception {
        List<User> foundTenantAdminsUsers = new ArrayList<>();
        User user = createUser("demo@hashmapinc.com",new TenantId(NULL_UUID),Authority.TENANT_ADMIN,false);
        foundTenantAdminsUsers.add(user);

        when(userService.findTrialUserByClientIdAndAuthority("tempus",Authority.TENANT_ADMIN.toString())).thenReturn(foundTenantAdminsUsers);

        userVerificationService.disableAllExpiredTrialUser(0,0);

        Mockito.verify(userService,times(0)).findUsersTenantId(TENANT_ID);
        Mockito.verify(userService,times(0)).saveUser(user);
    }

    @Test
    public void shouldDisableExpiredTrialTenantCustomersAlso() throws Exception {
        List<User> foundTenantAdminsUsers = new ArrayList<>();
        User tenantUser = createUser("trialUser@hashmapinc.com",new TenantId(NULL_UUID),Authority.TENANT_ADMIN,true);
        foundTenantAdminsUsers.add(tenantUser);
        when(userService.findTrialUserByClientIdAndAuthority("tempus",Authority.TENANT_ADMIN.toString())).thenReturn(foundTenantAdminsUsers);

        User disabledTenantUser = new User(tenantUser) ;
        disabledTenantUser.setEnabled(false);
        when(userService.saveUser(tenantUser)).thenReturn(disabledTenantUser);

        List<User> foundCustomerUsers = new ArrayList<>();
        User customerUser = createUser("bob",tenantUser.getTenantId(), Authority.CUSTOMER_USER,false);
        foundCustomerUsers.add(customerUser);
        when(userService.findUsersTenantId(TENANT_ID)).thenReturn(foundCustomerUsers);

        when(userService.saveUser(customerUser)).thenReturn(customerUser);
        doNothing().when(mailService).sendExpiryRemainderMailToUser(Mockito.anyString());
        doNothing().when(mailService).sendAccountExpiryMail(Mockito.anyString());

        userVerificationService.disableAllExpiredTrialUser(0,0);

        Mockito.verify(userService,times(1)).findUsersTenantId(TENANT_ID);
        Mockito.verify(userService,times(1)).saveUser(tenantUser);
        Mockito.verify(userService,times(1)).saveUser(customerUser);
    }

    @Test
    public void shouldNotDisableNonExpiredTrialUser() throws Exception {
        List<User> foundTenantAdminsUsers = new ArrayList<>();
        User user = createUser("trialUser@hashmapinc.com",new TenantId(NULL_UUID),Authority.TENANT_ADMIN,true);
        foundTenantAdminsUsers.add(user);

        when(userService.findTrialUserByClientIdAndAuthority("tempus",Authority.TENANT_ADMIN.toString())).thenReturn(foundTenantAdminsUsers);

        userVerificationService.disableAllExpiredTrialUser(2880,2880);

        Mockito.verify(userService,times(0)).saveUser(user);
        Mockito.verify(userService,times(0)).findUsersTenantId(TENANT_ID);
    }

    private User createUser(String email , TenantId tenantId , Authority authority , boolean trialAccount){

        User user = new User();
        user.setEmail(email);
        user.setEnabled(true);
        user.setAuthority(authority);
        user.setTenantId(tenantId);
        user.setCustomerId(new CustomerId(NULL_UUID));

        if(trialAccount) {
            Map<String, String> additionalDetails = new HashMap<>();
            String IS_TRIAL_ACCOUNT = "trialAccount";
            additionalDetails.put(IS_TRIAL_ACCOUNT,"true");
            String DATE = "date";
            additionalDetails.put(DATE, Long.toString(atStartOfDay().getTime()));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.convertValue(additionalDetails, JsonNode.class);
            user.setAdditionalInfo(jsonNode);
        }
        return user;
    }

    private Date atStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = Date.from(Instant.now());
        long currentDateTime = currentDate.getTime();
        calendar.setTimeInMillis(currentDateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
