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
package com.hashmapinc.server.dao.sql.device;

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.*;
import com.hashmapinc.server.common.data.id.DeviceId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.device.DeviceDao;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.sql.DeviceEntity;
import com.hashmapinc.server.dao.querybuilder.ResourceCriteria;
import com.hashmapinc.server.dao.querybuilder.TempusResourcePredicate;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;
import com.hashmapinc.server.dao.sql.querydsl.JPATempusResourcePredicateBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.hashmapinc.server.common.data.UUIDConverter.fromTimeUUID;
import static com.hashmapinc.server.common.data.UUIDConverter.fromTimeUUIDs;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
@Component
public class JpaDeviceDao extends JpaAbstractSearchTextDao<DeviceEntity, Device> implements DeviceDao {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    protected Class<DeviceEntity> getEntityClass() {
        return DeviceEntity.class;
    }

    @Override
    protected CrudRepository<DeviceEntity, String> getCrudRepository() {
        return deviceRepository;
    }

    @Override
    public List<Device> findDevicesByTenantId(UUID tenantId, TextPageLink pageLink) {
        return DaoUtil.convertDataList(
                deviceRepository.findByTenantId(
                        fromTimeUUID(tenantId),
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR : fromTimeUUID(pageLink.getIdOffset()),
                        new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    public ListenableFuture<List<Device>> findDevicesByTenantIdAndIdsAsync(UUID tenantId, List<UUID> deviceIds) {
        return service.submit(() -> DaoUtil.convertDataList(deviceRepository.findDevicesByTenantIdAndIdIn(UUIDConverter.fromTimeUUID(tenantId), fromTimeUUIDs(deviceIds))));
    }

    @Override
    public List<Device> findDevicesByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        return DaoUtil.convertDataList(
                deviceRepository.findByTenantIdAndCustomerId(
                        fromTimeUUID(tenantId),
                        fromTimeUUID(customerId),
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR : fromTimeUUID(pageLink.getIdOffset()),
                        new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    public ListenableFuture<List<Device>> findDevicesByTenantIdCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> deviceIds) {
        return service.submit(() -> DaoUtil.convertDataList(
                deviceRepository.findDevicesByTenantIdAndCustomerIdAndIdIn(fromTimeUUID(tenantId), fromTimeUUID(customerId), fromTimeUUIDs(deviceIds))));
    }

    @Override
    public Optional<Device> findDeviceByTenantIdAndName(UUID tenantId, String name) {
        Device device = DaoUtil.getData(deviceRepository.findByTenantIdAndName(fromTimeUUID(tenantId), name));
        return Optional.ofNullable(device);
    }

    @Override
    public List<Device> findDevicesByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
        return DaoUtil.convertDataList(
                deviceRepository.findByTenantIdAndType(
                        fromTimeUUID(tenantId),
                        type,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR : fromTimeUUID(pageLink.getIdOffset()),
                        new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    public List<Device> findDevicesByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink) {
        return DaoUtil.convertDataList(
                deviceRepository.findByTenantIdAndCustomerIdAndType(
                        fromTimeUUID(tenantId),
                        fromTimeUUID(customerId),
                        type,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR : fromTimeUUID(pageLink.getIdOffset()),
                        new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    public List<Device> findDeviceByDataModelObjectId(UUID dataModelObjectId) {
        return DaoUtil.convertDataList(deviceRepository
                                               .findByDataModelObjectId(
                                                       UUIDConverter.fromTimeUUID(dataModelObjectId)));
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findTenantDeviceTypesAsync(UUID tenantId) {
        return service.submit(() -> convertTenantDeviceTypesToDto(tenantId, deviceRepository.findTenantDeviceTypes(fromTimeUUID(tenantId))));
    }

    @Override
    public List<Device> findAll(TempusResourceCriteriaSpec tempusResourceCriteriaSpec, int limit, int pageNum) {
        final List<BooleanExpression> predicates = getPredicates(tempusResourceCriteriaSpec);
        final Optional<BooleanExpression> finalPredicate = predicates.stream().reduce(BooleanExpression::and);
        final PageRequest pageable = new PageRequest(pageNum, limit, new Sort(new Sort.Order(Sort.Direction.ASC, ModelConstants.ID_PROPERTY)));
        return finalPredicate.isPresent() ? DaoUtil.convertDataList(deviceRepository.findAll(finalPredicate.get(), pageable).getContent()) : Collections.emptyList();
    }

    private List<BooleanExpression> getPredicates(TempusResourceCriteriaSpec tempusResourceCriteriaSpec) {

        final JPATempusResourcePredicateBuilder basicPredicateBuilder = new JPATempusResourcePredicateBuilder(getEntityClass())
                .with(new ResourceCriteria(ModelConstants.DEVICE_TENANT_ID_PROPERTY, ResourceCriteria.Operation.EQUALS, UUIDConverter.fromTimeUUID(tempusResourceCriteriaSpec.getTenantId().getId())));

        tempusResourceCriteriaSpec.getDataModelObjectId().ifPresent(dataModelObjectId ->
                basicPredicateBuilder.with(new ResourceCriteria(ModelConstants.DEVICE_DATA_MODEL_OBJECT_ID, ResourceCriteria.Operation.EQUALS, UUIDConverter.fromTimeUUID(dataModelObjectId.getId()))));

        tempusResourceCriteriaSpec.getSearchText().ifPresent(searchText ->
                basicPredicateBuilder.with(new ResourceCriteria(ModelConstants.SEARCH_TEXT_PROPERTY, ResourceCriteria.Operation.LIKE, searchText)));

        tempusResourceCriteriaSpec.getType().ifPresent(type ->
                basicPredicateBuilder.with(new ResourceCriteria(ModelConstants.DEVICE_TYPE_PROPERTY, ResourceCriteria.Operation.EQUALS, type)));


        tempusResourceCriteriaSpec.getCustomerId().ifPresent(customerId ->
                                                                     basicPredicateBuilder.with(new ResourceCriteria(ModelConstants.DEVICE_CUSTOMER_ID_PROPERTY, ResourceCriteria.Operation.EQUALS, UUIDConverter.fromTimeUUID(customerId.getId()))));

        if(!tempusResourceCriteriaSpec.getAccessibleIdsForGivenDataModelObject().isEmpty()) {
            final ResourceCriteria idConstraint = getIdConstraint(tempusResourceCriteriaSpec);
            if (Objects.nonNull(idConstraint)) {
                basicPredicateBuilder.with(idConstraint);
            }
        }
        if(!tempusResourceCriteriaSpec.getDataModelObjectIds().isEmpty()) {
            final ResourceCriteria dmoIdsConstraint = getDataModelObjectIdConstraint(tempusResourceCriteriaSpec);
            if (Objects.nonNull(dmoIdsConstraint)) {
                basicPredicateBuilder.with(dmoIdsConstraint);
            }
        }

        final List<TempusResourcePredicate<BooleanExpression>> tempusResourcePredicates = basicPredicateBuilder.build();
        return tempusResourcePredicates.stream().map(TempusResourcePredicate::getValue).collect(Collectors.toList());
    }

    private ResourceCriteria getIdConstraint(TempusResourceCriteriaSpec tempusResourceCriteriaSpec) {
        final Set<String> accessibleTempusResourceIdsOnly = tempusResourceCriteriaSpec.getAccessibleIdsForGivenDataModelObject().stream()
                .filter(id -> id instanceof DeviceId)
                .map(id -> UUIDConverter.fromTimeUUID(id.getId()))
                .collect(Collectors.toSet());

        if(accessibleTempusResourceIdsOnly.size() == 1){
            return new ResourceCriteria(ModelConstants.ID_PROPERTY, ResourceCriteria.Operation.EQUALS, accessibleTempusResourceIdsOnly.toArray()[0]);
        } else if (accessibleTempusResourceIdsOnly.size() > 1) {
            return new ResourceCriteria(ModelConstants.ID_PROPERTY, ResourceCriteria.Operation.CONTAINS, accessibleTempusResourceIdsOnly.toArray(new String[0]));
        }
        return null;
    }

    private ResourceCriteria getDataModelObjectIdConstraint(TempusResourceCriteriaSpec tempusResourceCriteriaSpec) {
        final Set<String> accessibleDMOIdsOnly = tempusResourceCriteriaSpec.getDataModelObjectIds().stream()
                .map(id -> UUIDConverter.fromTimeUUID(id.getId()))
                .collect(Collectors.toSet());

        return new ResourceCriteria(ModelConstants.DEVICE_DATA_MODEL_OBJECT_ID, ResourceCriteria.Operation.CONTAINS, accessibleDMOIdsOnly.toArray(new String[0]));
    }

    private List<EntitySubtype> convertTenantDeviceTypesToDto(UUID tenantId, List<String> types) {
        List<EntitySubtype> list = Collections.emptyList();
        if (types != null && !types.isEmpty()) {
            list = new ArrayList<>();
            for (String type : types) {
                list.add(new EntitySubtype(new TenantId(tenantId), EntityType.DEVICE, type));
            }
        }
        return list;
    }
}
