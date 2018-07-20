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
package com.hashmapinc.server.service.mail;

import com.hashmapinc.server.exception.TempusException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestMailService {

    public static String currentActivateToken;
    public static String currentResetPasswordToken;

    @Bean
    @Primary
    public MailService mailService() throws TempusException {
        MailService mailService = Mockito.mock(MailService.class);
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String activationLink = (String) args[0];
                currentActivateToken = activationLink.split("=")[1];
                return null;
            }
        }).when(mailService).sendActivationEmail(Mockito.anyString(), Mockito.anyString());
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String passwordResetLink = (String) args[0];
                currentResetPasswordToken = passwordResetLink.split("=")[1];
                return null;
            }
        }).when(mailService).sendResetPasswordEmail(Mockito.anyString(), Mockito.anyString());
        return mailService;
    }

}
