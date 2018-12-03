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
package com.hashmapinc.server.config;

import com.hashmapinc.server.dao.audit.AuditLogLevelFilter;
import com.hashmapinc.server.exception.TempusErrorResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


@Configuration
@EnableResourceServer
public class TempusResourceServer extends ResourceServerConfigurerAdapter {

    public static final String WEBJARS_ENTRY_POINT = "/webjars/**";
    public static final String DEVICE_API_ENTRY_POINT = "/api/v1/**";
    public static final String FORM_BASED_LOGIN_ENTRY_POINT = "/api/auth/login";
    public static final String TOKEN_REFRESH_ENTRY_POINT = "/api/auth/token";
    protected static final String[] NON_TOKEN_BASED_AUTH_ENTRY_POINTS = new String[] {"/index.html", "/static/**", "/api/noauth/**", "/api/logo/**", "/api/theming/**","/api/settings/getUserLogo/**", WEBJARS_ENTRY_POINT};
    public static final String TOKEN_BASED_AUTH_ENTRY_POINT = "/api/**";
    public static final String WS_TOKEN_BASED_AUTH_ENTRY_POINT = "/api/ws/**";


    @Autowired
    private ClientCredentialsResourceDetails apiResourceDetails;

    @Autowired
    @Lazy
    private TempusErrorResponseHandler restAccessDeniedHandler;

    @Autowired
    private ResourceServerProperties resourceServerProperties;

    public static final String JWT_TOKEN_HEADER_PARAM = "X-Authorization";


    @Autowired
    private UserAuthenticationConverter userTokenConverter;


    @Bean
    protected BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuditLogLevelFilter auditLogLevelFilter(AuditLogLevelProperties auditLogLevelProperties) {
        return new AuditLogLevelFilter(auditLogLevelProperties.getMask());
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(resourceServerProperties.getResourceId());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.headers().cacheControl().and().frameOptions().disable()
                .and()
                .cors()
                .and()
                .csrf().disable()
                .exceptionHandling()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(WEBJARS_ENTRY_POINT).permitAll() // Webjars
                .antMatchers(DEVICE_API_ENTRY_POINT).permitAll() // Device HTTP Transport API
                .antMatchers(FORM_BASED_LOGIN_ENTRY_POINT).permitAll() // Login end-point
                .antMatchers(TOKEN_REFRESH_ENTRY_POINT).permitAll() // Token refresh end-point
                .antMatchers(NON_TOKEN_BASED_AUTH_ENTRY_POINTS).permitAll() // static resources, user activation and password reset end-points
                .and()
                .authorizeRequests()
                .antMatchers(WS_TOKEN_BASED_AUTH_ENTRY_POINT).authenticated() // Protected WebSocket API End-points
                .antMatchers(TOKEN_BASED_AUTH_ENTRY_POINT).authenticated() // Protected API End-points
                .and()
                .exceptionHandling().accessDeniedHandler(restAccessDeniedHandler);

    }

    @Bean
    @Primary
    protected ResourceServerTokenServices tokenServices(){
        RemoteTokenServices services = new RemoteTokenServices();
        services.setClientId(resourceServerProperties.getClientId());
        services.setClientSecret(resourceServerProperties.getClientSecret());
        services.setCheckTokenEndpointUrl(resourceServerProperties.getTokenInfoUri());
        services.setAccessTokenConverter(tokenConverter());
        return services;
    }

    @Bean
    @ConditionalOnMissingBean(CorsFilter.class)
    public CorsFilter corsFilter(MvcCorsProperties mvcCorsProperties) {
        if (mvcCorsProperties.getMappings().size() == 0) {
            return new CorsFilter(new UrlBasedCorsConfigurationSource());
        } else {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.setCorsConfigurations(mvcCorsProperties.getMappings());
            return new CorsFilter(source);
        }
    }

    private AccessTokenConverter tokenConverter() {
        DefaultAccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();
        tokenConverter.setUserTokenConverter(userTokenConverter);
        return tokenConverter;
    }

}
