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
package com.hashmapinc.server.dao.sql.tenant;

import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.sql.TenantEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;
import com.hashmapinc.server.dao.tenant.TenantDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

/**
 * Created by Valerii Sosliuk on 4/30/2017.
 */
@Component
public class JpaTenantDao extends JpaAbstractSearchTextDao<TenantEntity, Tenant> implements TenantDao {

    private static final String INSERT_USER_UNIT_SYSTEM = String.format("INSERT INTO %s  (%s, %s) VALUES (?, ?)", TENANT_UNIT_SYSTEM_TABLE , TENANT_ID_PROPERTY, UNIT_SYSTEM_PROPERTY);
    private static final String UPDATE_USER_UNIT_SYSTEM = String.format("UPDATE %s SET %s = ? WHERE %s = ?", TENANT_UNIT_SYSTEM_TABLE , UNIT_SYSTEM_PROPERTY, TENANT_ID_PROPERTY);
    private static final String SELECT_USER_SYSTEM_FOR_USER_ID = String.format("SELECT DISTINCT %s FROM %s WHERE %s = ?", UNIT_SYSTEM_PROPERTY, TENANT_UNIT_SYSTEM_TABLE , TENANT_ID_PROPERTY);
    private static final String DELETE_USER_SYSTEM_FOR_USER_ID = String.format("DELETE FROM %s WHERE %s = ?", TENANT_UNIT_SYSTEM_TABLE , TENANT_ID_PROPERTY);


    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    protected Class<TenantEntity> getEntityClass() {
        return TenantEntity.class;
    }

    @Override
    protected CrudRepository<TenantEntity, String> getCrudRepository() {
        return tenantRepository;
    }

    @Override
    public List<Tenant> findTenantsByRegion(String region, TextPageLink pageLink) {
        return DaoUtil.convertDataList(tenantRepository
                .findByRegionNextPage(
                        region,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getIdOffset() == null ? NULL_UUID_STR : UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                        new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    public void saveUnitSystem(String unitSystem , UUID userId) {
        jdbcTemplate.update(INSERT_USER_UNIT_SYSTEM , UUIDConverter.fromTimeUUID(userId) , unitSystem);
    }

    @Override
    public String findUnitSystemByTenantId(UUID userId) {
        List<String> unitList  = jdbcTemplate.query(SELECT_USER_SYSTEM_FOR_USER_ID, new Object[]{UUIDConverter.fromTimeUUID(userId)},
                (ResultSet rs, int rowNum) -> (rs.getString(UNIT_SYSTEM_PROPERTY)));
        if (unitList.size() == 1) {
            return unitList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void deleteUnitSystemByTenantId(UUID userId) {
        jdbcTemplate.update(DELETE_USER_SYSTEM_FOR_USER_ID, UUIDConverter.fromTimeUUID(userId));
    }

    @Override
    public void updateUnitSystem(String unitSystem , UUID userId) {
        jdbcTemplate.update(UPDATE_USER_UNIT_SYSTEM, unitSystem, UUIDConverter.fromTimeUUID(userId));
    }
}
