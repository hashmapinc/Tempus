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
package com.hashmapinc.server.controller;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.page.TimePageLink;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.requests.ActivateUserRequest;
import com.hashmapinc.server.requests.CreateUserRequest;
import com.hashmapinc.server.requests.IdentityUser;
import com.hashmapinc.server.requests.IdentityUserCredentials;
import com.hashmapinc.server.service.computation.CloudStorageService;
import com.hashmapinc.server.service.mail.TestMailService;
import com.hashmapinc.server.service.security.auth.jwt.RefreshTokenRequest;
import com.hashmapinc.server.service.security.auth.rest.LoginRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Context;
import javax.naming.directory.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.hashmapinc.server.common.data.DataConstants.*;
import static com.hashmapinc.server.dao.model.ModelConstants.NULL_UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AbstractControllerTest.class, loader = SpringBootContextLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Configuration
@ComponentScan({"com.hashmapinc.server"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public abstract class AbstractControllerTest {

    @Value("${ldap.authentication-enabled}")
    protected boolean ldapEnabled;

    @Value("${ldap.authentication-server}")
    protected String ldapURL;

    protected static final String TEST_TENANT_NAME = "TEST TENANT";
    protected static final String TEST_TENANT_LOGO = "TEST LOGO";

    protected static final String SYS_ADMIN_EMAIL = "sysadmin@hashmapinc.com";
    private static final String SYS_ADMIN_PASSWORD = "sysadmin";

    protected static final String TENANT_ADMIN_EMAIL = "testtenant@tempus.org";
    private static final String TENANT_ADMIN_PASSWORD = "tenant";

    protected static final String CUSTOMER_USER_EMAIL = "testcustomer@tempus.org";
    private static final String CUSTOMER_USER_PASSWORD = "customer";

    /** See {@link org.springframework.test.web.servlet.DefaultMvcResult#getAsyncResult(long)}
     *  and {@link org.springframework.mock.web.MockAsyncContext#getTimeout()}
     */
    private static final long DEFAULT_TIMEOUT = -1L;

    protected MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    protected MediaType contentTypeFile = new MediaType(MediaType.MULTIPART_FORM_DATA.getType());

    protected MockMvc mockMvc;

    protected String token;
    protected String refreshToken;
    protected String username;

    protected TenantId tenantId;
    protected Tenant savedTenant;
    protected User tenantAdmin;
    protected CustomerGroup tenantGroup;

    protected Customer savedCustomer;
    protected User customerUser;
    protected CustomerGroup customerGroup;

    @SuppressWarnings("rawtypes")
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @SuppressWarnings("rawtypes")
    private HttpMessageConverter stringHttpMessageConverter;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            log.info("Starting test: {}", description.getMethodName());
        }

        protected void finished(Description description) {
            log.info("Finished test: {}", description.getMethodName());
        }
    };

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .get();

        this.stringHttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof StringHttpMessageConverter)
                .findAny()
                .get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        log.info("Executing setup");
        if (this.mockMvc == null) {
            this.mockMvc = webAppContextSetup(webApplicationContext)
                    .apply(springSecurity()).build();
        }
        if(ldapEnabled) {
            createLDAPEntry(SYS_ADMIN_EMAIL, SYS_ADMIN_PASSWORD);
            createLDAPEntry(TENANT_ADMIN_EMAIL, TENANT_ADMIN_PASSWORD);
            createLDAPEntry(CUSTOMER_USER_EMAIL, CUSTOMER_USER_PASSWORD);
        }

        stubForFetchUsers();
        log.info("Logging in as admin");
        loginSysAdmin();
        log.info("Logged in as sysadmin");

        Tenant tenant = new Tenant();
        tenant.setTitle(TEST_TENANT_NAME);
        tenant.setLogo(TEST_TENANT_LOGO);
        savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();

        tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(tenantId);
        tenantAdmin.setEmail(TENANT_ADMIN_EMAIL);

        stubUser(tenantAdmin, TENANT_ADMIN_PASSWORD);
        tenantAdmin = createUserAndLogin(tenantAdmin, TENANT_ADMIN_PASSWORD);

        tenantGroup = new CustomerGroup();
        tenantGroup.setTitle("Test Tenant Group");
        tenantGroup.setTenantId(tenantId);
        tenantGroup.setCustomerId(null);
        tenantGroup.setPolicies(Collections.singletonList(TENANT_ADMIN_DEFAULT_PERMISSION));
        tenantGroup = doPost("/api/customer/group", tenantGroup, CustomerGroup.class);
        doPost("/api/customer/group/" + tenantGroup.getId()+"/users", Collections.singletonList(tenantAdmin.getId().getId()));

        Customer customer = new Customer();
        customer.setTitle("Customer");
        customer.setTenantId(tenantId);
        savedCustomer = doPost("/api/customer", customer, Customer.class);

        customerUser = new User();
        customerUser.setAuthority(Authority.CUSTOMER_USER);
        customerUser.setTenantId(tenantId);
        customerUser.setCustomerId(savedCustomer.getId());
        customerUser.setEmail(CUSTOMER_USER_EMAIL);

        customerGroup = new CustomerGroup();
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(savedCustomer.getId());
        customerGroup.setTitle("Test Customer Group");
        customerGroup.setPolicies(Arrays.asList(
                CUSTOMER_USER_DEFAULT_ASSET_READ_PERMISSION,
                CUSTOMER_USER_DEFAULT_ASSET_UPDATE_PERMISSION,
                CUSTOMER_USER_DEFAULT_DEVICE_READ_PERMISSION,
                CUSTOMER_USER_DEFAULT_DEVICE_UPDATE_PERMISSION)
        );

        customerGroup = doPost("/api/customer/group", customerGroup, CustomerGroup.class);
        stubUser(customerUser, CUSTOMER_USER_PASSWORD);

        customerUser = createUserAndLogin(customerUser, CUSTOMER_USER_PASSWORD);
        doPost("/api/customer/group/"+ customerGroup.getId() + "/users", Collections.singletonList(customerUser.getId().getId()));

        logout();
        log.info("Executed setup");
    }

    @After
    public void teardown() throws Exception {
        log.info("Executing teardown");
        loginSysAdmin();
        doDelete("/api/tenant/" + tenantId.getId().toString())
                .andExpect(status().isOk());
        if(ldapEnabled) {
            deleteLDAPEntry(SYS_ADMIN_EMAIL);
            deleteLDAPEntry(TENANT_ADMIN_EMAIL);
            deleteLDAPEntry(CUSTOMER_USER_EMAIL);
        }
        log.info("Executed teardown");
    }

    @Bean
    @Primary
    public CloudStorageService nameService() {
        return Mockito.mock(CloudStorageService.class);
    }

    protected void stubUser(User user, String password) throws IOException {
        String activationToken = RandomStringUtils.randomAlphanumeric(30);
        String accessToken = RandomStringUtils.randomAlphanumeric(30);
        String refreshToken = RandomStringUtils.randomAlphanumeric(30);
        user = stubForCreateUser(user, activationToken);
        stubForCredentialsByToken(activationToken, user.getId().getId());
        stubForFetchUserById(user);
        stubForCheckToken(accessToken, user);
        stubForUserActivation(activationToken, password, user);
        stubForUserLogin(user, password, accessToken, refreshToken);
    }

    protected void loginSysAdmin() throws Exception {
        login(SYS_ADMIN_EMAIL, SYS_ADMIN_PASSWORD);
    }

    protected void loginTenantAdmin() throws Exception {
        login(TENANT_ADMIN_EMAIL, TENANT_ADMIN_PASSWORD);
    }

    protected void loginCustomerUser() throws Exception {
        login(CUSTOMER_USER_EMAIL, CUSTOMER_USER_PASSWORD);
    }

    protected User createUserAndLogin(User user, String password) throws Exception {

        User savedUser = doPost("/api/user?activationType=mail", user, User.class);
        savedUser.setTenantId(user.getTenantId());
        savedUser.setCustomerId(user.getCustomerId());
        logout();
        doGet("/api/noauth/activate?activateToken={activateToken}", TestMailService.currentActivateToken)
                .andExpect(status().isSeeOther())
                .andExpect(header().string(HttpHeaders.LOCATION, "/login/createPassword?activateToken=" + TestMailService.currentActivateToken));
        JsonNode activateRequest = new ObjectMapper().createObjectNode()
                .put("activateToken", TestMailService.currentActivateToken)
                .put("password", password);
        JsonNode tokenInfo = readResponse(doPost("/api/noauth/activate", activateRequest).andExpect(status().isOk()), JsonNode.class);
        validateAndSetJwtToken(tokenInfo, user.getEmail());
        return savedUser;
    }

    protected void deleteLDAPEntry(String email) throws Exception{
        DirContext ldapContext = getAdminLDAPContext();
        ldapContext.destroySubcontext("uid="+email+",dc=example,dc=org");
    }

    private DirContext getAdminLDAPContext() throws Exception{
        String adminPwd = "admin";
        String conntype = "simple";
        String AdminDn  = "cn=admin,dc=example,dc=org";

        Hashtable<String, String> environment = new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL,ldapURL);
        environment.put(Context.SECURITY_AUTHENTICATION,conntype);
        environment.put(Context.SECURITY_PRINCIPAL,AdminDn);
        environment.put(Context.SECURITY_CREDENTIALS, adminPwd);
        return new InitialDirContext(environment);
    }

    protected void createLDAPEntry(String email, String password) throws Exception{
        DirContext ldapContext = getAdminLDAPContext();

        String entryDN = "uid="+email+",dc=example,dc=org";

        Attribute userPassword = new BasicAttribute("userpassword",password);
        Attribute uid = new BasicAttribute("uid",email);
        Attribute oc = new BasicAttribute("objectClass");
        oc.add("account");
        oc.add("simpleSecurityObject");
        oc.add("top");
        BasicAttributes entry = new BasicAttributes();
        entry.put(uid);
        entry.put(userPassword);
        entry.put(oc);
        ldapContext.createSubcontext(entryDN, entry);
    }

    protected User stubForCreateUser(User user, String activationToken) throws IOException {
        UUID userId = UUIDs.timeBased();
        IdentityUser identityUser = new IdentityUser(user);
        CreateUserRequest userRequest = CreateUserRequest.builder().user(identityUser).activationType("mail").build();
        String request = mapper.writeValueAsString(userRequest);
        IdentityUser createdUser = new IdentityUser(user);
        createdUser.setId(userId);
        createdUser.setClientId("tempus");
        ObjectNode response = mapper.createObjectNode();
        response.set("user", mapper.readTree(mapper.writeValueAsString(createdUser)));
        response.put("activationToken", activationToken);
        createdUser.setEnabled(true);
        stubFor(WireMock.post(urlPathEqualTo("/uaa/users"))
                .withRequestBody(equalToJson(request))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response.toString())));
        return createdUser.toUser();
    }

    protected void stubForUserActivation(String token, String password, User user) throws JsonProcessingException {
        ActivateUserRequest request = ActivateUserRequest.builder().activateToken(token).password(password).build();
        ObjectNode response = mapper.createObjectNode();
        response.put("id", user.getId().getId().toString());
        response.put("userId", user.getId().getId().toString());
        stubFor(WireMock.post(urlPathEqualTo("/uaa/users/activate"))
                .withRequestBody(equalToJson(mapper.writeValueAsString(request)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response.toString())));
    }

    protected void stubForCredentialsByToken(String token, UUID userId) throws JsonProcessingException {
        IdentityUserCredentials credentials = new IdentityUserCredentials();
        credentials.setActivationToken(token);
        credentials.setId(userId);
        credentials.setUserId(userId);
        stubFor(get(urlPathEqualTo("/uaa/users/activate/"+token+"/user-credentials"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(credentials))));
    }

    protected void stubForFetchUsers() throws JsonProcessingException {
        TextPageData<User> users = new TextPageData<>(Collections.emptyList(), new TextPageLink(100));
        stubFor(
                get(urlPathMatching("/uaa/users/list"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(mapper.writeValueAsString(users)))
        );
    }

    protected void stubForFetchUserById(User user) throws JsonProcessingException {
        stubFor(get(urlPathEqualTo("/uaa/users/"+user.getId().getId().toString()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(new IdentityUser(user)))));
    }

    protected void stubForUserLogin(User user, String password, String accessToken, String refreshToken) throws UnsupportedEncodingException, JsonProcessingException {
        Map<String, Object> additionalDetails = new HashMap<>();
        additionalDetails.put("iat", System.currentTimeMillis() / 1000);
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(accessToken);
        token.setRefreshToken(new DefaultOAuth2RefreshToken(refreshToken));
        token.setScope(new HashSet<>(Arrays.asList("server")));
        token.setAdditionalInformation(additionalDetails);

        String body = mapper.writeValueAsString(token);
        stubFor(WireMock.post(urlPathEqualTo("/uaa/oauth/token"))
                .withRequestBody(equalTo("grant_type=password&username="+ URLEncoder.encode(user.getEmail(), "UTF-8")+ "&password="+password+"&scope=server"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    protected void stubForCheckToken(String accessToken, User user) throws JsonProcessingException {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId().getId());
        response.put("user_name", user.getEmail());
        response.put("tenant_id", user.getTenantId().getId());
        response.put("customer_id", user.getCustomerId().getId());
        response.put("scope", Arrays.asList("server"));
        response.put("authorities", Arrays.asList(user.getAuthority().name()));
        response.put("permissions", user.getPermissions());
        response.put("enabled", true);
        response.put("active", true);
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());

        stubFor(WireMock.post(urlPathEqualTo("/uaa/oauth/check_token"))
                .withRequestBody(equalTo("token="+accessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(response))));
    }

    protected void login(String username, String password) throws Exception {
        this.token = null;
        this.refreshToken = null;
        this.username = null;
        JsonNode tokenInfo = readResponse(doPost("/api/auth/login", new LoginRequest(username, password)).andExpect(status().isOk()), JsonNode.class);
        validateAndSetJwtToken(tokenInfo, username);
    }

    protected void refreshToken() throws Exception {
        this.token = null;
        JsonNode tokenInfo = readResponse(doPost("/api/auth/token", new RefreshTokenRequest(this.refreshToken)).andExpect(status().isOk()), JsonNode.class);

        validateAndSetJwtToken(tokenInfo, this.username);
    }

    protected void validateAndSetJwtToken(JsonNode tokenInfo, String username) {
        Assert.assertNotNull(tokenInfo);
        Assert.assertTrue(tokenInfo.has("token"));
        Assert.assertTrue(tokenInfo.has("refreshToken"));
        String token = tokenInfo.get("token").asText();
        String refreshToken = tokenInfo.get("refreshToken").asText();
        //validateJwtToken(token, username);
        //validateJwtToken(refreshToken, username);
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
    }

    protected void validateJwtToken(String token, String username) {
        Assert.assertNotNull(token);
        Assert.assertFalse(token.isEmpty());
        int i = token.lastIndexOf('.');
        Assert.assertTrue(i > 0);
        String withoutSignature = token.substring(0, i + 1);
        Jwt<Header, Claims> jwsClaims = Jwts.parser().parseClaimsJwt(withoutSignature);
        Claims claims = jwsClaims.getBody();
        String subject = claims.get("user_name", String.class);
        Assert.assertEquals(username, subject);
    }

    protected void logout() throws Exception {
        this.token = null;
        this.refreshToken = null;
        this.username = null;
    }

    protected void setJwtToken(MockHttpServletRequestBuilder request) {
        if (this.token != null) {
            request.header("Authorization", "Bearer " + this.token);
        }
    }

    protected ResultActions doGet(String urlTemplate, Object... urlVariables) throws Exception {
        MockHttpServletRequestBuilder getRequest = get(urlTemplate, urlVariables);
        setJwtToken(getRequest);
        return mockMvc.perform(getRequest);
    }

    protected <T> T doGet(String urlTemplate, Class<T> responseClass, Object... urlVariables) throws Exception {
        return readResponse(doGet(urlTemplate, urlVariables).andExpect(status().isOk()), responseClass);
    }

    protected <T> T doGetAsync(String urlTemplate, Class<T> responseClass, Object... urlVariables) throws Exception {
        return readResponse(doGetAsync(urlTemplate, urlVariables).andExpect(status().isOk()), responseClass);
    }

    protected ResultActions doGetAsync(String urlTemplate, Object... urlVariables) throws Exception {
        MockHttpServletRequestBuilder getRequest;
        getRequest = get(urlTemplate, urlVariables);
        setJwtToken(getRequest);
        return mockMvc.perform(asyncDispatch(mockMvc.perform(getRequest).andExpect(request().asyncStarted()).andReturn()));
    }

    protected <T> T doGetTyped(String urlTemplate, TypeReference<T> responseType, Object... urlVariables) throws Exception {
        return readResponse(doGet(urlTemplate, urlVariables).andExpect(status().isOk()), responseType);
    }

    protected <T> T doGetTypedWithPageLink(String urlTemplate, TypeReference<T> responseType,
                                           TextPageLink pageLink,
                                           Object... urlVariables) throws Exception {
        List<Object> pageLinkVariables = new ArrayList<>();
        urlTemplate += "limit={limit}";
        pageLinkVariables.add(pageLink.getLimit());
        if (StringUtils.isNotEmpty(pageLink.getTextSearch())) {
            urlTemplate += "&textSearch={textSearch}";
            pageLinkVariables.add(pageLink.getTextSearch());
        }
        if (pageLink.getIdOffset() != null) {
            urlTemplate += "&idOffset={idOffset}";
            pageLinkVariables.add(pageLink.getIdOffset().toString());
        }
        if (StringUtils.isNotEmpty(pageLink.getTextOffset())) {
            urlTemplate += "&textOffset={textOffset}";
            pageLinkVariables.add(pageLink.getTextOffset());
        }

        Object[] vars = new Object[urlVariables.length + pageLinkVariables.size()];
        System.arraycopy(urlVariables, 0, vars, 0, urlVariables.length);
        System.arraycopy(pageLinkVariables.toArray(), 0, vars, urlVariables.length, pageLinkVariables.size());

        return readResponse(doGet(urlTemplate, vars).andExpect(status().isOk()), responseType);
    }

    protected <T> T  doGetTypedWithTimePageLink(String urlTemplate, TypeReference<T> responseType,
                                                TimePageLink pageLink,
                                                Object... urlVariables) throws Exception {
        List<Object> pageLinkVariables = new ArrayList<>();
        urlTemplate += "limit={limit}";
        pageLinkVariables.add(pageLink.getLimit());
        if (pageLink.getStartTime() != null) {
            urlTemplate += "&startTime={startTime}";
            pageLinkVariables.add(pageLink.getStartTime());
        }
        if (pageLink.getEndTime() != null) {
            urlTemplate += "&endTime={endTime}";
            pageLinkVariables.add(pageLink.getEndTime());
        }
        if (pageLink.getIdOffset() != null) {
            urlTemplate += "&offset={offset}";
            pageLinkVariables.add(pageLink.getIdOffset().toString());
        }
        if (pageLink.isAscOrder()) {
            urlTemplate += "&ascOrder={ascOrder}";
            pageLinkVariables.add(pageLink.isAscOrder());
        }
        Object[] vars = new Object[urlVariables.length + pageLinkVariables.size()];
        System.arraycopy(urlVariables, 0, vars, 0, urlVariables.length);
        System.arraycopy(pageLinkVariables.toArray(), 0, vars, urlVariables.length, pageLinkVariables.size());

        return readResponse(doGet(urlTemplate, vars).andExpect(status().isOk()), responseType);
    }

    protected <T> T doPost(String urlTemplate, Class<T> responseClass, String... params) throws Exception {
        return readResponse(doPost(urlTemplate, params).andExpect(status().isOk()), responseClass);
    }

    protected <T> T doPost(String urlTemplate, T content, Class<T> responseClass, ResultMatcher resultMatcher, String... params) throws Exception {
        return readResponse(doPost(urlTemplate, content, params).andExpect(resultMatcher), responseClass);
    }

    protected <T> T doPost(String urlTemplate, T content, Class<T> responseClass, String... params) throws Exception {
        return readResponse(doPost(urlTemplate, content, params).andExpect(status().isOk()), responseClass);
    }

    protected <T, R> R doPostFile(String urlTemplate, T content, Class<R> responseClass, String... params) throws Exception {
        return readResponse(doPostFile(urlTemplate, content, params).andExpect(status().isOk()), responseClass);
    }

    protected <T, R> R doPostWithDifferentResponse(String urlTemplate, T content, Class<R> responseClass, String... params) throws Exception {
        return readResponse(doPost(urlTemplate, content, params).andExpect(status().isOk()), responseClass);
    }

    protected <T, R> R doPutWithDifferentResponse(String urlTemplate, T content, Class<R> responseClass, String... params) throws Exception {
        return readResponse(doPut(urlTemplate, content, params).andExpect(status().isOk()), responseClass);
    }

    protected <T> T doPostAsync(String urlTemplate, T content, Class<T> responseClass, ResultMatcher resultMatcher, String... params) throws Exception {
        return readResponse(doPostAsync(urlTemplate, content, DEFAULT_TIMEOUT, params).andExpect(resultMatcher), responseClass);
    }

    protected <T> T doPostAsync(String urlTemplate, T content, Class<T> responseClass, ResultMatcher resultMatcher, Long timeout, String... params) throws Exception {
        return readResponse(doPostAsync(urlTemplate, content, timeout, params).andExpect(resultMatcher), responseClass);
    }

    protected <T> T doDelete(String urlTemplate, Class<T> responseClass, String... params) throws Exception {
        return readResponse(doDelete(urlTemplate, params).andExpect(status().isOk()), responseClass);
    }

    protected ResultActions doPost(String urlTemplate, String... params) throws Exception {
        MockHttpServletRequestBuilder postRequest = post(urlTemplate);
        setJwtToken(postRequest);
        populateParams(postRequest, params);
        return mockMvc.perform(postRequest);
    }

    protected <T> ResultActions doPost(String urlTemplate, T content, String... params) throws Exception {
        MockHttpServletRequestBuilder postRequest = post(urlTemplate);
        setJwtToken(postRequest);
        String json = json(content);
        postRequest.contentType(contentType).content(json);
        return mockMvc.perform(postRequest);
    }

    protected <T> ResultActions doPostFile(String urlTemplate, T content, String... params) throws Exception {
        MockMultipartFile file = (MockMultipartFile) content;
        MockHttpServletRequestBuilder postRequest = MockMvcRequestBuilders.multipart(urlTemplate).file(file);
        setJwtToken(postRequest);
        return mockMvc.perform(postRequest);
    }

    protected <T> ResultActions doPut(String urlTemplate, T content, String... params) throws Exception {
        MockHttpServletRequestBuilder postRequest = put(urlTemplate);
        setJwtToken(postRequest);
        String json = json(content);
        postRequest.contentType(contentType).content(json);
        return mockMvc.perform(postRequest);
    }



    protected <T> ResultActions doPostAsync(String urlTemplate, T content, Long timeout, String... params)  throws Exception {

        MockHttpServletRequestBuilder postRequest = post(urlTemplate);
        setJwtToken(postRequest);
        String json = json(content);
        postRequest.contentType(contentType).content(json);
        MvcResult result = mockMvc.perform(postRequest).andReturn();
        result.getAsyncResult(timeout);
        return mockMvc.perform(asyncDispatch(result));
    }

    protected ResultActions doDelete(String urlTemplate, String... params) throws Exception {
        MockHttpServletRequestBuilder deleteRequest = delete(urlTemplate);
        setJwtToken(deleteRequest);
        populateParams(deleteRequest, params);
        return mockMvc.perform(deleteRequest);
    }

    protected void populateParams(MockHttpServletRequestBuilder request, String... params) {
        if (params != null && params.length > 0) {
            Assert.assertEquals(0, params.length % 2);
            MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
            for (int i = 0; i < params.length; i += 2) {
                paramsMap.add(params[i], params[i + 1]);
            }
            request.params(paramsMap);
        }
    }

    @SuppressWarnings("unchecked")
    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();

        HttpMessageConverter converter = o instanceof String ? stringHttpMessageConverter : mappingJackson2HttpMessageConverter;
        converter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    @SuppressWarnings("unchecked")
    protected <T> T readResponse(ResultActions result, Class<T> responseClass) throws Exception {
        byte[] content = result.andReturn().getResponse().getContentAsByteArray();
        MockHttpInputMessage mockHttpInputMessage = new MockHttpInputMessage(content);
        HttpMessageConverter converter = responseClass.equals(String.class) ? stringHttpMessageConverter : mappingJackson2HttpMessageConverter;
        return (T) converter.read(responseClass, mockHttpInputMessage);
    }

    protected <T> T readResponse(ResultActions result, TypeReference<T> type) throws Exception {
        byte[] content = result.andReturn().getResponse().getContentAsByteArray();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readerFor(type).readValue(content);
    }

    public class IdComparator<D extends BaseData<? extends UUIDBased>> implements Comparator<D> {
        @Override
        public int compare(D o1, D o2) {
            return o1.getId().getId().compareTo(o2.getId().getId());
        }
    }

    protected static <T> ResultMatcher statusReason(Matcher<T> matcher) {
        return jsonPath("$.message", matcher);
    }

    protected void assignUserToGroup(UserId customerUserId, CustomerGroup savedCustomerGroup) throws Exception {
        loginTenantAdmin();
        doPost("/api/customer/group/"+savedCustomerGroup.getId().getId().toString()+"/users", Collections.singletonList(customerUserId.getId().toString()))
                .andExpect(status().isOk());
        logout();
    }

    protected void unAssignUserFromGroup(CustomerGroupId savedCustomerGroupId, UserId userId) throws Exception {
        loginTenantAdmin();
        doPut("/api/customer/group/"+savedCustomerGroupId.getId().toString()+"/users", Collections.singletonList(userId.getId().toString()))
                .andExpect(status().isOk());
        logout();
    }

    protected UserId getCustomerUserId() throws Exception {
        loginCustomerUser();
        User user = doGet("/api/auth/user", User.class);
        logout();
        return user.getId();
    }

    protected CustomerGroup createGroupWithPolicies(List<String> policies, CustomerId customerId, String my_customer_group) throws Exception {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTitle(my_customer_group);
        customerGroup.setTenantId(tenantId);
        customerGroup.setCustomerId(customerId);
        customerGroup.setPolicies(policies);
        return doPost("/api/customer/group", customerGroup, CustomerGroup.class);
    }

    protected CustomerGroup updateGroupWithPolicies(List<String> policies, CustomerGroup customerGroup) throws Exception {
        loginTenantAdmin();
        customerGroup.setPolicies(policies);
        CustomerGroup customerGroupReturned = doPost("/api/customer/group", customerGroup, CustomerGroup.class);
        logout();
        return customerGroupReturned;
    }

    protected void deleteGroup(CustomerGroupId customerGroupId) throws Exception {
        doDelete("/api/customer/group/"+customerGroupId.getId().toString())
                .andExpect(status().isOk());
    }


    protected DataModel createDataModel() throws Exception{
        DataModel dataModel = new DataModel();
        dataModel.setName("Default Drilling Data Model1");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());
        DataModel savedDataModel = doPost("/api/data-model", dataModel, DataModel.class);
        Assert.assertNotNull(savedDataModel);
        Assert.assertNotNull(savedDataModel.getId());
        Assert.assertTrue(savedDataModel.getCreatedTime() > 0);
        Assert.assertEquals(savedTenant.getId(), savedDataModel.getTenantId());
        Assert.assertEquals(dataModel.getName(), savedDataModel.getName());
        Assert.assertTrue(savedDataModel.getLastUpdatedTs() > 0);
        return savedDataModel;
    }

    protected DataModelObject createDataModelObject(DataModel dataModel, String name, String type) throws Exception{
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName(name);
        dataModelObject.setType(type);

        AttributeDefinition ad = new AttributeDefinition();
        ad.setValueType("STRING");
        ad.setName("attr name2");
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(ad);
        dataModelObject.setAttributeDefinitions(attributeDefinitions);

        DataModelObject savedDataModelObj = doPost("/api/data-model/" + dataModel.getId().toString() + "/objects", dataModelObject, DataModelObject.class);
        Assert.assertNotNull(savedDataModelObj);
        Assert.assertEquals(dataModel.getId(), savedDataModelObj.getDataModelId());
        return savedDataModelObj;
    }

    protected void deleteDataModelObject(DataModelObjectId dataModelObjectId) throws Exception {
        doDelete("/api/data-model/objects/"+dataModelObjectId.getId().toString())
                .andExpect(status().isOk());
    }

    protected Asset createAsset(DataModelObjectId dataModelObjectId, CustomerId customerId, String assetName) throws Exception {
        Asset asset = new Asset();
        asset.setName(assetName);
        asset.setType("default");
        asset.setDataModelObjectId(dataModelObjectId);
        asset.setTenantId(tenantId);
        asset.setCustomerId(customerId);
        Asset savedAsset = doPost("/api/asset", asset, Asset.class);
        Assert.assertNotNull(savedAsset);
        Assert.assertNotNull(savedAsset.getId());
        Assert.assertTrue(savedAsset.getCreatedTime() > 0);
        Assert.assertEquals(savedTenant.getId(), savedAsset.getTenantId());
        Assert.assertNotNull(savedAsset.getCustomerId());
        if(customerId == null){
            Assert.assertEquals(NULL_UUID, savedAsset.getCustomerId().getId());
        }else{
            Assert.assertEquals(customerId, savedAsset.getCustomerId());
        }
        Assert.assertEquals(asset.getName(), savedAsset.getName());
        return savedAsset;
    }

    protected void deleteAsset(AssetId assetId) throws Exception {
        doDelete("/api/asset/"+assetId.getId().toString())
                .andExpect(status().isOk());
    }

    protected Device createDevice(DataModelObjectId dataModelObjectId, CustomerId customerId, String deviceName) throws Exception {
        Device device = new Device();
        device.setName(deviceName);
        device.setType("default");
        device.setDataModelObjectId(dataModelObjectId);
        device.setTenantId(tenantId);
        device.setCustomerId(customerId);
        Device savedDevice = doPost("/api/device", device, Device.class);
        Assert.assertNotNull(savedDevice);
        Assert.assertNotNull(savedDevice.getId());
        Assert.assertTrue(savedDevice.getCreatedTime() > 0);
        Assert.assertEquals(savedTenant.getId(), savedDevice.getTenantId());
        Assert.assertNotNull(savedDevice.getCustomerId());
        if(customerId == null){
            Assert.assertEquals(NULL_UUID, savedDevice.getCustomerId().getId());
        }else{
            Assert.assertEquals(customerId, savedDevice.getCustomerId());
        }
        Assert.assertEquals(device.getName(), savedDevice.getName());
        return savedDevice;
    }

    protected void deleteDevice(DeviceId deviceId) throws Exception {
        doDelete("/api/device/"+deviceId.getId().toString())
                .andExpect(status().isOk());
    }

    protected DataModelObject createDataModelObjectWithParentDMOId(DataModel dataModel , String name , String type , DataModelObjectId parentId) throws Exception {
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName(name);
        dataModelObject.setDataModelId(dataModel.getId());
        dataModelObject.setType(type);
        dataModelObject.setParentId(parentId);
        DataModelObject savedDataModelObject = doPost("/api/data-model/" + dataModel.getId().toString() + "/objects", dataModelObject, DataModelObject.class);
        assertNotNull(savedDataModelObject);
        assertNotNull(savedDataModelObject.getId());
        Assert.assertTrue(savedDataModelObject.getCreatedTime() > 0);
        assertNotNull(savedDataModelObject.getCustomerId());
        assertEquals(dataModelObject.getName(), savedDataModelObject.getName());
        return savedDataModelObject;
    }

    protected Device createGatewayDevice(String name , String deviceType , DataModelObjectId dataModelObjectId , CustomerId customerId ) throws Exception{
        Device gatewayDevice = new Device();
        gatewayDevice.setName(name);
        gatewayDevice.setType(deviceType);
        gatewayDevice.setDataModelObjectId(dataModelObjectId);
        gatewayDevice.setCustomerId(customerId);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode additionalInfo = mapper.readTree("{\"gateway\":true}");
        gatewayDevice.setAdditionalInfo(additionalInfo);
        return doPost("/api/device", gatewayDevice, Device.class);
    }
}
