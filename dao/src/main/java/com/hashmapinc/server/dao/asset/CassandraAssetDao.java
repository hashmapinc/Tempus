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
package com.hashmapinc.server.dao.asset;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Result;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.EntitySubtype;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.TempusResourceCriteriaSpec;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.id.AssetId;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.EntitySubtypeEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.nosql.AssetEntity;
import com.hashmapinc.server.dao.nosql.CassandraAbstractSearchTextDao;
import com.hashmapinc.server.dao.querybuilder.CassandraTempusResourcePredicateBuilder;
import com.hashmapinc.server.dao.querybuilder.ResourceCriteria;
import com.hashmapinc.server.dao.querybuilder.TempusResourcePredicate;
import com.hashmapinc.server.dao.util.NoSqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Component
@Slf4j
@NoSqlDao
public class CassandraAssetDao extends CassandraAbstractSearchTextDao<AssetEntity, Asset> implements AssetDao {


    @Override
    protected Class<AssetEntity> getColumnFamilyClass() {
        return AssetEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ModelConstants.ASSET_COLUMN_FAMILY_NAME;
    }

    @Override
    public Asset save(Asset domain) {
        Asset savedAsset = super.save(domain);
        EntitySubtype entitySubtype = new EntitySubtype(savedAsset.getTenantId(), EntityType.ASSET, savedAsset.getType());
        EntitySubtypeEntity entitySubtypeEntity = new EntitySubtypeEntity(entitySubtype);
        Statement saveStatement = cluster.getMapper(EntitySubtypeEntity.class).saveQuery(entitySubtypeEntity);
        executeWrite(saveStatement);
        return savedAsset;
    }

