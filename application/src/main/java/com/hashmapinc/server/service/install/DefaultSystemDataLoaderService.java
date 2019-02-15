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
package com.hashmapinc.server.service.install;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.AttributeKvEntry;
import com.hashmapinc.server.common.data.kv.BaseAttributeKvEntry;
import com.hashmapinc.server.common.data.kv.StringDataEntry;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleState;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.common.data.rule.RuleMetaData;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.common.data.security.DeviceCredentials;
import com.hashmapinc.server.common.data.security.UserCredentials;
import com.hashmapinc.server.common.data.widget.WidgetType;
import com.hashmapinc.server.common.data.widget.WidgetsBundle;
import com.hashmapinc.server.dao.attributes.AttributesService;
import com.hashmapinc.server.dao.customer.CustomerService;
import com.hashmapinc.server.dao.customergroup.CustomerGroupService;
import com.hashmapinc.server.dao.dashboard.DashboardService;
import com.hashmapinc.server.dao.device.DeviceCredentialsService;
import com.hashmapinc.server.dao.device.DeviceService;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.plugin.PluginService;
import com.hashmapinc.server.dao.rule.RuleService;
import com.hashmapinc.server.dao.settings.UserSettingsService;
import com.hashmapinc.server.dao.tenant.TenantService;
import com.hashmapinc.server.dao.theme.ThemeService;
import com.hashmapinc.server.dao.user.UserService;
import com.hashmapinc.server.dao.widget.WidgetTypeService;
import com.hashmapinc.server.dao.widget.WidgetsBundleService;
import com.hashmapinc.server.exception.TempusApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.hashmapinc.server.common.data.DataConstants.*;

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
    public static final String JSON_EXT = ".json";
    public static final String CUSTOMER_CRED = "customer";
    public static final String DEFAULT_DEVICE_TYPE = "default";

    private static final String SYS_ADMIN_GROUP = "SysAdmin Group";
    private static final String TENANT_GROUP = "Tenant Group";

    @Value("${install.data_dir}")
    private String dataDir;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSettingsService userSettingsService;

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

    @Autowired
    private ThemeService themeService;

    @Autowired
    private CustomerGroupService customerGroupService;

    @Bean
    protected BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Value("${ldap.admin-email}")
    private String adminEmail;

    @Value("${ldap.authentication-enabled}")
    private boolean isLdapEnabled;

    @Override
    public void createSysAdminWithGroupAndSettings() {
        if (userService.findUserByEmail(adminEmail) != null) {
            log.info("System admin already exists !!");
            return;
        }

        CustomerGroup adminGroup = createGroup(SYS_ADMIN_GROUP, Collections.singletonList(SYS_ADMIN_DEFAULT_PERMISSION), null, null);

        User adminUser;
        if(isLdapEnabled) {
            adminUser = createUser(Authority.SYS_ADMIN,null, null, adminEmail, null, true);
        } else {
            adminUser = createUser(Authority.SYS_ADMIN,null, null, adminEmail, "sysadmin", false);
        }
        assignUserToGroup(adminGroup, adminUser);
        createAdminSettings(adminUser);
    }

    private void createAdminSettings(User adminUser) {
        UserSettings generalSettings = new UserSettings();
        generalSettings.setKey("general");
        ObjectNode node = objectMapper.createObjectNode();
        node.put("baseUrl", "http://localhost:8080");
        generalSettings.setJsonValue(node);
        generalSettings.setUserId(adminUser.getId());
        userSettingsService.saveUserSettings(generalSettings);

        UserSettings mailSettings = new UserSettings();
        mailSettings.setKey("mail");
        node = objectMapper.createObjectNode();
        node.put("mailFrom", "tempus <sysadmin@localhost.localdomain>");
        node.put("smtpProtocol", "smtp");
        node.put("smtpHost", "localhost");
        node.put("smtpPort", "25");
        node.put("timeout", "10000");
        node.put("enableTls", "false");
        node.put("username", "");
        node.put("password", ""); //NOSONAR, key used to identify password field (not password value itself)
        mailSettings.setUserId(adminUser.getId());
        mailSettings.setJsonValue(node);
        userSettingsService.saveUserSettings(mailSettings);
    }

    public void loadSystemThemes() throws TempusApplicationException {

        List<Theme> theme = themeService.findAll();

        if(theme.size() < 2) {

            
            Theme theme1 = new Theme();
            theme1.setThemeName("Tempus Blue");
            theme1.setThemeValue("themeBlue");
            theme1.setThemeStatus(false);
            themeService.saveTheme(theme1);

            Theme theme2 = new Theme();
            theme2.setThemeName("Tempus Dark");
            theme2.setThemeValue("themeDark");
            theme2.setThemeStatus(true);
            themeService.saveTheme(theme2);

        }


    }

    @Override
    public void loadSystemWidgets() throws TempusApplicationException {
        Path widgetBundlesDir = Paths.get(dataDir, JSON_DIR, SYSTEM_DIR, WIDGET_BUNDLES_DIR);
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(widgetBundlesDir, path -> path.toString().endsWith(JSON_EXT))) {
            dirStream.forEach(
                    path -> {
                        try {
                            JsonNode widgetsBundleDescriptorJson = objectMapper.readTree(path.toFile());
                            JsonNode widgetsBundleJson = widgetsBundleDescriptorJson.get("widgetsBundle");
                            WidgetsBundle widgetsBundle = objectMapper.treeToValue(widgetsBundleJson, WidgetsBundle.class);
                            if (widgetsBundle.getTenantId() == null) {
                                widgetsBundle.setTenantId(new TenantId(ModelConstants.NULL_UUID));
                            }
                            WidgetsBundle existingWB = widgetsBundleService.findWidgetsBundleByTenantIdAndAlias(widgetsBundle.getTenantId(), widgetsBundle.getAlias());
                            WidgetsBundle savedWidgetsBundle = existingWB != null ? existingWB : widgetsBundleService.saveWidgetsBundle(widgetsBundle);
                            JsonNode widgetTypesArrayJson = widgetsBundleDescriptorJson.get("widgetTypes");
                            widgetTypesArrayJson.forEach(
                                    widgetTypeJson -> {
                                        try {
                                            WidgetType widgetType = objectMapper.treeToValue(widgetTypeJson, WidgetType.class);
                                            widgetType.setBundleAlias(savedWidgetsBundle.getAlias());
                                            if (widgetType.getTenantId() == null) {
                                                widgetType.setTenantId(new TenantId(ModelConstants.NULL_UUID));
                                            }
                                            WidgetType existingWT = widgetTypeService.findWidgetTypeByTenantIdBundleAliasAndAlias(widgetType.getTenantId(), savedWidgetsBundle.getAlias(), widgetType.getAlias());
                                            if (existingWT == null)
                                                widgetTypeService.saveWidgetType(widgetType);
                                            else
                                                log.info("Widget type '[{}]', already loaded from json: [{}]", widgetType.getName(), path.toString());
                                        } catch (Exception e) {
                                            log.error("Unable to load widget type from json: [{}]", path.toString());
                                        }
                                    }
                            );
                        } catch (Exception e) {
                            log.error("Unable to load widgets bundle from json: [{}]", path.toString());
                        }
                    }
            );
        } catch (IOException e) {
            throw new TempusApplicationException(e);
        }
    }

    @Override
    public void loadSystemPlugins() throws TempusApplicationException {
        try {
            loadPlugins(Paths.get(dataDir, JSON_DIR, SYSTEM_DIR, PLUGINS_DIR), null);
        } catch (IOException e) {
            throw new TempusApplicationException(e);
        }
    }


    @Override
    public void loadSystemRules() throws TempusApplicationException {
        try {
            loadRules(Paths.get(dataDir, JSON_DIR, SYSTEM_DIR, RULES_DIR), new TenantId(ModelConstants.NULL_UUID));
        } catch (IOException e) {
            throw new TempusApplicationException(e);
        }
    }

    @Override
    public void loadDemoData() throws TempusApplicationException {
        Tenant demoTenant = createDemoTenant();

        CustomerGroup tenantGroup = createGroup(TENANT_GROUP, Collections.singletonList(TENANT_ADMIN_DEFAULT_PERMISSION) , demoTenant.getId(), null);
        User tenantUser = createUser(Authority.TENANT_ADMIN, demoTenant.getId(), null, "demo@hashmapinc.com", "tenant", false);

        assignUserToGroup(tenantGroup, tenantUser);

        Customer customerA = createCustomer(demoTenant, "Drilling Team");

        List<String> customerPermissions = Arrays.asList(
                CUSTOMER_USER_DEFAULT_ASSET_READ_PERMISSION,
                CUSTOMER_USER_DEFAULT_ASSET_UPDATE_PERMISSION,
                CUSTOMER_USER_DEFAULT_DEVICE_READ_PERMISSION,
                CUSTOMER_USER_DEFAULT_DEVICE_UPDATE_PERMISSION
        );
        CustomerGroup customerGroupA = createGroup("Driller Group", customerPermissions, demoTenant.getId(), customerA.getId());
        User bobJones = createUser(Authority.CUSTOMER_USER, demoTenant.getId(), customerA.getId(), "bob.jones@hashmapinc.com", "driller", false);
        assignUserToGroup(customerGroupA, bobJones);

        List<AttributeKvEntry> attributesTank123 = new ArrayList<>();
        attributesTank123.add(new BaseAttributeKvEntry(new StringDataEntry("latitude", "52.330732"), DateTime.now().getMillis()));
        attributesTank123.add(new BaseAttributeKvEntry(new StringDataEntry("longitude", "-114.051973"), DateTime.now().getMillis()));

        List<AttributeKvEntry> attributesTank456 = new ArrayList<>();
        attributesTank456.add(new BaseAttributeKvEntry(new StringDataEntry("latitude", "52.317932"), DateTime.now().getMillis()));
        attributesTank456.add(new BaseAttributeKvEntry(new StringDataEntry("longitude", "-113.993608"), DateTime.now().getMillis()));

        createDevice(new DeviceUser(demoTenant.getId(), customerA.getId(), "Test_Token_Tank123"), "WaterTank", "Tank 123", null, false, attributesTank123);
        createDevice(new DeviceUser(demoTenant.getId(), customerA.getId(), "Test_Token_Tank456"), "WaterTank", "Tank 456", null, false, attributesTank456);
        createDevice(new DeviceUser(demoTenant.getId(), customerA.getId(), "GATEWAY_ACCESS_TOKEN"), "Gateway", "Spark Analytics Gateway", null, true, null);
        createDevice(new DeviceUser(demoTenant.getId(), customerA.getId(), "DEVICE_GATEWAY_TOKEN"), "Gateway", "Device Gateway", null, true, null);

        Customer customerB = createCustomer(demoTenant, "Customer B");
        Customer customerC = createCustomer(demoTenant, "Customer C");

        createDevice(demoTenant.getId(), customerA.getId(), DEFAULT_DEVICE_TYPE, "Test Device A1", "A1_TEST_TOKEN", null);
        createDevice(demoTenant.getId(), customerA.getId(), DEFAULT_DEVICE_TYPE, "Test Device A2", "A2_TEST_TOKEN", null);
        createDevice(demoTenant.getId(), customerA.getId(), DEFAULT_DEVICE_TYPE, "Test Device A3", "A3_TEST_TOKEN", null);
        createDevice(demoTenant.getId(), customerB.getId(), DEFAULT_DEVICE_TYPE, "Test Device B1", "B1_TEST_TOKEN", null);
        createDevice(demoTenant.getId(), customerC.getId(), DEFAULT_DEVICE_TYPE, "Test Device C1", "C1_TEST_TOKEN", null);

        createDevice(demoTenant.getId(), null, DEFAULT_DEVICE_TYPE, "DHT11 Demo Device", "DHT11_DEMO_TOKEN", "Demo device that is used in sample " +
                "applications that upload data from DHT11 temperature and humidity sensor");

        createDevice(demoTenant.getId(), null, DEFAULT_DEVICE_TYPE, "Raspberry Pi Demo Device", "RASPBERRY_PI_DEMO_TOKEN", "Demo device that is used in " +
                "Raspberry Pi GPIO control sample application");


        try {
            loadPlugins(Paths.get(dataDir, JSON_DIR, DEMO_DIR, PLUGINS_DIR), demoTenant.getId());
            loadRules(Paths.get(dataDir, JSON_DIR, DEMO_DIR, RULES_DIR), demoTenant.getId());
            loadDashboards(Paths.get(dataDir, JSON_DIR, DEMO_DIR, DASHBOARDS_DIR), demoTenant.getId(), null);
        } catch (IOException e) {
            throw new TempusApplicationException(e);
        }
    }

    private Tenant createDemoTenant() {
        Tenant demoTenant = new Tenant();
        String demoTenantName = "DemoTenant";
        List<Tenant> demoTenants = tenantService.findTenants(new TextPageLink(1, demoTenantName)).getData();
        if (demoTenants.size() == 0) {
            demoTenant.setRegion("Global");
            demoTenant.setTitle(demoTenantName);
            demoTenant = tenantService.saveTenant(demoTenant);
        } else {
            log.info("Tenant [{}] already exists !!", demoTenantName);
            demoTenant = demoTenants.get(0);
        }
        return demoTenant;
    }

    private Customer createCustomer(Tenant tenant, String title) {
        Optional<Customer> existingCustomer = customerService.findCustomerByTenantIdAndTitle(tenant.getId(), title);
        if (existingCustomer.isPresent()) {
            log.info("Customer [{}] already exists !!", title);
            return existingCustomer.get();
        }

        Customer customer = new Customer();
        customer.setTenantId(tenant.getId());
        customer.setTitle(title);
        customer = customerService.saveCustomer(customer);
        return customer;
    }

    private void assignUserToGroup(CustomerGroup group, User user) {
        List<CustomerGroup> groups = customerGroupService.findByUserId(user.getId(), new TextPageLink(100, group.getSearchText())).getData().stream().filter(cg -> cg.getId().equals(group.getId())).collect(Collectors.toList());
        if (groups.size() == 0)
            customerGroupService.assignUsers(group.getId(), Collections.singletonList(user.getId()));
        else
            log.info("User [{}] already assigned to Group [{}] !!", user.getEmail(), group.getName());
    }

    @Override
    public void deleteSystemWidgetBundle(String bundleAlias) throws TempusApplicationException {
        WidgetsBundle widgetsBundle = widgetsBundleService.findWidgetsBundleByTenantIdAndAlias(new TenantId(ModelConstants.NULL_UUID), bundleAlias);
        if (widgetsBundle != null) {
            widgetsBundleService.deleteWidgetsBundle(widgetsBundle.getId());
        }
    }

    private CustomerGroup createGroup(String title, List<String> permissions , TenantId tenantId , CustomerId customerId) {
        TenantId tenantIdForFind = tenantId == null ? new TenantId(ModelConstants.NULL_UUID) : tenantId;
        CustomerId customerIdForFind = customerId == null ? new CustomerId(ModelConstants.NULL_UUID) : customerId;
        Optional<CustomerGroup> existingGroup = customerGroupService.findCustomerByTenantIdAndCustomerIdAndTitle(tenantIdForFind, customerIdForFind, title);

        if (existingGroup.isPresent()) {
            log.info("Customer Group [{}] already exists !!", title);
            return existingGroup.get();
        } else {
            CustomerGroup customerGroup = new CustomerGroup();
            customerGroup.setTitle(title);
            customerGroup.setTenantId(tenantId);
            customerGroup.setCustomerId(customerId);
            customerGroup.setPolicies(permissions);
            return customerGroupService.saveCustomerGroup(customerGroup);
        }
    }

    private User createUser(Authority authority,
                            TenantId tenantId,
                            CustomerId customerId,
                            String email,
                            String password,
                            boolean isExternalUser) {

        User userByEmail = userService.findUserByEmail(email);
        if (userByEmail != null) {
            log.info("User with email id [{}] already created !!", email);
            return userByEmail;
        }

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
            userCredentials.setPassword(password);
            userCredentials.setEnabled(true);
            userCredentials.setActivateToken(null);
            userService.saveUserCredentials(userCredentials);
        }
        return user;
    }

    private Device createDevice(DeviceUser deviceUser, String type,
                                String name,
                                String description,
                                Boolean isGateway,
                                List<AttributeKvEntry> attributes) {

        Device existingDevice = deviceService.findDeviceByTenantIdAndName(deviceUser.getTenantId(), name);
        if (existingDevice != null) {
            log.info("Device [{}] already created !!", name);
            return existingDevice;
        }
        Device device = new Device();
        device.setTenantId(deviceUser.getTenantId());
        device.setCustomerId(deviceUser.getCustomerId());
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
            attributeService.save(device.getId(), SERVER_SCOPE, attributes);
        }

        DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(device.getId());
        deviceCredentials.setCredentialsId(deviceUser.getAccessToken());
        deviceCredentialsService.updateDeviceCredentials(deviceCredentials);
        return device;
    }

    private Device createDevice(TenantId tenantId,
                                CustomerId customerId,
                                String type,
                                String name,
                                String accessToken,
                                String description) {
        return createDevice(new DeviceUser(tenantId, customerId, accessToken), type, name, description, false, null);
    }

    private void loadPlugins(Path pluginsDir, TenantId tenantId) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(pluginsDir, path -> path.toString().endsWith(JSON_EXT))) {
            dirStream.forEach(
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
                        } catch (IncorrectParameterException e) {
                            log.info("Plugin already loaded: [{}]", path.toString());
                        } catch (Exception e) {
                            log.error("Unable to load plugin from json: [{}]", path.toString());
                        }
                    }
            );
        }
    }

    private void loadRules(Path rulesDir, TenantId tenantId) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(rulesDir, path -> path.toString().endsWith(JSON_EXT))) {
            dirStream.forEach(
                    path -> {
                        try {
                            JsonNode ruleJson = objectMapper.readTree(path.toFile());
                            RuleMetaData rule = objectMapper.treeToValue(ruleJson, RuleMetaData.class);
                            rule.setTenantId(tenantId);
                            List<RuleMetaData> existingRule = ruleService.findAllTenantRulesByTenantId(tenantId).stream().filter(r -> r.getName().equals(rule.getName())).collect(Collectors.toList());
                            if (existingRule.isEmpty()) {
                                if (rule.getState() == ComponentLifecycleState.ACTIVE) {
                                    rule.setState(ComponentLifecycleState.SUSPENDED);
                                    RuleMetaData savedRule = ruleService.saveRule(rule);
                                    ruleService.activateRuleById(savedRule.getId());
                                } else {
                                    ruleService.saveRule(rule);
                                }
                            } else {
                                log.info("Rule already loaded: [{}]", path.toString());
                            }
                        } catch (Exception e) {
                            log.error("Unable to load rule from json: [{}]", path.toString());
                        }
                    }
            );
        }
    }

    private void loadDashboards(Path dashboardsDir, TenantId tenantId, CustomerId customerId) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dashboardsDir, path -> path.toString().endsWith(JSON_EXT))) {
            dirStream.forEach(
                    path -> {
                        try {
                            JsonNode dashboardJson = objectMapper.readTree(path.toFile());
                            Dashboard dashboard = objectMapper.treeToValue(dashboardJson, Dashboard.class);
                            dashboard.setTenantId(tenantId);
                            Dashboard savedDashboard = dashboardService.saveDashboard(dashboard);
                            if (customerId != null && !customerId.isNullUid()) {
                                dashboardService.assignDashboardToCustomer(savedDashboard.getId(), customerId);
                            }
                        } catch (DataValidationException e) {
                            log.info("Dashboard already loaded: [{}]", path.toString());
                        } catch (Exception e) {
                            log.error("Unable to load dashboard from json: [{}]", path.toString());
                        }
                    }
            );
        }
    }

    private static class DeviceUser {
        private final TenantId tenantId;
        private final CustomerId customerId;
        private final String accessToken;

        private DeviceUser(TenantId tenantId, CustomerId customerId, String accessToken) {
            this.tenantId = tenantId;
            this.customerId = customerId;
            this.accessToken = accessToken;
        }

        public TenantId getTenantId() {
            return tenantId;
        }

        public CustomerId getCustomerId() {
            return customerId;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }
}
