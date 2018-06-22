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
package com.hashmapinc.server.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.Customer;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.User;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.common.data.security.UserCredentials;
import com.hashmapinc.server.dao.customer.CustomerDao;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.service.DataValidator;
import com.hashmapinc.server.dao.service.PaginatedRemover;
import com.hashmapinc.server.dao.service.Validator;
import com.hashmapinc.server.dao.tenant.TenantDao;
import com.hashmapinc.server.dao.user.UserCredentialsDao;
import com.hashmapinc.server.dao.user.UserDao;
import com.hashmapinc.server.dao.user.UserService;
import com.hashmapinc.server.requests.ActivateUserRequest;
import com.hashmapinc.server.requests.CreateUserRequest;
import com.hashmapinc.server.requests.IdentityUser;
import com.hashmapinc.server.requests.IdentityUserCredentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Slf4j
@Service
public class RestUserService extends AbstractEntityService implements UserService{

    public static final String IDENTITY_ENDPOINT = "http://localhost:9002/uaa/users/";

    @Autowired
    @Qualifier("clientRestTemplate")
    private RestTemplate restTemplate;

    private static final int DEFAULT_TOKEN_LENGTH = 30;
    public static final String INCORRECT_USER_ID = "Incorrect userId ";
    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserCredentialsDao userCredentialsDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;

    private ObjectMapper mapper = new ObjectMapper();


    @Override
    public User findUserById(UserId userId) {
        validateId(userId, INCORRECT_USER_ID + userId);
        ResponseEntity<IdentityUser> response = restTemplate.getForEntity(IDENTITY_ENDPOINT + userId.getId(), IdentityUser.class);
        if(response.getStatusCode().equals(HttpStatus.OK)){
            return response.getBody().toUser();
        }
        return null;
    }

    @Override
    public ListenableFuture<User> findUserByIdAsync(UserId userId) {
        return null;
    }

    @Override
    public User findUserByEmail(String email) {
        Validator.validateString(email, "Incorrect email " + email);
        String encoded = Base64.getUrlEncoder().encodeToString(email.getBytes());
        ResponseEntity<IdentityUser> response = restTemplate.getForEntity(IDENTITY_ENDPOINT + "username/" + encoded, IdentityUser.class);
        if(response.getStatusCode().equals(HttpStatus.OK)){
            return response.getBody().toUser();
        }
        return null;
    }


    @Override
    public User saveUser(User user) {
        log.trace("Executing saveUser [{}]", user);
        if(user.getId() == null) {
            CreateUserRequest userRequest = CreateUserRequest.builder().user(new IdentityUser(user)).activationType("link").build();
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(IDENTITY_ENDPOINT, userRequest, JsonNode.class);
            if (response.getStatusCode().equals(HttpStatus.CREATED)) {
                JsonNode body = response.getBody();
                try {
                    return mapper.treeToValue(body.get("user"), IdentityUser.class).toUser();
                } catch (JsonProcessingException e) {
                    log.error("Error while saving user ", e);
                }

            }
        }else{
            ResponseEntity<IdentityUser> response = restTemplate.exchange(IDENTITY_ENDPOINT + user.getId(),
                    HttpMethod.PUT, new HttpEntity<>(new IdentityUser(user)), IdentityUser.class);
            if(response.getStatusCode().equals(HttpStatus.OK)) {
                return response.getBody().toUser();
            }
        }
        return null;
    }

    @Override
    public User saveExternalUser(User user){
        log.trace("Executing save external user [{}]", user);
        userValidator.validate(user);
        User savedUser = userDao.save(user);
        /*if(user.getId() == null) {
            UserCredentials userCredentials = new UserCredentials();
            userCredentials.setEnabled(true);
            userCredentials.setActivateToken(null);
            userCredentials.setUserId(new UserId(savedUser.getUuidId()));
            userCredentialsDao.save(userCredentials);
        }*/
        return savedUser;
    }

    @Override
    public UserCredentials findUserCredentialsByUserId(UserId userId) {
        log.trace("Executing findUserCredentialsByUserId [{}]", userId);
        validateId(userId, INCORRECT_USER_ID + userId);

        ResponseEntity<IdentityUserCredentials> response = restTemplate
                .getForEntity(IDENTITY_ENDPOINT + userId.getId() + "/user-credentials", IdentityUserCredentials.class);
        if(response.getStatusCode().equals(HttpStatus.OK)){
            return response.getBody().toUserCredentials();
        }

        return null;
    }

    @Override
    public UserCredentials findUserCredentialsByActivateToken(String activateToken) {
        log.trace("Executing findUserCredentialsByActivateToken [{}]", activateToken);
        Validator.validateString(activateToken, "Incorrect activateToken " + activateToken);
        ResponseEntity<IdentityUserCredentials> response = restTemplate
                .getForEntity(IDENTITY_ENDPOINT + "activate/" + activateToken +"/user-credentials",
                        IdentityUserCredentials.class);
        if(response.getStatusCode().equals(HttpStatus.OK)){
            return response.getBody().toUserCredentials();
        }
        return null;
    }

