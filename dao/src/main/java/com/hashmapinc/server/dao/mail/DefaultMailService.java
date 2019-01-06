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

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.UserSettings;
import com.hashmapinc.server.common.data.exception.TempusErrorCode;
import com.hashmapinc.server.common.data.exception.TempusException;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.settings.UserSettingsService;
import com.hashmapinc.server.dao.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.NestedRuntimeException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

@Service
@Slf4j
public class DefaultMailService implements MailService {

    public static final String MAIL_PROP = "mail.";
    public static final String TARGET_EMAIL = "targetEmail";
    public static final String UTF_8 = "UTF-8";
    private static final String DEVICE_NAME = "deviceName";
    private static final String ASSET_NAME = "assetName";
    @Autowired
    private MessageSource messages;
    
    @Autowired
    @Qualifier("velocityEngine")
    private VelocityEngine engine;
    
    private JavaMailSenderImpl mailSender;
    
    private String mailFrom;
    
    @Autowired
    private UserSettingsService userSettingsService;

    @Value("${ldap.admin-email}")
    private String adminEmail;

    @Autowired
    private UserService userService;

    @PostConstruct
    private void init() {
        updateMailConfiguration();
    }

    @Bean
    @Qualifier("velocityEngine")
    public VelocityEngine velocityEngine() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("input.encoding", "UTF-8");
        properties.setProperty("output.encoding", "UTF-8");
        properties.setProperty("resource.loader", "classpath");
        properties.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        VelocityEngine velocityEngine = new VelocityEngine(properties);
        return velocityEngine;
    }

    @Override
    public void updateMailConfiguration() {
        User adminUser = userService.findUserByEmail(adminEmail);
        UserSettings settings = userSettingsService.findUserSettingsByKeyAndUserId("mail", adminUser.getId());
        if (settings != null) {
            JsonNode jsonConfig = settings.getJsonValue();
            mailSender = createMailSender(jsonConfig);
            mailFrom = jsonConfig.get("mailFrom").asText();
        } else {
            throw new IncorrectParameterException("Failed to date mail configuration. Settings not found!");
        }
    }
    
    private JavaMailSenderImpl createMailSender(JsonNode jsonConfig) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(jsonConfig.get("smtpHost").asText());
        javaMailSender.setPort(parsePort(jsonConfig.get("smtpPort").asText()));
        javaMailSender.setUsername(jsonConfig.get("username").asText());
        javaMailSender.setPassword(jsonConfig.get("password").asText());
        javaMailSender.setJavaMailProperties(createJavaMailProperties(jsonConfig));
        return javaMailSender;
    }

    private Properties createJavaMailProperties(JsonNode jsonConfig) {
        Properties javaMailProperties = new Properties();
        String protocol = jsonConfig.get("smtpProtocol").asText();
        javaMailProperties.put("mail.transport.protocol", protocol);
        javaMailProperties.put(MAIL_PROP + protocol + ".host", jsonConfig.get("smtpHost").asText());
        javaMailProperties.put(MAIL_PROP + protocol + ".port", jsonConfig.get("smtpPort").asText());
        javaMailProperties.put(MAIL_PROP + protocol + ".timeout", jsonConfig.get("timeout").asText());
        javaMailProperties.put(MAIL_PROP + protocol + ".auth", String.valueOf(StringUtils.isNotEmpty(jsonConfig.get("username").asText())));
        javaMailProperties.put(MAIL_PROP + protocol + ".starttls.enable", jsonConfig.has("enableTls") ? jsonConfig.get("enableTls").asText() : "false");
        return javaMailProperties;
    }
    
    private int parsePort(String strPort) {
        try {
            return Integer.valueOf(strPort);
        } catch (NumberFormatException e) {
            throw new IncorrectParameterException(String.format("Invalid smtp port value: %s", strPort));
        }
    }

    @Override
    public void sendEmail(String email, String subject, String message) throws TempusException {
        sendMail(mailSender, mailFrom, email, subject, message);
    }
    
    @Override
    public void sendTestMail(JsonNode jsonConfig, String email) throws TempusException {
        JavaMailSenderImpl testMailSender = createMailSender(jsonConfig);
        String from = jsonConfig.get("mailFrom").asText();
        String subject = messages.getMessage("test.message.subject", null, Locale.US);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(TARGET_EMAIL, email);

        String message = mergeVelocityTemplate("templates/test.vm", velocityContext);

        sendMail(testMailSender, from, email, subject, message);
    }

    @Override
    public void sendActivationEmail(String activationLink, String email) throws TempusException {
        
        String subject = messages.getMessage("activation.subject", null, Locale.US);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("activationLink", activationLink);
        velocityContext.put(TARGET_EMAIL, email);

        String message = mergeVelocityTemplate("templates/activation.vm", velocityContext);

        sendMail(mailSender, mailFrom, email, subject, message); 
    }
    
    @Override
    public void sendAccountActivatedEmail(String loginLink, String email) throws TempusException {
        
        String subject = messages.getMessage("account.activated.subject", null, Locale.US);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("loginLink", loginLink);
        velocityContext.put(TARGET_EMAIL, email);

        String message = mergeVelocityTemplate("templates/account.activated.vm", velocityContext);

        sendMail(mailSender, mailFrom, email, subject, message); 
    }

    @Override
    public void sendResetPasswordEmail(String passwordResetLink, String email) throws TempusException {
        
        String subject = messages.getMessage("reset.password.subject", null, Locale.US);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("passwordResetLink", passwordResetLink);
        velocityContext.put(TARGET_EMAIL, email);

        String message = mergeVelocityTemplate("templates/reset.password.vm", velocityContext);

        sendMail(mailSender, mailFrom, email, subject, message); 
    }
    
    @Override
    public void sendPasswordWasResetEmail(String loginLink, String email) throws TempusException {
        
        String subject = messages.getMessage("password.was.reset.subject", null, Locale.US);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("loginLink", loginLink);
        velocityContext.put(TARGET_EMAIL, email);

        String message = mergeVelocityTemplate("templates/password.was.reset.vm", velocityContext);

        sendMail(mailSender, mailFrom, email, subject, message); 
    }

    @Override
    public void sendAttributeMissingMail(String deviceName, TenantId tenantId) throws TempusException {
        TextPageData<User> userTextPageData = userService.findTenantAdmins(tenantId,new TextPageLink(300));
        List<User> users = userTextPageData.getData();

        String subject = messages.getMessage("attribute.missing.subject", null, Locale.US);
        subject = subject + " of " + deviceName + " missing";

        for (User user : users) {
            String email = user.getEmail();
            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put(DEVICE_NAME,deviceName);
            velocityContext.put(TARGET_EMAIL, email);
            String message = mergeVelocityTemplate("templates/attribute.missing.vm", velocityContext);

            sendMail(mailSender, mailFrom, email, subject, message);
        }
    }

    @Override
    public void sendAssetNotPresentMail(String deviceName, String assetName , TenantId tenantId) throws TempusException {
        TextPageData<User> userTextPageData = userService.findTenantAdmins(tenantId,new TextPageLink(300));
        List<User> users = userTextPageData.getData();

        String subject = messages.getMessage("asset.message.subject", null, Locale.US);
        subject = subject + " " + assetName + " of " + deviceName + " is absent";

        for (User user : users) {
            String email = user.getEmail();
            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put(TARGET_EMAIL, email);
            velocityContext.put(DEVICE_NAME,deviceName);
            velocityContext.put(ASSET_NAME,assetName);
            String message = mergeVelocityTemplate("templates/asset.absent.vm", velocityContext);

            sendMail(mailSender, mailFrom, email, subject, message);
        }
    }

    private void sendMail(JavaMailSenderImpl mailSender,
                          String mailFrom, String email,
                          String subject, String message) throws TempusException {
        try {
            MimeMessage mimeMsg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, UTF_8);
            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(message, true);
            mailSender.send(helper.getMimeMessage());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private String mergeVelocityTemplate(String template, VelocityContext velocityContext) {
        StringWriter stringWriter = new StringWriter();
        this.engine.mergeTemplate(template, UTF_8, velocityContext, stringWriter);
        return stringWriter.toString();
    }

    protected TempusException handleException(Exception exception) {
        String message;
        if (exception instanceof NestedRuntimeException) {
            message = ((NestedRuntimeException)exception).getMostSpecificCause().getMessage();
        } else {
            message = exception.getMessage();
        }
        return new TempusException(String.format("Unable to send mail: %s", message),
                TempusErrorCode.GENERAL);
    }

}
