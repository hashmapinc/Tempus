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
package com.hashmapinc.server.dao.entity;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.dao.customergroup.CustomerGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.common.data.HasName;
import com.hashmapinc.server.common.data.alarm.AlarmId;
import com.hashmapinc.server.common.data.id.*;
import com.hashmapinc.server.dao.alarm.AlarmService;
import com.hashmapinc.server.dao.asset.AssetService;
import com.hashmapinc.server.dao.customer.CustomerService;
import com.hashmapinc.server.dao.dashboard.DashboardService;
import com.hashmapinc.server.dao.device.DeviceService;
import com.hashmapinc.server.dao.plugin.PluginService;
import com.hashmapinc.server.dao.rule.RuleService;
import com.hashmapinc.server.dao.tenant.TenantService;
import com.hashmapinc.server.dao.user.UserService;

/**
 * Created by ashvayka on 04.05.17.
 */
@Service
@Slf4j
public class BaseEntityService extends AbstractEntityService implements EntityService {

    @Autowired
    private AssetService assetService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserService userService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private CustomerGroupService customerGroupService;

    @Override
    public void deleteEntityRelations(EntityId entityId) {
        super.deleteEntityRelations(entityId);
    }

    @Override
    public ListenableFuture<String> fetchEntityNameAsync(EntityId entityId) {
        log.trace("Executing fetchEntityNameAsync [{}]", entityId);
        ListenableFuture<String> entityName;
        ListenableFuture<? extends HasName> hasName;
        switch (entityId.getEntityType()) {
            case ASSET:
                hasName = assetService.findAssetByIdAsync(new AssetId(entityId.getId()));
                break;
            case DEVICE:
                hasName = deviceService.findDeviceByIdAsync(new DeviceId(entityId.getId()));
                break;
            case RULE:
                hasName = ruleService.findRuleByIdAsync(new RuleId(entityId.getId()));
                break;
            case PLUGIN:
                hasName = pluginService.findPluginByIdAsync(new PluginId(entityId.getId()));
                break;
            case TENANT:
                hasName = tenantService.findTenantByIdAsync(new TenantId(entityId.getId()));
                break;
            case CUSTOMER:
                hasName = customerService.findCustomerByIdAsync(new CustomerId(entityId.getId()));
                break;
            case USER:
                hasName = userService.findUserByIdAsync(new UserId(entityId.getId()));
                break;
            case DASHBOARD:
                hasName = dashboardService.findDashboardInfoByIdAsync(new DashboardId(entityId.getId()));
                break;
            case ALARM:
                hasName = alarmService.findAlarmByIdAsync(new AlarmId(entityId.getId()));
                break;
            case CUSTOMER_GROUP:
                hasName = customerGroupService.findCustomerGroupByIdAsync(new CustomerGroupId(entityId.getId()));
                break;
            default:
                throw new IllegalStateException("Not Implemented!");
        }
        entityName = Futures.transform(hasName, (Function<HasName, String>) hasName1 -> hasName1 != null ? hasName1.getName() : null );
        return entityName;
    }

}