    @Override
    public UserCredentials findUserCredentialsByResetToken(String resetToken) {
        log.trace("Executing findUserCredentialsByResetToken [{}]", resetToken);
        Validator.validateString(resetToken, "Incorrect resetToken " + resetToken);
        ResponseEntity<IdentityUserCredentials> response = restTemplate
                .getForEntity(IDENTITY_ENDPOINT + "reset/"+ resetToken + "/user-credentials",
                        IdentityUserCredentials.class);

        if(response.getStatusCode().equals(HttpStatus.OK)){
            return response.getBody().toUserCredentials();
        }

        return userCredentialsDao.findByResetToken(resetToken);
    }

    @Override
    public UserCredentials saveUserCredentials(UserCredentials userCredentials) {
        log.trace("Executing saveUserCredentials [{}]", userCredentials);
        //userCredentialsValidator.validate(userCredentials);
        ResponseEntity<IdentityUserCredentials> response = restTemplate
                .exchange(IDENTITY_ENDPOINT + userCredentials.getId().getId() +"/user-credentials",
                        HttpMethod.PUT,
                        new HttpEntity<>(new IdentityUserCredentials(userCredentials)),
                        IdentityUserCredentials.class);
        if(response.getStatusCode().equals(HttpStatus.OK)){
            return response.getBody().toUserCredentials();
        }

        return null;
    }

    @Override
    public UserCredentials activateUserCredentials(String activateToken, String password) {
        log.trace("Executing activateUserCredentials activateToken [{}], password [{}]", activateToken, password);
        Validator.validateString(activateToken, "Incorrect activateToken " + activateToken);
        Validator.validateString(password, "Incorrect password " + password);

        ActivateUserRequest request = ActivateUserRequest.builder().activateToken(activateToken).password(password).build();
        ResponseEntity<IdentityUserCredentials> response = restTemplate
                .postForEntity(IDENTITY_ENDPOINT+"/activate", request, IdentityUserCredentials.class);

        if(response.getStatusCode().equals(HttpStatus.OK)){
            return response.getBody().toUserCredentials();
        }

        return null;
    }

    @Override
    public UserCredentials requestPasswordReset(String email) {
        log.trace("Executing requestPasswordReset email [{}]", email);
        Validator.validateString(email, "Incorrect email " + email);
        ObjectNode request = mapper.createObjectNode();
        request.put("email", email);
        ResponseEntity<IdentityUserCredentials> response = restTemplate
                .postForEntity(IDENTITY_ENDPOINT + "resetPasswordByEmail", request, IdentityUserCredentials.class);
        if(response.getStatusCode().equals(HttpStatus.OK)){
            return response.getBody().toUserCredentials();
        }
        return null;
    }


    @Override
    public void deleteUser(UserId userId) {
        log.trace("Executing deleteUser [{}]", userId);
        validateId(userId, INCORRECT_USER_ID + userId);
        UserCredentials userCredentials = userCredentialsDao.findByUserId(userId.getId());
        userCredentialsDao.removeById(userCredentials.getUuidId());
        deleteEntityRelations(userId);
        userDao.removeById(userId.getId());
    }

    @Override
    public TextPageData<User> findTenantAdmins(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findTenantAdmins, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        Validator.validatePageLink(pageLink, "Incorrect page link " + pageLink);
        List<User> users = userDao.findTenantAdmins(tenantId.getId(), pageLink);
        return new TextPageData<>(users, pageLink);
    }

