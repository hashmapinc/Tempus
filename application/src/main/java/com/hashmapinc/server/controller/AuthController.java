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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hashmapinc.server.requests.ActivateUserRequest;
import com.hashmapinc.server.requests.IdentityUser;
import com.hashmapinc.server.requests.IdentityUserCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.web.bind.annotation.*;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.security.UserCredentials;
import com.hashmapinc.server.exception.TempusErrorCode;
import com.hashmapinc.server.exception.TempusException;
import com.hashmapinc.server.service.mail.MailService;
/*import com.hashmapinc.server.service.security.auth.jwt.RefreshTokenRepository;*/
import com.hashmapinc.server.service.security.model.SecurityUser;
import com.hashmapinc.server.service.security.model.UserPrincipal;
import org.springframework.web.client.RestTemplate;
//import com.hashmapinc.server.service.security.model.token.JwtToken;
//import com.hashmapinc.server.service.security.model.token.JwtTokenFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class AuthController extends BaseController {

    public static final String IDENTITY_ENDPOINT = "http://localhost:9002/uaa/users";

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("clientRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private ClientCredentialsResourceDetails apiResourceDetails;

    /*@Autowired
    private JwtTokenFactory tokenFactory;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;*/

    @Autowired
    private MailService mailService;

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/auth/user", method = RequestMethod.GET)
    public @ResponseBody User getUser() throws TempusException {
        try {
            SecurityUser securityUser = getCurrentUser();
            return userService.findUserById(securityUser.getId());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/auth/changePassword", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void changePassword (
            @RequestBody JsonNode changePasswordRequest) throws TempusException {
        try {
            String currentPassword = changePasswordRequest.get("currentPassword").asText();
            String newPassword = changePasswordRequest.get("newPassword").asText();
            SecurityUser securityUser = getCurrentUser();
            UserCredentials userCredentials = userService.findUserCredentialsByUserId(securityUser.getId());
            if (!passwordEncoder.matches(currentPassword, userCredentials.getPassword())) {
                throw new TempusException("Current password doesn't match!", TempusErrorCode.BAD_REQUEST_PARAMS);
            }
            userCredentials.setPassword(passwordEncoder.encode(newPassword));
            userService.saveUserCredentials(userCredentials);
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    @RequestMapping(value = "/noauth/activate", params = { "activateToken" }, method = RequestMethod.GET)
    public ResponseEntity<String> checkActivateToken(@RequestParam(value = "activateToken") String activateToken) throws JsonProcessingException, TempusException {
        HttpHeaders headers = new HttpHeaders();
        HttpStatus responseStatus;

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(IDENTITY_ENDPOINT+"/activate", JsonNode.class);

        if(response.getStatusCode().equals(HttpStatus.OK)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode body = response.getBody();
            UserCredentials userCredentials = mapper.treeToValue(body, IdentityUserCredentials.class).toUserCredentials();
            if(userCredentials != null) {
                String createURI = "/login/createPassword";
                try {
                    URI location = new URI(createURI + "?activateToken=" + activateToken);
                    headers.setLocation(location);
                    responseStatus = HttpStatus.SEE_OTHER;
                } catch (URISyntaxException e) {
                    log.error("Unable to create URI with address [{}]", createURI);
                    responseStatus = HttpStatus.BAD_REQUEST;
                }
            } else {
                responseStatus = HttpStatus.CONFLICT;
            }
        } else {
            throw new TempusException(response.getBody().asText(), TempusErrorCode.GENERAL);
        }

        return new ResponseEntity<>(headers, responseStatus);
    }

    @RequestMapping(value = "/noauth/resetPasswordByEmail", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void requestResetPasswordByEmail (
            @RequestBody JsonNode resetPasswordByEmailRequest,
            HttpServletRequest request) throws TempusException {
        try {

            ResponseEntity<JsonNode> userCredentialsResponse = restTemplate.postForEntity(IDENTITY_ENDPOINT+"/resetPasswordByEmail", resetPasswordByEmailRequest, JsonNode.class);

            if(userCredentialsResponse.getStatusCode().equals(HttpStatus.OK)) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode body = userCredentialsResponse.getBody();
                UserCredentials userCredentials = mapper.treeToValue(body, IdentityUserCredentials.class).toUserCredentials();
                String baseUrl = constructBaseUrl(request);


                String resetUrl = String.format("%s/api/noauth/resetPassword?resetToken=%s", baseUrl,
                        userCredentials.getResetToken());
                mailService.sendResetPasswordEmail(resetUrl, resetPasswordByEmailRequest.get("email").asText());
            } else {
                throw new TempusException("Error while resetting password by email", TempusErrorCode.GENERAL);
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @RequestMapping(value = "/noauth/resetPassword", params = { "resetToken" }, method = RequestMethod.GET)
    public ResponseEntity<String> checkResetToken(
            @RequestParam(value = "resetToken") String resetToken) throws TempusException {
        HttpHeaders headers = new HttpHeaders();
        HttpStatus responseStatus;
        String resetURI = "/login/resetPassword";
        ResponseEntity<IdentityUserCredentials> identityUserCredentialsResponse = restTemplate.getForEntity(IDENTITY_ENDPOINT+"/"+ resetToken+"/user-credentials", IdentityUserCredentials.class);
        if(identityUserCredentialsResponse.getStatusCode().equals(HttpStatus.OK)) {
            IdentityUserCredentials identityUserCredentials = identityUserCredentialsResponse.getBody();
            if (identityUserCredentials != null) {
                try {
                    URI location = new URI(resetURI + "?resetToken=" + resetToken);
                    headers.setLocation(location);
                    responseStatus = HttpStatus.SEE_OTHER;
                } catch (URISyntaxException e) {
                    log.error("Unable to create URI with address [{}]", resetURI);
                    responseStatus = HttpStatus.BAD_REQUEST;
                }
            } else {
                responseStatus = HttpStatus.CONFLICT;
            }
        } else {
            throw new TempusException("Error while retrieving user credentials for a reset token ", TempusErrorCode.GENERAL);
        }
        return new ResponseEntity<>(headers, responseStatus);
    }

    @RequestMapping(value = "/noauth/activate", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public JsonNode activateUser(
            @RequestBody JsonNode activateRequest,
            HttpServletRequest request) throws TempusException, JsonProcessingException {
        try {
            String activateToken = activateRequest.get("activateToken").asText();
            String password = activateRequest.get("password").asText();
            ActivateUserRequest activateUserRequest = ActivateUserRequest.builder().activateToken(activateToken).password(password).build();

            ResponseEntity<JsonNode> userCredentialsResponse = restTemplate.postForEntity(IDENTITY_ENDPOINT+"/activate", activateUserRequest, JsonNode.class);

            if(userCredentialsResponse.getStatusCode().equals(HttpStatus.OK)) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode body = userCredentialsResponse.getBody();
                UserCredentials userCredentials = mapper.treeToValue(body, IdentityUserCredentials.class).toUserCredentials();

                String userGetUrl = IDENTITY_ENDPOINT + "/" + userCredentials.getUserId().getId();
                ResponseEntity<IdentityUser> userResponse = restTemplate.getForEntity(userGetUrl, IdentityUser.class);
                if(userResponse.getStatusCode().equals(HttpStatus.OK)){
                    IdentityUser identityUser = userResponse.getBody();
                    String baseUrl = constructBaseUrl(request);
                    String loginUrl = String.format("%s/login", baseUrl);
                    String email = identityUser.getUserName();
                    try {
                        mailService.sendAccountActivatedEmail(loginUrl, email);
                    } catch (Exception e) {
                        log.info("Unable to send account activation email [{}]", e.getMessage());
                    }
                    OAuth2RestOperations oAuth2RestTemplate = getoAuth2RestOperations(password, identityUser);
                    ObjectMapper objectMapper = new ObjectMapper();
                    ObjectNode tokenObject = objectMapper.createObjectNode();
                    tokenObject.put("token", oAuth2RestTemplate.getAccessToken().getValue());
                    tokenObject.put("refreshToken", oAuth2RestTemplate.getAccessToken().getRefreshToken().getValue());
                    return tokenObject;
                } else {
                    throw new TempusException("Error while retrieving user", TempusErrorCode.GENERAL);
                }
            } else {
                throw new TempusException("Error while activating user", TempusErrorCode.GENERAL);
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private OAuth2RestOperations getoAuth2RestOperations(String password, IdentityUser identityUser) {
        ResourceOwnerPasswordResourceDetails userPasswordReq = new ResourceOwnerPasswordResourceDetails();
        userPasswordReq.setAccessTokenUri(apiResourceDetails.getAccessTokenUri());
        userPasswordReq.setClientId(apiResourceDetails.getClientId());
        userPasswordReq.setClientSecret(apiResourceDetails.getClientSecret());
        userPasswordReq.setScope(apiResourceDetails.getScope());
        userPasswordReq.setUsername(identityUser.getUserName());
        userPasswordReq.setPassword(password);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(identityUser.getUserName(), password));

        return new OAuth2RestTemplate(userPasswordReq);
    }

    @RequestMapping(value = "/noauth/resetPassword", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public JsonNode resetPassword(
            @RequestBody JsonNode resetPasswordRequest,
            HttpServletRequest request) throws TempusException {
        try {
            String resetToken = resetPasswordRequest.get("resetToken").asText();
            String password = resetPasswordRequest.get("password").asText();

            ResponseEntity<IdentityUserCredentials> identityUserCredentialsResponse = restTemplate.getForEntity(IDENTITY_ENDPOINT+"/"+ resetToken+"/user-credentials", IdentityUserCredentials.class);
            if(identityUserCredentialsResponse.getStatusCode().equals(HttpStatus.OK)) {
                UserCredentials userCredentials = identityUserCredentialsResponse.getBody().toUserCredentials();
                if(userCredentials !=null) {
                    userCredentials.setPassword(password);
                    userCredentials.setResetToken(null);

                    ResponseEntity<JsonNode> updatedUserCredentialsResponse = restTemplate.postForEntity(IDENTITY_ENDPOINT+"/user-credentials", new IdentityUserCredentials(userCredentials), JsonNode.class);
                    if(updatedUserCredentialsResponse.getStatusCode().equals(HttpStatus.OK)) {
                        String userGetUrl = IDENTITY_ENDPOINT + "/" + userCredentials.getUserId().getId();
                        ResponseEntity<IdentityUser> userResponse = restTemplate.getForEntity(userGetUrl, IdentityUser.class);
                        if(userResponse.getStatusCode().equals(HttpStatus.OK)){
                            IdentityUser identityUser = userResponse.getBody();
                            String baseUrl = constructBaseUrl(request);
                            String loginUrl = String.format("%s/login", baseUrl);
                            String email = identityUser.getUserName();

                            try {
                                mailService.sendPasswordWasResetEmail(loginUrl, email);
                            } catch (Exception e) {
                                log.info("Unable to send password  email [{}]", e.getMessage());
                            }

                            OAuth2RestOperations oAuth2RestTemplate = getoAuth2RestOperations(password, identityUser);
                            ObjectMapper objectMapper = new ObjectMapper();
                            ObjectNode tokenObject = objectMapper.createObjectNode();
                            tokenObject.put("token", oAuth2RestTemplate.getAccessToken().getValue());
                            tokenObject.put("refreshToken", oAuth2RestTemplate.getAccessToken().getRefreshToken().getValue());
                            return tokenObject;
                        } else {
                            throw new TempusException("Error retrieving user!", TempusErrorCode.GENERAL);
                        }
                    } else {
                        throw new TempusException("Error updating password!", TempusErrorCode.GENERAL);
                    }
                } else {
                    throw new TempusException("Invalid reset token!", TempusErrorCode.BAD_REQUEST_PARAMS);
                }

            } else {
                throw new TempusException("Error retrieving user credentials from reset token", TempusErrorCode.GENERAL);
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
