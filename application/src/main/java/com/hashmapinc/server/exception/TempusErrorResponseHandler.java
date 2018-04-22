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
package com.hashmapinc.server.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmapinc.server.service.security.exception.AuthMethodNotSupportedException;
import com.hashmapinc.server.service.security.exception.JwtExpiredTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Component
@Slf4j
public class TempusErrorResponseHandler implements AccessDeniedHandler {

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException,
            ServletException {
        if (!response.isCommitted()) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            mapper.writeValue(response.getWriter(),
                    TempusErrorResponse.of("You don't have permission to perform this operation!",
                            TempusErrorCode.PERMISSION_DENIED, HttpStatus.FORBIDDEN));
        }
    }

    public void handle(Exception exception, HttpServletResponse response) {
        log.debug("Processing exception {}", exception.getMessage(), exception);
        if (!response.isCommitted()) {
            try {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                if (exception instanceof TempusException) {
                    handleTempusException((TempusException) exception, response);
                } else if (exception instanceof AccessDeniedException) {
                    handleAccessDeniedException(response);
                } else if (exception instanceof AuthenticationException) {
                    handleAuthenticationException((AuthenticationException) exception, response);
                } else {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    mapper.writeValue(response.getWriter(), TempusErrorResponse.of(exception.getMessage(),
                            TempusErrorCode.GENERAL, HttpStatus.INTERNAL_SERVER_ERROR));
                }
            } catch (IOException e) {
                log.error("Can't handle exception", e);
            }
        }
    }

    private void handleTempusException(TempusException TempusException, HttpServletResponse response) throws IOException {

        TempusErrorCode errorCode = TempusException.getErrorCode();
        HttpStatus status;

        switch (errorCode) {
            case AUTHENTICATION:
                status = HttpStatus.UNAUTHORIZED;
                break;
            case PERMISSION_DENIED:
                status = HttpStatus.FORBIDDEN;
                break;
            case INVALID_ARGUMENTS:
                status = HttpStatus.BAD_REQUEST;
                break;
            case ITEM_NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                break;
            case BAD_REQUEST_PARAMS:
                status = HttpStatus.BAD_REQUEST;
                break;
            case GENERAL:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
        }

        response.setStatus(status.value());
        mapper.writeValue(response.getWriter(), TempusErrorResponse.of(TempusException.getMessage(), errorCode, status));
    }

    private void handleAccessDeniedException(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        mapper.writeValue(response.getWriter(),
                TempusErrorResponse.of("You don't have permission to perform this operation!",
                        TempusErrorCode.PERMISSION_DENIED, HttpStatus.FORBIDDEN));

    }

    private void handleAuthenticationException(AuthenticationException authenticationException, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        if (authenticationException instanceof BadCredentialsException) {
            mapper.writeValue(response.getWriter(), TempusErrorResponse.of("Invalid username or password", TempusErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
        } else if (authenticationException instanceof JwtExpiredTokenException) {
            mapper.writeValue(response.getWriter(), TempusErrorResponse.of("Token has expired", TempusErrorCode.JWT_TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED));
        } else if (authenticationException instanceof AuthMethodNotSupportedException) {
            mapper.writeValue(response.getWriter(), TempusErrorResponse.of(authenticationException.getMessage(), TempusErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
        }
        mapper.writeValue(response.getWriter(), TempusErrorResponse.of("Authentication failed", TempusErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
    }

}
