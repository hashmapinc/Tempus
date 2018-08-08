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
package com.hashmapinc.server.dao.sql.customergroup;

import com.hashmapinc.server.common.data.CustomerGroup;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.CustomerGroupId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.customergroup.CustomerGroupDao;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.sql.CustomerGroupEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Component
@SqlDao
public class JpaCustomerGroupDao extends JpaAbstractSearchTextDao<CustomerGroupEntity, CustomerGroup> implements CustomerGroupDao {

    private static final String QUERY_TO_FETCH_USER_ID_BY_GROUP_ID =
            String.format("SELECT %s FROM %s WHERE %s = ?", USER_ID_PROPERTY, USER_GROUP_TABLE_NAME, CUSTOMER_GROUP_ID_PROPERTY);
    private static final String DELETE_USER_ID_FROM_USER_GROUP = String.format("DELETE FROM %s WHERE %s = ?", USER_GROUP_TABLE_NAME, CUSTOMER_GROUP_ID_PROPERTY);
    private static final String SELECT_GROUP_IDS_FOR_USER_ID = String.format("SELECT %s FROM %s WHERE %s = ?", CUSTOMER_GROUP_ID_PROPERTY, USER_GROUP_TABLE_NAME, USER_ID_PROPERTY);
    private static final String INSERT_USER_GROUPS = String.format("INSERT INTO %s  (%s, %s) VALUES (?, ?)", USER_GROUP_TABLE_NAME, USER_ID_PROPERTY, CUSTOMER_GROUP_ID_PROPERTY);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private CustomerGroupRepository customerGroupRepository;

    @Override
    public List<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        return DaoUtil.convertDataList(customerGroupRepository.findByTenantIdAndCustomerId(
                UUIDConverter.fromTimeUUID(tenantId),
                UUIDConverter.fromTimeUUID(customerId),
                Objects.toString(pageLink.getTextSearch(), ""),
                pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR : UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    public Optional<CustomerGroup> findCustomerGroupsByTenantIdAndCustomerIdAndTitle(UUID tenantId, UUID customerId, String title) {

        CustomerGroup customerGroup = DaoUtil.getData(
                customerGroupRepository.findByTenantIdAndCustomerIdAndTitle(UUIDConverter.fromTimeUUID(tenantId), UUIDConverter.fromTimeUUID(customerId), title)
        );
        return Optional.ofNullable(customerGroup);
    }

    @Override
    public List<UserId> findUserIdsByCustomerGroupId(UUID customerGroupId) {
        return jdbcTemplate.query(QUERY_TO_FETCH_USER_ID_BY_GROUP_ID, new Object[]{UUIDConverter.fromTimeUUID(customerGroupId)},
                (ResultSet rs, int rowNum) -> UserId.fromString(rs.getString(USER_ID_PROPERTY)));
    }

    @Override
    public void deleteUserIdsForCustomerGroupId(UUID customerGroupId) {
        jdbcTemplate.update(DELETE_USER_ID_FROM_USER_GROUP, UUIDConverter.fromTimeUUID(customerGroupId));
    }

    @Override
    public List<CustomerGroup> findByUserId(UUID userId, TextPageLink textPageLink) {
        List<CustomerGroupId> customerGroupIds = jdbcTemplate.query(SELECT_GROUP_IDS_FOR_USER_ID, new Object[]{UUIDConverter.fromTimeUUID(userId)},
                (ResultSet rs, int rowNum) -> CustomerGroupId.fromString(rs.getString(CUSTOMER_GROUP_ID_PROPERTY)));
        List<String> customerGroupIdsStr = customerGroupIds.stream().map(id -> id.getId().toString()).collect(Collectors.toList());
        List<CustomerGroupEntity> customerGroupEntities = customerGroupRepository.findByIdIn(customerGroupIdsStr , new PageRequest(0 , textPageLink.getLimit()));
        return customerGroupEntities.stream().map(CustomerGroupEntity::toData).collect(Collectors.toList());
    }

    @Override
    public void assignUsers(CustomerGroupId customerGroupId, List<UserId> userIds) {

        List<String> userIdsStr = userIds.stream().map(userId -> UUIDConverter.fromTimeUUID(userId.getId())).collect(Collectors.toList());

        jdbcTemplate.batchUpdate(INSERT_USER_GROUPS, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                String userIdStr = userIdsStr.get(i);
                ps.setString(1, userIdStr);
                ps.setString(2, UUIDConverter.fromTimeUUID(customerGroupId.getId()));
            }

            @Override
            public int getBatchSize() {
                return userIds.size();
            }
        });
    }

    @Override
    public void assignGroups(UserId userId , List<CustomerGroupId> customerGroupIds) {
        List<String> groupIdsStr = customerGroupIds.stream().map(groupId -> UUIDConverter.fromTimeUUID(groupId.getId())).collect(Collectors.toList());

        jdbcTemplate.batchUpdate(INSERT_USER_GROUPS , new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps , int i) throws SQLException {
                String groupIdStr = groupIdsStr.get(i);
                ps.setString(1, UUIDConverter.fromTimeUUID(userId.getId()));
                ps.setString(2, groupIdStr);
            }

            @Override
            public int getBatchSize() {
                return customerGroupIds.size();
            }
        });
    }

    @Override
    protected Class<CustomerGroupEntity> getEntityClass() {
        return CustomerGroupEntity.class;
    }

    @Override
    protected CrudRepository<CustomerGroupEntity, String> getCrudRepository() {
        return customerGroupRepository;
    }


}
