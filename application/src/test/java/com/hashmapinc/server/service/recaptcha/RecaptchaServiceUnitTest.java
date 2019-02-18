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
package com.hashmapinc.server.service.recaptcha;


import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecaptchaServiceUnitTest {

    @InjectMocks
    private RecaptchaService recaptchaService;

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void shouldReturnSuccess() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(new ResponseEntity(body, HttpStatus.OK));

        String captchaVerifyMessage = recaptchaService.verifyRecaptcha("reCaptchaResponse");

        Assert.assertEquals(StringUtils.EMPTY, captchaVerifyMessage);
    }

    @Test
    public void shouldReturnFailure() {
        List<String> errorCodes = new ArrayList<>();
        errorCodes.add("invalid-input-response");

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("error-codes",errorCodes);

        when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(new ResponseEntity(body, HttpStatus.OK));

        String captchaVerifyMessage = recaptchaService.verifyRecaptcha("reCaptchaResponse");

        Assert.assertEquals("The response parameter is invalid or malformed", captchaVerifyMessage);
    }
}
