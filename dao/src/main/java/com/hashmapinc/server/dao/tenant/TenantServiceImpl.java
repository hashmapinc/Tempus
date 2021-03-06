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
package com.hashmapinc.server.dao.tenant;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.DataConstants;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.customer.CustomerService;
import com.hashmapinc.server.dao.customergroup.CustomerGroupService;
import com.hashmapinc.server.dao.dashboard.DashboardService;
import com.hashmapinc.server.dao.datamodel.DataModelService;
import com.hashmapinc.server.dao.device.DeviceService;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.rule.RuleService;
import com.hashmapinc.server.dao.service.DataValidator;
import com.hashmapinc.server.dao.service.PaginatedRemover;
import com.hashmapinc.server.dao.user.UserService;
import com.hashmapinc.server.dao.widget.WidgetsBundleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.dao.asset.AssetService;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.plugin.PluginService;
import com.hashmapinc.server.dao.service.Validator;

import java.util.Collections;
import java.util.List;



import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class TenantServiceImpl extends AbstractEntityService implements TenantService {

    private static final String DEFAULT_TENANT_REGION = "Global";
    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    private static final String DEFAULT_UNIT_SYSTEM = "Metric";

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private WidgetsBundleService widgetsBundleService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private CustomerGroupService customerGroupService;

    @Autowired
    private DataModelService dataModelService;

    @Override
    public Tenant findTenantById(TenantId tenantId) {
        log.trace("Executing findTenantById [{}]", tenantId);
        Validator.validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return tenantDao.findById(tenantId.getId());
    }

    @Override
    public ListenableFuture<Tenant> findTenantByIdAsync(TenantId tenantId) {
        log.trace("Executing TenantIdAsync [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return tenantDao.findByIdAsync(tenantId.getId());
    }

    @Override
    public Tenant saveTenant(Tenant tenant) {
        log.trace("Executing saveTenant [{}]", tenant);
        tenant.setRegion(DEFAULT_TENANT_REGION);
        tenantValidator.validate(tenant);
        Tenant savedTenant = tenantDao.save(tenant);
        final boolean isNewTenant = tenant.getId() == null || tenant.getId().equals(new TenantId(EntityId.NULL_UUID));
        if(isNewTenant && savedTenant != null){
            createGroupForTenant(tenant.getTitle(), savedTenant.getId());
        }
        return savedTenant;
    }

    @Override
    public void deleteTenant(TenantId tenantId) {
        log.trace("Executing deleteTenant [{}]", tenantId);
        Validator.validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        customerService.deleteCustomersByTenantId(tenantId);
        widgetsBundleService.deleteWidgetsBundlesByTenantId(tenantId);
        dashboardService.deleteDashboardsByTenantId(tenantId);
        assetService.deleteAssetsByTenantId(tenantId);
        deviceService.deleteDevicesByTenantId(tenantId);
        userService.deleteTenantAdmins(tenantId);
        ruleService.deleteRulesByTenantId(tenantId);
        pluginService.deletePluginsByTenantId(tenantId);
        tenantDao.removeById(tenantId.getId());
        customerGroupService.deleteCustomerGroupsByTenantIdAndCustomerId(tenantId, new CustomerId(ModelConstants.NULL_UUID));
        dataModelService.deleteDataModelsByTenantId(tenantId);
        deleteEntityRelations(tenantId);
    }

    @Override
    public TextPageData<Tenant> findTenants(TextPageLink pageLink) {
        log.trace("Executing findTenants pageLink [{}]", pageLink);
        Validator.validatePageLink(pageLink, "Incorrect page link " + pageLink);
        List<Tenant> tenants = tenantDao.findTenantsByRegion(DEFAULT_TENANT_REGION, pageLink);
        return new TextPageData<>(tenants, pageLink);
    }

    @Override
    public void deleteTenants() {
        log.trace("Executing deleteTenants");
        tenantsRemover.removeEntities(DEFAULT_TENANT_REGION);
    }

    private DataValidator<Tenant> tenantValidator =
            new DataValidator<Tenant>() {
                @Override
                protected void validateDataImpl(Tenant tenant) {
                    if (StringUtils.isEmpty(tenant.getTitle())) {
                        throw new DataValidationException("Tenant title should be specified!");
                    }
                    if (!StringUtils.isEmpty(tenant.getEmail())) {
                        validateEmail(tenant.getEmail());
                    }
                }
    };

    private PaginatedRemover<String, Tenant> tenantsRemover =
            new PaginatedRemover<String, Tenant>() {

        @Override
        protected List<Tenant> findEntities(String region, TextPageLink pageLink) {
            return tenantDao.findTenantsByRegion(region, pageLink);
        }

        @Override
        protected void removeEntity(Tenant entity) {
            deleteTenant(new TenantId(entity.getUuidId()));
        }
    };

    private void createGroupForTenant(String title, TenantId savedTenantId) {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setTitle("group-"+ title);
        customerGroup.setTenantId(savedTenantId);
        customerGroup.setCustomerId(null);
        customerGroup.setPolicies(Collections.singletonList(DataConstants.TENANT_ADMIN_DEFAULT_PERMISSION));
        customerGroupService.saveCustomerGroup(customerGroup);
    }

    @Override
    public String findLogoByTenantId(TenantId tenantId) {
        Validator.validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        String file = tenantDao.findById(tenantId.getId()).getLogo();
        JsonObject jsonObject = new JsonObject();
        if(file != null && !file.isEmpty()){
            jsonObject.addProperty("logo",file);
        }else {
            jsonObject.addProperty("logo","");
        }
        return jsonObject.toString();
    }

    @Override
    public void saveUnitSystem(String unitSystem , TenantId tenantId) {
        log.trace("Executing saveUnitSystem, unitSystem [{}], tenantId [{}]", unitSystem, tenantId);
        Validator.validateEntityId(tenantId, INCORRECT_TENANT_ID + tenantId);
        String foundUnitSystem = tenantDao.findUnitSystemByTenantId(tenantId.getId());
        if (foundUnitSystem == null) {
            tenantDao.saveUnitSystem(unitSystem , tenantId.getId());
        } else if (!foundUnitSystem.equals(unitSystem)) {
            tenantDao.updateUnitSystem(unitSystem, tenantId.getId());
        }
    }

    @Override
    public String findUnitSystemByTenantId(TenantId tenantId) {
        log.trace("Executing findUnitSystemByTenantId, tenantId [{}]", tenantId);
        Validator.validateEntityId(tenantId, INCORRECT_TENANT_ID + tenantId);
        String unitSystemByUserId = tenantDao.findUnitSystemByTenantId(tenantId.getId());
        if (unitSystemByUserId == null) {
            unitSystemByUserId = DEFAULT_UNIT_SYSTEM;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("unit_system",unitSystemByUserId);
        return jsonObject.toString();
    }

    @Override
    public void deleteUnitSystemByTenantId(TenantId tenantId) {
        log.trace("Executing deleteUnitSystemByTenantId, tenantId [{}]", tenantId);
        Validator.validateEntityId(tenantId, INCORRECT_TENANT_ID + tenantId);
        tenantDao.deleteUnitSystemByTenantId(tenantId.getId());
    }
}
