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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecaptchaService {

    @Value("${google-recaptcha.secret_key}")
    private String recaptchaSecret;

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @Autowired
    @Qualifier("clientRestTemplate")
    private RestTemplate restTemplate;


    public String verifyRecaptcha(String recaptchaResponse){
        Map<String, String> body = new HashMap<>();
        body.put("secret", recaptchaSecret);
        body.put("response", recaptchaResponse);
        log.debug("Request body for recaptcha: {}", body);

        ResponseEntity<Map> recaptchaResponseEntity =
                restTemplate.postForEntity(GOOGLE_RECAPTCHA_VERIFY_URL +"?secret=" + recaptchaSecret +"&response=" + recaptchaResponse,body,Map.class);

        log.info("Response from recaptcha: {}", recaptchaResponseEntity);

        if(recaptchaResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            Map<String, Object> responseBody = recaptchaResponseEntity.getBody();
            boolean recaptchaSucess = (Boolean) responseBody.get("success");
            if (!recaptchaSucess) {
                List<String> errorCodes = (List) responseBody.get("error-codes");
                String errorMessage = errorCodes.stream()
                        .map(s -> RecaptchaUtil.RECAPTCHA_ERROR_CODE.get(s))
                        .collect(Collectors.joining(", "));
                return errorMessage;
            } else {
                return StringUtils.EMPTY;
            }
        }
        return StringUtils.EMPTY;
    }
}

