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

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.exception.TempusException;
import com.hashmapinc.server.service.security.auth.jwt.RefreshTokenRequest;
import com.hashmapinc.server.service.security.auth.rest.LoginRequest;
import com.hashmapinc.server.service.security.auth.rest.LoginResponseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class LoginController extends BaseController {

    public static final String REFRESH_TOKEN = "refresh_token";
    @Autowired
    private ClientCredentialsResourceDetails apiResourceDetails;

    @Autowired
    @Qualifier("clientRestTemplate")
    OAuth2RestTemplate restTemplate;

    @PostMapping(value = "/auth/login")
    @ResponseBody
    public LoginResponseToken loginUser(@RequestBody LoginRequest loginRequest) throws TempusException {
        try {
            ResourceOwnerPasswordResourceDetails userPasswordReq = new ResourceOwnerPasswordResourceDetails();
            userPasswordReq.setAccessTokenUri(apiResourceDetails.getAccessTokenUri());
            userPasswordReq.setClientId(apiResourceDetails.getClientId());
            userPasswordReq.setClientSecret(apiResourceDetails.getClientSecret());
            userPasswordReq.setScope(apiResourceDetails.getScope());
            userPasswordReq.setUsername(loginRequest.getUsername());
            userPasswordReq.setPassword(loginRequest.getPassword());

            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            OAuth2RestOperations oAuth2RestTemplate = new OAuth2RestTemplate(userPasswordReq);
            return new LoginResponseToken(oAuth2RestTemplate.getAccessToken().getValue(), oAuth2RestTemplate.getAccessToken().getRefreshToken().getValue());
        }catch (Exception e){
            throw handleException(e);
        }

    }

    @PostMapping(value = "/auth/token")
    @ResponseBody
    public LoginResponseToken refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) throws TempusException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
            requestParams.add("grant_type", REFRESH_TOKEN);
            requestParams.add(REFRESH_TOKEN, refreshTokenRequest.getRefreshToken());
            HttpEntity<MultiValueMap<String, String>> postRequest = new HttpEntity<>(requestParams, headers);
            ResponseEntity<JsonNode> responseEntity =
                    restTemplate.postForEntity(apiResourceDetails.getAccessTokenUri(), postRequest, JsonNode.class);
            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                JsonNode body = responseEntity.getBody();
                return new LoginResponseToken(body.get("access_token").asText(), body.get(REFRESH_TOKEN).asText());
            }
            return null;
        }catch (Exception e){
            throw handleException(e);
        }
    }
}
