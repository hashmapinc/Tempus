/**
 * Copyright © 2016-2017 The Thingsboard Authors
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

package org.thingsboard.server.service.install;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.*;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleState;
import org.thingsboard.server.common.data.plugin.PluginMetaData;
import org.thingsboard.server.common.data.rule.RuleMetaData;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.common.data.widget.WidgetType;
import org.thingsboard.server.common.data.widget.WidgetsBundle;
import org.thingsboard.server.dao.attributes.AttributesService;
import org.thingsboard.server.dao.customer.CustomerService;
import org.thingsboard.server.dao.dashboard.DashboardService;
import org.thingsboard.server.dao.device.DeviceCredentialsService;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.plugin.PluginService;
import org.thingsboard.server.dao.rule.RuleService;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.dao.tenant.TenantService;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.dao.widget.WidgetTypeService;
import org.thingsboard.server.dao.widget.WidgetsBundleService;
import org.thingsboard.server.extensions.core.plugin.telemetry.AttributeData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("install")
@Slf4j
public class DefaultSystemDataLoaderService implements SystemDataLoaderService {

    private static final String JSON_DIR = "json";
    private static final String SYSTEM_DIR = "system";
    private static final String DEMO_DIR = "demo";
    private static final String WIDGET_BUNDLES_DIR = "widget_bundles";
    private static final String PLUGINS_DIR = "plugins";
    private static final String RULES_DIR = "rules";
    private static final String DASHBOARDS_DIR = "dashboards";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${install.data_dir}")
    private String dataDir;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminSettingsService adminSettingsService;

    @Autowired
    private WidgetsBundleService widgetsBundleService;

    @Autowired
    private WidgetTypeService widgetTypeService;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AttributesService attributeService;

    @Autowired
    private DeviceCredentialsService deviceCredentialsService;

    @Autowired
    private DashboardService dashboardService;

    @Bean
    protected BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Value("${ldap.admin-email}")
    private String adminEmail;

    @Value("${ldap.authentication-enabled}")
    private boolean isLdapEnabled;

    @Override
    public void createSysAdmin() {
        if(isLdapEnabled) {
            createUser(Authority.SYS_ADMIN, null, null, adminEmail, null, true);
        } else {
            createUser(Authority.SYS_ADMIN, null, null, "sysadmin@hashmapinc.com", "sysadmin", false);
        }
    }

    @Override
    public void createAdminSettings() throws Exception {
        AdminSettings generalSettings = new AdminSettings();
        generalSettings.setKey("general");
        ObjectNode node = objectMapper.createObjectNode();
        node.put("baseUrl", "http://localhost:8080");
        generalSettings.setJsonValue(node);
        adminSettingsService.saveAdminSettings(generalSettings);

        AdminSettings mailSettings = new AdminSettings();
        mailSettings.setKey("mail");
        node = objectMapper.createObjectNode();
        node.put("mailFrom", "ThingsBoard <sysadmin@localhost.localdomain>");
        node.put("smtpProtocol", "smtp");
        node.put("smtpHost", "localhost");
        node.put("smtpPort", "25");
        node.put("timeout", "10000");
        node.put("enableTls", "false");
        node.put("username", "");
        node.put("password", "");
        mailSettings.setJsonValue(node);
        adminSettingsService.saveAdminSettings(mailSettings);
    }

    @Override
    public void loadSystemWidgets() throws Exception {
        Path widgetBundlesDir = Paths.get(dataDir, JSON_DIR, SYSTEM_DIR, WIDGET_BUNDLES_DIR);
        Files.newDirectoryStream(widgetBundlesDir, path -> path.toString().endsWith(".json"))
                .forEach(
                        path -> {
                            try {
                                JsonNode widgetsBundleDescriptorJson = objectMapper.readTree(path.toFile());
                                JsonNode widgetsBundleJson = widgetsBundleDescriptorJson.get("widgetsBundle");
                                WidgetsBundle widgetsBundle = objectMapper.treeToValue(widgetsBundleJson, WidgetsBundle.class);
                                WidgetsBundle savedWidgetsBundle = widgetsBundleService.saveWidgetsBundle(widgetsBundle);
                                JsonNode widgetTypesArrayJson = widgetsBundleDescriptorJson.get("widgetTypes");
                                widgetTypesArrayJson.forEach(
                                        widgetTypeJson -> {
                                            try {
                                                WidgetType widgetType = objectMapper.treeToValue(widgetTypeJson, WidgetType.class);
                                                widgetType.setBundleAlias(savedWidgetsBundle.getAlias());
                                                widgetTypeService.saveWidgetType(widgetType);
                                            } catch (Exception e) {
                                                log.error("Unable to load widget type from json: [{}]", path.toString());
                                                throw new RuntimeException("Unable to load widget type from json", e);
                                            }
                                        }
                                );
                            } catch (Exception e) {
                                log.error("Unable to load widgets bundle from json: [{}]", path.toString());
                                throw new RuntimeException("Unable to load widgets bundle from json", e);
                            }
                        }
                );
    }

    @Override
    public void loadSystemPlugins() throws Exception {
        loadPlugins(Paths.get(dataDir, JSON_DIR, SYSTEM_DIR, PLUGINS_DIR), null);
    }


    @Override
    public void loadSystemRules() throws Exception {
        loadRules(Paths.get(dataDir, JSON_DIR, SYSTEM_DIR, RULES_DIR), null);
    }

    @Override
    public void loadDemoData() throws Exception {
        Tenant demoTenant = new Tenant();
        demoTenant.setRegion("Global");
        demoTenant.setTitle("DemoTenant");
        demoTenant = tenantService.saveTenant(demoTenant);
        createUser(Authority.TENANT_ADMIN, demoTenant.getId(), null, "demo@hashmapinc.com", "tenant", false);

        Customer customerA = new Customer();
        customerA.setTenantId(demoTenant.getId());
        customerA.setTitle("Drilling Team");
        customerA = customerService.saveCustomer(customerA);

        createUser(Authority.CUSTOMER_USER, demoTenant.getId(), customerA.getId(), "bob.jones@hashmapinc.com", "driller", false);

        List<AttributeKvEntry> attributesTank123 = new ArrayList<>();
        attributesTank123.add(new BaseAttributeKvEntry(new StringDataEntry("latitude", "52.330732"), DateTime.now().getMillis()));
        attributesTank123.add(new BaseAttributeKvEntry(new StringDataEntry("longitude", "-114.051973"), DateTime.now().getMillis()));

        List<AttributeKvEntry> attributesTank456 = new ArrayList<>();
        attributesTank456.add(new BaseAttributeKvEntry(new StringDataEntry("latitude", "52.317932"), DateTime.now().getMillis()));
        attributesTank456.add(new BaseAttributeKvEntry(new StringDataEntry("longitude", "-113.993608"), DateTime.now().getMillis()));

        createDevice(demoTenant.getId(), customerA.getId(), "WaterTank", "Tank 123", "Test_Token_Tank123", null, false, attributesTank123);
        createDevice(demoTenant.getId(), customerA.getId(), "WaterTank", "Tank 456", "Test_Token_Tank456", null, false, attributesTank456);
        createDevice(demoTenant.getId(), customerA.getId(), "Gateway", "Spark Analytics Gateway", "GATEWAY_ACCESS_TOKEN", null, true, null);

        loadPlugins(Paths.get(dataDir, JSON_DIR, DEMO_DIR, PLUGINS_DIR), demoTenant.getId());
        loadRules(Paths.get(dataDir, JSON_DIR, DEMO_DIR, RULES_DIR), demoTenant.getId());
        loadDashboards(Paths.get(dataDir, JSON_DIR, DEMO_DIR, DASHBOARDS_DIR), demoTenant.getId(), null);
    }

    @Override
    public void deleteSystemWidgetBundle(String bundleAlias) throws Exception {
        WidgetsBundle widgetsBundle = widgetsBundleService.findWidgetsBundleByTenantIdAndAlias(new TenantId(ModelConstants.NULL_UUID), bundleAlias);
        if (widgetsBundle != null) {
            widgetsBundleService.deleteWidgetsBundle(widgetsBundle.getId());
        }
    }

    private User createUser(Authority authority,
                            TenantId tenantId,
                            CustomerId customerId,
                            String email,
                            String password,
                            boolean isExternalUser) {
        User user = new User();
        user.setAuthority(authority);
        user.setEmail(email);
        user.setTenantId(tenantId);
        user.setCustomerId(customerId);
        if(isExternalUser) {
            user = userService.saveExternalUser(user);
        } else {
            user = userService.saveUser(user);
            UserCredentials userCredentials = userService.findUserCredentialsByUserId(user.getId());
            userCredentials.setPassword(passwordEncoder.encode(password));
            userCredentials.setEnabled(true);
            userCredentials.setActivateToken(null);
            userService.saveUserCredentials(userCredentials);
        }
        return user;
    }

    private Device createDevice(TenantId tenantId,
                                CustomerId customerId,
                                String type,
                                String name,
                                String accessToken,
                                String description,
                                Boolean isGateway,
                                List<AttributeKvEntry> attributes) {
        Device device = new Device();
        device.setTenantId(tenantId);
        device.setCustomerId(customerId);
        device.setType(type);
        device.setName(name);
        if (isGateway || description != null){
            ObjectNode additionalInfo = objectMapper.createObjectNode();
            if (isGateway) {
                additionalInfo.put("gateway", true);
            }
            if (description != null){
                additionalInfo.put("description", description);
            }
            device.setAdditionalInfo(additionalInfo);
        }
        device = deviceService.saveDevice(device);

        if (attributes != null){
            attributeService.save(device.getId(), DataConstants.SERVER_SCOPE, attributes);
        }

        DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(device.getId());
        deviceCredentials.setCredentialsId(accessToken);
        deviceCredentialsService.updateDeviceCredentials(deviceCredentials);
        return device;
    }

    private Device createDevice(TenantId tenantId,
                                CustomerId customerId,
                                String type,
                                String name,
                                String accessToken,
                                String description) {
        return createDevice(tenantId, customerId, type, name, accessToken, description, false, null);
    }

    private void loadPlugins(Path pluginsDir, TenantId tenantId) throws Exception{
        Files.newDirectoryStream(pluginsDir, path -> path.toString().endsWith(".json"))
                .forEach(
                        path -> {
                            try {
                                JsonNode pluginJson = objectMapper.readTree(path.toFile());
                                PluginMetaData plugin = objectMapper.treeToValue(pluginJson, PluginMetaData.class);
                                plugin.setTenantId(tenantId);
                                if (plugin.getState() == ComponentLifecycleState.ACTIVE) {
                                    plugin.setState(ComponentLifecycleState.SUSPENDED);
                                    PluginMetaData savedPlugin = pluginService.savePlugin(plugin);
                                    pluginService.activatePluginById(savedPlugin.getId());
                                } else {
                                    pluginService.savePlugin(plugin);
                                }
                            } catch (Exception e) {
                                log.error("Unable to load plugin from json: [{}]", path.toString());
                                throw new RuntimeException("Unable to load plugin from json", e);
                            }
                        }
                );

    }

    private void loadRules(Path rulesDir, TenantId tenantId) throws Exception {
        Files.newDirectoryStream(rulesDir, path -> path.toString().endsWith(".json"))
                .forEach(
                        path -> {
                            try {
                                JsonNode ruleJson = objectMapper.readTree(path.toFile());
                                RuleMetaData rule = objectMapper.treeToValue(ruleJson, RuleMetaData.class);
                                rule.setTenantId(tenantId);
                                if (rule.getState() == ComponentLifecycleState.ACTIVE) {
                                    rule.setState(ComponentLifecycleState.SUSPENDED);
                                    RuleMetaData savedRule = ruleService.saveRule(rule);
                                    ruleService.activateRuleById(savedRule.getId());
                                } else {
                                    ruleService.saveRule(rule);
                                }
                            } catch (Exception e) {
                                log.error("Unable to load rule from json: [{}]", path.toString());
                                throw new RuntimeException("Unable to load rule from json", e);
                            }
                        }
                );
    }

    private void loadDashboards(Path dashboardsDir, TenantId tenantId, CustomerId customerId) throws Exception {
        Files.newDirectoryStream(dashboardsDir, path -> path.toString().endsWith(".json"))
                .forEach(
                        path -> {
                            try {
                                JsonNode dashboardJson = objectMapper.readTree(path.toFile());
                                Dashboard dashboard = objectMapper.treeToValue(dashboardJson, Dashboard.class);
                                dashboard.setTenantId(tenantId);
                                dashboard.setCustomerId(customerId);
                                dashboardService.saveDashboard(dashboard);
                            } catch (Exception e) {
                                log.error("Unable to load dashboard from json: [{}]", path.toString());
                                throw new RuntimeException("Unable to load dashboard from json", e);
                            }
                        }
                );
    }
}
