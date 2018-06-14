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

import com.hashmapinc.server.service.security.auth.rest.LoginRequest;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;

import java.util.Arrays;

public class Test1 {
    public static void main(String[] args) {

        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();

        resource.setAccessTokenUri("http://localhost:9002/uaa/oauth/token");
        resource.setClientId("tempus");
        resource.setClientSecret("tempus");
        resource.setGrantType("password");
        resource.setScope(Arrays.asList("server"));
        resource.setUsername("jetinder1@hashmapinc.com");
        resource.setPassword("demo");


        OAuth2RestOperations oAuth2RestTemplate  = new OAuth2RestTemplate(resource);


        System.out.println(oAuth2RestTemplate.getAccessToken().getValue());
        System.out.println(oAuth2RestTemplate.getAccessToken().getRefreshToken().getValue());

    }
}