    @Override
    public void deleteTenantAdmins(TenantId tenantId) {
        log.trace("Executing deleteTenantAdmins, tenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        tenantAdminsRemover.removeEntities(tenantId);
    }

    @Override
    public TextPageData<User> findCustomerUsers(TenantId tenantId, CustomerId customerId, TextPageLink pageLink) {
        log.trace("Executing findCustomerUsers, tenantId [{}], customerId [{}], pageLink [{}]", tenantId, customerId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, "Incorrect customerId " + customerId);
        Validator.validatePageLink(pageLink, "Incorrect page link " + pageLink);
        List<User> users = userDao.findCustomerUsers(tenantId.getId(), customerId.getId(), pageLink);
        return new TextPageData<>(users, pageLink);
    }

    @Override
    public void deleteCustomerUsers(TenantId tenantId, CustomerId customerId) {
        log.trace("Executing deleteCustomerUsers, customerId [{}]", customerId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, "Incorrect customerId " + customerId);
        new RestUserService.CustomerUsersRemover(tenantId).removeEntities(customerId);
    }

    private DataValidator<User> userValidator =
            new DataValidator<User>() {
                @Override
                protected void validateDataImpl(User user) {
                    if (StringUtils.isEmpty(user.getEmail())) {
                        throw new DataValidationException("User email should be specified!");
                    }

                    validateEmail(user.getEmail());

                    Authority authority = user.getAuthority();
                    if (authority == null) {
                        throw new DataValidationException("User authority isn't defined!");
                    }
                    TenantId tenantId = user.getTenantId();
                    if (tenantId == null) {
                        tenantId = new TenantId(ModelConstants.NULL_UUID);
                        user.setTenantId(tenantId);
                    }
                    CustomerId customerId = user.getCustomerId();
                    if (customerId == null) {
                        customerId = new CustomerId(ModelConstants.NULL_UUID);
                        user.setCustomerId(customerId);
                    }

                    switch (authority) {
                        case SYS_ADMIN:
                            if (!tenantId.getId().equals(ModelConstants.NULL_UUID)
                                    || !customerId.getId().equals(ModelConstants.NULL_UUID)) {
                                throw new DataValidationException("System administrator can't be assigned neither to tenant nor to customer!");
                            }
                            break;
                        case TENANT_ADMIN:
                            if (tenantId.getId().equals(ModelConstants.NULL_UUID)) {
                                throw new DataValidationException("Tenant administrator should be assigned to tenant!");
                            } else if (!customerId.getId().equals(ModelConstants.NULL_UUID)) {
                                throw new DataValidationException("Tenant administrator can't be assigned to customer!");
                            }
                            break;
                        case CUSTOMER_USER:
                            if (tenantId.getId().equals(ModelConstants.NULL_UUID)
                                    || customerId.getId().equals(ModelConstants.NULL_UUID) ) {
                                throw new DataValidationException("Customer user should be assigned to customer!");
                            }
                            break;
                        default:
                            break;
                    }

                    User existentUserWithEmail = findUserByEmail(user.getEmail());
                    if (existentUserWithEmail != null && !isSameData(existentUserWithEmail, user)) {
                        throw new DataValidationException("User with email '" + user.getEmail() + "' "
                                + " already present in database!");
                    }
                    if (!tenantId.getId().equals(ModelConstants.NULL_UUID)) {
                        Tenant tenant = tenantDao.findById(user.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("User is referencing to non-existent tenant!");
                        }
                    }
                    if (!customerId.getId().equals(ModelConstants.NULL_UUID)) {
                        Customer customer = customerDao.findById(user.getCustomerId().getId());
                        if (customer == null) {
                            throw new DataValidationException("User is referencing to non-existent customer!");
                        } else if (!customer.getTenantId().getId().equals(tenantId.getId())) {
                            throw new DataValidationException("User can't be assigned to customer from different tenant!");
                        }
                    }
                }
            };

    private DataValidator<UserCredentials> userCredentialsValidator =
            new DataValidator<UserCredentials>() {

                @Override
                protected void validateCreate(UserCredentials userCredentials) {
                    throw new IncorrectParameterException("Creation of new user credentials is prohibited.");
                }

                @Override
                protected void validateDataImpl(UserCredentials userCredentials) {
                    if (userCredentials.getUserId() == null) {
                        throw new DataValidationException("User credentials should be assigned to user!");
                    }
                    if (userCredentials.isEnabled()) {
                        //activated ldap users can have no password
                        /*if (StringUtils.isEmpty(userCredentials.getPassword())) {
                            throw new DataValidationException("Enabled user credentials should have password!");
                        }*/
                        if (StringUtils.isNotEmpty(userCredentials.getActivateToken())) {
                            throw new DataValidationException("Enabled user credentials can't have activate token!");
                        }
                    }
                    UserCredentials existingUserCredentialsEntity = userCredentialsDao.findById(userCredentials.getId().getId());
                    if (existingUserCredentialsEntity == null) {
                        throw new DataValidationException("Unable to update non-existent user credentials!");
                    }
                    User user = findUserById(userCredentials.getUserId());
                    if (user == null) {
                        throw new DataValidationException("Can't assign user credentials to non-existent user!");
                    }
                }
            };

    private PaginatedRemover<TenantId, User> tenantAdminsRemover =
            new PaginatedRemover<TenantId, User>() {

                @Override
                protected List<User> findEntities(TenantId id, TextPageLink pageLink) {
                    return userDao.findTenantAdmins(id.getId(), pageLink);
                }

                @Override
                protected void removeEntity(User entity) {
                    deleteUser(new UserId(entity.getUuidId()));
                }
            };

    private class CustomerUsersRemover extends PaginatedRemover<CustomerId, User> {

        private TenantId tenantId;

        CustomerUsersRemover(TenantId tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        protected List<User> findEntities(CustomerId id, TextPageLink pageLink) {
            return userDao.findCustomerUsers(tenantId.getId(), id.getId(), pageLink);

        }

        @Override
        protected void removeEntity(User entity) {
            deleteUser(new UserId(entity.getUuidId()));
        }

    }
}
