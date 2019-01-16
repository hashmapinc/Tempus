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
package com.hashmapinc.server.dao.mail;

import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.exception.TempusException;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.mail.DefaultMailService;
import com.hashmapinc.server.dao.user.UserService;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public  class MailServiceTest {

    @InjectMocks
    private DefaultMailService defaultMailService;
    @Mock
    private JavaMailSenderImpl mailSender;
    @Mock
    private UserService userService;
    @Mock
    private MessageSource messages;
    @Mock
    private VelocityEngine engine;
    @Mock
    private MimeMessage mimeMessage;

    private TenantId tenantId;
    private User tenantAdmin1;
    private User tenantAdmin2;

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";

    @Before
    public void setup(){
        ReflectionTestUtils.setField(defaultMailService, "mailFrom", "sysadmin@tempus.org"); //dependency injection

        tenantId = new TenantId(UUID.randomUUID());
        tenantAdmin1 = new User();
        tenantAdmin1.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin1.setTenantId(tenantId);
        tenantAdmin1.setEmail("tenant1@tempus.org");

        tenantAdmin2 = new User();
        tenantAdmin2.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin2.setTenantId(tenantId);
        tenantAdmin2.setEmail("tenant1@tempus.org");
    }

    @Test
    public void testSendAttributeMissingMailWithOneTenantUser() throws TempusException {
        List<User> users = new ArrayList<>();
        users.add(tenantAdmin1);
        TextPageData< User > userTextPageData = new TextPageData<>(users,null,false);

        when(userService.findTenantAdmins(Mockito.any(TenantId.class),Mockito.any(TextPageLink.class))).thenReturn(userTextPageData);
        when(messages.getMessage("attribute.missing.subject", null, Locale.US)).thenReturn("Tempus - parent_asset attribute");
        when(engine.mergeTemplate(Mockito.anyString(), Mockito.anyString(), Mockito.any(VelocityContext.class), Mockito.any(StringWriter.class))).thenReturn(true);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(Mockito.any(MimeMessage.class));

        defaultMailService.sendAttributeMissingMail("device_1",tenantId);

        Mockito.verify(mailSender,times(1)).send(Mockito.any(MimeMessage.class));
    }

    @Test
    public void testSendAttributeMissingMailWithTwoTenantUser() throws TempusException {
        List<User> users = new ArrayList<>();
        users.add(tenantAdmin1);
        users.add(tenantAdmin2);
        TextPageData< User > userTextPageData = new TextPageData<>(users,null,false);

        when(userService.findTenantAdmins(Mockito.any(TenantId.class),Mockito.any(TextPageLink.class))).thenReturn(userTextPageData);
        when(messages.getMessage("attribute.missing.subject", null, Locale.US)).thenReturn("Tempus - parent_asset attribute");
        when(engine.mergeTemplate(Mockito.anyString(), Mockito.anyString(), Mockito.any(VelocityContext.class), Mockito.any(StringWriter.class))).thenReturn(true);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(Mockito.any(MimeMessage.class));

        defaultMailService.sendAttributeMissingMail("device_1",tenantId);

        Mockito.verify(mailSender,times(2)).send(Mockito.any(MimeMessage.class));
    }

    @Test(expected = IncorrectParameterException.class)
    public void testSendAttributeMissingMailWithInvalidTenantId() throws TempusException {
        List<User> users = new ArrayList<>();
        users.add(tenantAdmin1);

        TextPageData< User > userTextPageData = new TextPageData<>(users,null,false);

        tenantId = null;
        String exceptionMessage = INCORRECT_TENANT_ID + tenantId;
        when(userService.findTenantAdmins(Mockito.any(TenantId.class),Mockito.any(TextPageLink.class))).thenThrow(new IncorrectParameterException(exceptionMessage));

        defaultMailService.sendAttributeMissingMail("device_1",tenantId);

        Mockito.verify(mailSender,times(0)).send(Mockito.any(MimeMessage.class));
    }

}