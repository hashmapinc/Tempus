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
package com.hashmapinc.server.service.security.auth.jwt;

import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.service.security.auth.JwtAuthenticationToken;
import com.hashmapinc.server.service.security.auth.jwt.extractor.TokenExtractor;
import com.hashmapinc.server.service.security.model.SecurityUser;
import com.hashmapinc.server.service.security.model.token.RawAccessJwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class JwtTokenAuthenticationProcessingFilter implements Filter{
    private final TokenExtractor tokenExtractor;
    private final ResourceServerProperties resourceServerProperties;

    @Autowired
    public JwtTokenAuthenticationProcessingFilter(TokenExtractor tokenExtractor, ResourceServerProperties resourceServerProperties) {
        this.tokenExtractor = tokenExtractor;
        this.resourceServerProperties = resourceServerProperties;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        /*
        * EMPTY
        * */
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        String tokenString = tokenExtractor.extract(request);

        if(tokenString !=null && !tokenString.isEmpty()) {
            RawAccessJwtToken token = new RawAccessJwtToken(tokenString);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("token", token.getToken());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", getAuthorizationHeader(resourceServerProperties.getClientId(), resourceServerProperties.getClientSecret()));
            Map map = (new RestTemplate()).exchange(resourceServerProperties.getTokenInfoUri(), HttpMethod.POST, new HttpEntity<>(formData, headers), HashMap.class).getBody();

            //todo more information needs to go it.
            SecurityUser securityUser = new SecurityUser();
            securityUser.setEmail(map.get("user_name").toString());
            securityUser.setAuthority(Authority.SYS_ADMIN);

            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(securityUser));
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        /*
         * EMPTY
         * */
    }

    private String getAuthorizationHeader(String clientId, String clientSecret) {
        String creds = String.format("%s:%s", clientId, clientSecret);
        try {
            return "Basic " + new String(Base64.encode(creds.getBytes("UTF-8")));
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not convert String");
        }
    }

}