    @Override
    public List<Asset> findAssetsByTenantId(UUID tenantId, TextPageLink pageLink) {
        log.debug("Try to find assets by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<AssetEntity> assetEntities = findPageWithTextSearch(ModelConstants.ASSET_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Collections.singletonList(eq(ModelConstants.ASSET_TENANT_ID_PROPERTY, tenantId)), pageLink);

        log.trace("Found assets [{}] by tenantId [{}] and pageLink [{}]", assetEntities, tenantId, pageLink);
        return DaoUtil.convertDataList(assetEntities);
    }

    @Override
    public List<Asset> findAssetsByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
        log.debug("Try to find assets by tenantId [{}], type [{}] and pageLink [{}]", tenantId, type, pageLink);
        List<AssetEntity> assetEntities = findPageWithTextSearch(ModelConstants.ASSET_BY_TENANT_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(ModelConstants.ASSET_TYPE_PROPERTY, type),
                        eq(ModelConstants.ASSET_TENANT_ID_PROPERTY, tenantId)), pageLink);
        log.trace("Found assets [{}] by tenantId [{}], type [{}] and pageLink [{}]", assetEntities, tenantId, type, pageLink);
        return DaoUtil.convertDataList(assetEntities);
    }

    public ListenableFuture<List<Asset>> findAssetsByTenantIdAndIdsAsync(UUID tenantId, List<UUID> assetIds) {
        log.debug("Try to find assets by tenantId [{}] and asset Ids [{}]", tenantId, assetIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ASSET_TENANT_ID_PROPERTY, tenantId));
        query.and(QueryBuilder.in(ModelConstants.ID_PROPERTY, assetIds));
        return findListByStatementAsync(query);
    }

    @Override
    public List<Asset> findAssetsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        log.debug("Try to find assets by tenantId [{}], customerId[{}] and pageLink [{}]", tenantId, customerId, pageLink);
        List<AssetEntity> assetEntities = findPageWithTextSearch(ModelConstants.ASSET_BY_CUSTOMER_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(ModelConstants.ASSET_CUSTOMER_ID_PROPERTY, customerId),
                        eq(ModelConstants.ASSET_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found assets [{}] by tenantId [{}], customerId [{}] and pageLink [{}]", assetEntities, tenantId, customerId, pageLink);
        return DaoUtil.convertDataList(assetEntities);
    }

    @Override
    public List<Asset> findAssetsByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink) {
        log.debug("Try to find assets by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", tenantId, customerId, type, pageLink);
        List<AssetEntity> assetEntities = findPageWithTextSearch(ModelConstants.ASSET_BY_CUSTOMER_BY_TYPE_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Arrays.asList(eq(ModelConstants.ASSET_TYPE_PROPERTY, type),
                        eq(ModelConstants.ASSET_CUSTOMER_ID_PROPERTY, customerId),
                        eq(ModelConstants.ASSET_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found assets [{}] by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", assetEntities, tenantId, customerId, type, pageLink);
        return DaoUtil.convertDataList(assetEntities);
    }

    @Override
    public ListenableFuture<List<Asset>> findAssetsByTenantIdAndCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> assetIds) {
        log.debug("Try to find assets by tenantId [{}], customerId [{}] and asset Ids [{}]", tenantId, customerId, assetIds);
        Select select = select().from(getColumnFamilyName());
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ASSET_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ModelConstants.ASSET_CUSTOMER_ID_PROPERTY, customerId));
        query.and(QueryBuilder.in(ModelConstants.ID_PROPERTY, assetIds));
        return findListByStatementAsync(query);
    }

    @Override
    public Optional<Asset> findAssetsByTenantIdAndName(UUID tenantId, String assetName) {
        Select select = select().from(ModelConstants.ASSET_BY_TENANT_AND_NAME_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ASSET_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ModelConstants.ASSET_NAME_PROPERTY, assetName));
        AssetEntity assetEntity = findOneByStatement(query);
        return Optional.ofNullable(DaoUtil.getData(assetEntity));
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findTenantAssetTypesAsync(UUID tenantId) {
        Select select = select().from(ModelConstants.ENTITY_SUBTYPE_COLUMN_FAMILY_NAME);
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ENTITY_SUBTYPE_TENANT_ID_PROPERTY, tenantId));
        query.and(eq(ModelConstants.ENTITY_SUBTYPE_ENTITY_TYPE_PROPERTY, EntityType.ASSET));
        query.setConsistencyLevel(cluster.getDefaultReadConsistencyLevel());
        ResultSetFuture resultSetFuture = getSession().executeAsync(query);
        return Futures.transform(resultSetFuture,(@Nullable ResultSet resultSet)->{
            Result<EntitySubtypeEntity> result = cluster.getMapper(EntitySubtypeEntity.class).map(resultSet);
            if (result != null) {
                List<EntitySubtype> entitySubtypes = new ArrayList<>();
                result.all().forEach(entitySubtypeEntity ->
                        entitySubtypes.add(entitySubtypeEntity.toEntitySubtype())
                );
                return entitySubtypes;
            } else {
                return Collections.emptyList();
            }
        });
    }

    @Override
    public List<Asset> findAssetsByDataModelObjectId(UUID dataModelObjectId) {
        log.debug("Try to find assets by dataModelObjectId [{}]", dataModelObjectId);
        Select select = select().from(ModelConstants.ASSET_BY_DATA_MODEL_OBJECT_VIEW_NAME);
        Select.Where query = select.where();
        query.and(eq(ModelConstants.ASSET_DATA_MODEL_OBJECT_ID, dataModelObjectId));
        return DaoUtil.convertDataList(findListByStatement(query));
    }

    @Override
    public List<Asset> findAll(TempusResourceCriteriaSpec tempusResourceCriteriaSpec, TextPageLink textPageLink) {
        final ResourceCriteria idConstraint = getIdConstraint(tempusResourceCriteriaSpec, textPageLink);
        final List<Clause> predicates = getPredicates(tempusResourceCriteriaSpec, idConstraint);
        List<AssetEntity> assetEntities = Collections.emptyList();
        if(Objects.nonNull(idConstraint)){
            final String viewName = tempusResourceCriteriaSpec.getCustomerId().isPresent() ?
                    ModelConstants.ASSET_BY_DATA_MODEL_OBJECT_AND_ASSET_AND_CUSTOMER_VIEW_NAME:
                    ModelConstants.ASSET_BY_DATA_MODEL_AND_ASSET_VIEW_NAME ;
            assetEntities = findPageWithTextSearchAndNoIdCompare(viewName, predicates, textPageLink);
        }
        if(assetEntities.isEmpty()){
            final String viewName = tempusResourceCriteriaSpec.getCustomerId().isPresent() ?
                    ModelConstants.ASSET_BY_DATA_MODEL_OBJECT_AND_CUSTOMER_VIEW_NAME:
                    ModelConstants.ASSET_BY_DATA_MODEL_VIEW_NAME;
            assetEntities = findPageWithTextSearch(viewName, predicates, textPageLink);
        }
        return DaoUtil.convertDataList(assetEntities);
    }

    private List<Clause> getPredicates(TempusResourceCriteriaSpec tempusResourceCriteriaSpec, ResourceCriteria idConstraint) {
        final CassandraTempusResourcePredicateBuilder basicPredicateBuilder = new CassandraTempusResourcePredicateBuilder()
                .with(new ResourceCriteria(ModelConstants.ASSET_TENANT_ID_PROPERTY, ResourceCriteria.Operation.EQUALS, tempusResourceCriteriaSpec.getTenantId().getId()))
                .with(new ResourceCriteria(ModelConstants.ASSET_DATA_MODEL_OBJECT_ID, ResourceCriteria.Operation.EQUALS, tempusResourceCriteriaSpec.getDataModelObjectId().getId()));

        tempusResourceCriteriaSpec.getCustomerId().ifPresent(customerId ->
                basicPredicateBuilder.with(new ResourceCriteria(ModelConstants.ASSET_CUSTOMER_ID_PROPERTY, ResourceCriteria.Operation.EQUALS, customerId.getId())));

        if(Objects.nonNull(idConstraint)){
            basicPredicateBuilder.with(idConstraint);
        }
        return basicPredicateBuilder.build().stream().map(TempusResourcePredicate::getValue).collect(Collectors.toList());
    }

    private ResourceCriteria getIdConstraint(TempusResourceCriteriaSpec tempusResourceCriteriaSpec, TextPageLink textPageLink) {

        final Set<UUID> accessibleTempusResourceIdsOnly = tempusResourceCriteriaSpec.getAccessibleIdsForGivenDataModelObject().stream()
                .filter(id -> id instanceof AssetId)
                .map(EntityId::getId)
                .filter(id -> {
                    if(textPageLink.getIdOffset() != null){
                        return id.compareTo(textPageLink.getIdOffset()) < 0;
                    }
                    return true;
                })
                .collect(Collectors.toSet());

        if(accessibleTempusResourceIdsOnly.size() == 1){
            return new ResourceCriteria(ModelConstants.ID_PROPERTY, ResourceCriteria.Operation.EQUALS, accessibleTempusResourceIdsOnly.toArray()[0]);
        } else if (accessibleTempusResourceIdsOnly.size() > 1) {
            return new ResourceCriteria(ModelConstants.ID_PROPERTY, ResourceCriteria.Operation.CONTAINS, accessibleTempusResourceIdsOnly.toArray(new UUID[0]));
        }
        return null;
    }
}