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
package com.hashmapinc.server.dao.device;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Result;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.EntitySubtype;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.EntitySubtypeEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.DeviceEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

@Component
@Slf4j
@NoSqlDao
public class CassandraDeviceDao extends CassandraAbstractSearchTextDao<DeviceEntity, Device> implements DeviceDao {

    @Override
    protected Class<DeviceEntity> getColumnFamilyClass() {
        return DeviceEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.DEVICE_COLUMN_FAMILY_NAME;
    }

    @Override
    public Device save(Device domain) {
        Device savedDevice = super.save(domain);
        EntitySubtype entitySubtype = new EntitySubtype(savedDevice.getTenantId(), EntityType.DEVICE, savedDevice.getType());
        EntitySubtypeEntity entitySubtypeEntity = new EntitySubtypeEntity(entitySubtype);
        Statement saveStatement = cluster.getMapper(EntitySubtypeEntity.class).saveQuery(entitySubtypeEntity);
        executeWrite(saveStatement);
        return savedDevice;
    }

    @Override
    public List<Device> findDevicesByTenantId(UUID tenantId, TextPageLink pageLink) {
        log.debug("Try to find devices by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<DeviceEntity> deviceEntities = findPageWithTextSearch(ModelConstants.DEVICE_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Collections.singletonList(eq(ModelConstants.DEVICE_TENANT_ID_PROPERTY, tenantId)), pageLink);

        log.trace("Found devices [{}] by tenantId [{}] and pageLink [{}]", deviceEntities, tenantId, pageLink);
        return DaoUtil.convertDataList(deviceEntities);
    }

    @Override
    public List<Device> findDevicesByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
        log.debug("Try to find devices by tenantId [{}], type [{}] and pageLink [{}]", tenantId, type, pageLink);
        List<DeviceEntity> deviceEntities = findPageWithTextSearch(ModelConstants.DEVICE_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(ModelConstants.DEVICE_TYPE_PROPERTY, type),
                        eq(ModelConstants.DEVICE_TENANT_ID_PROPERTY, tenantId)), pageLink);
        log.trace("Found devices [{}] by tenantId [{}], type [{}] and pageLink [{}]", deviceEntities, tenantId, type, pageLink);
        return DaoUtil.convertDataList(deviceEntities);
    }

    @Override
    public ListenableFuture<List<Device>> findDevicesByTenantIdAndIdsAsync(UUID tenantId, List<UUID> deviceIds) {
        log.debug("Try to find devices by tenantId [{}] and device Ids [{}]", tenantId, deviceIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(ModelConstants.DEVICE_TENANT_ID_PROPERTY, tenantId));
        query.and(QueryBuilder.in(ModelConstants.ID_PROPERTY, deviceIds));
        return findListByStatementAsync(query);
    }

    @Override
    public List<Device> findDevicesByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        log.debug("Try to find devices by tenantId [{}], customerId[{}] and pageLink [{}]", tenantId, customerId, pageLink);
        List<DeviceEntity> deviceEntities = findPageWithTextSearch(ModelConstants.DEVICE_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(ModelConstants.DEVICE_CUSTOMER_ID_PROPERTY, customerId),
                        eq(ModelConstants.DEVICE_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found devices [{}] by tenantId [{}], customerId [{}] and pageLink [{}]", deviceEntities, tenantId, customerId, pageLink);
        return DaoUtil.convertDataList(deviceEntities);
    }

    @Override
    public List<Device> findDevicesByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink) {
        log.debug("Try to find devices by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", tenantId, customerId, type, pageLink);
        List<DeviceEntity> deviceEntities = findPageWithTextSearch(ModelConstants.DEVICE_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(ModelConstants.DEVICE_TYPE_PROPERTY, type),
                        eq(ModelConstants.DEVICE_CUSTOMER_ID_PROPERTY, customerId),
                        eq(ModelConstants.DEVICE_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found devices [{}] by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", deviceEntities, tenantId, customerId, type, pageLink);
        return DaoUtil.convertDataList(deviceEntities);
    }

    @Override
    public ListenableFuture<List<Device>> findDevicesByTenantIdCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> deviceIds) {
        log.debug("Try to find devices by tenantId [{}], customerId [{}] and device Ids [{}]", tenantId, customerId, deviceIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(ModelConstants.DEVICE_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ModelConstants.DEVICE_CUSTOMER_ID_PROPERTY, customerId));
        query.and(QueryBuilder.in(ModelConstants.ID_PROPERTY, deviceIds));
        return findListByStatementAsync(query);
    }

    @Override
    public Optional<Device> findDeviceByTenantIdAndName(UUID tenantId, String deviceName) {
        Select select = select().from(ModelConstants.DEVICE_BY_TENANT_AND_NAME_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(ModelConstants.DEVICE_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ModelConstants.DEVICE_NAME_PROPERTY, deviceName));
        return Optional.ofNullable(DaoUtil.getData(findOneByStatement(query)));
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findTenantDeviceTypesAsync(UUID tenantId) {
        Select select = select().from(ModelConstants.ENTITY_SUBTYPE_COLUMN_FAMILY_NAME);
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ENTITY_SUBTYPE_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ModelConstants.ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY, EntityType.DEVICE));
        query.setConsistencyLevel(cluster.getDefaultReadConsistencyLevel());
        ResultSetFuture resultSetFuture = getSession().executeAsync(query);
        return Futures.transform(resultSetFuture, new Function<ResultSet, List<EntitySubtype>>() {
            @Nullable
            @Override
            public List<EntitySubtype> apply(@Nullable ResultSet resultSet) {
                Result<EntitySubtypeEntity> result = cluster.getMapper(EntitySubtypeEntity.class).map(resultSet);
                if (result != null) {
                    List<EntitySubtype> entitySubtypes = new ArrayList<>();
                    result.all().forEach((entitySubtypeEntity) ->
                        entitySubtypes.add(entitySubtypeEntity.toEntitySubtype())
                    );
                    return entitySubtypes;
                } else {
                    return Collections.emptyList();
                }
            }
        });
    }

}
