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
package com.hashmapinc.server.dao.sql.entitlements;

import com.hashmapinc.server.common.data.Entitlements;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.entitlements.EntitlementsDao;
import com.hashmapinc.server.dao.model.sql.EntitlementsEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDao;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Component
@SqlDao
@Slf4j
public class JPAEntitlementsDao  extends JpaAbstractDao<EntitlementsEntity, Entitlements> implements EntitlementsDao {

    @Autowired
    private EntitlementsRepository entitlementsRepository;

    @Override
    public Optional<Entitlements> findEntitlementsByUserId(UUID userId) {
        Entitlements entitlements = DaoUtil.getData(entitlementsRepository.findByUserId(UUIDConverter.fromTimeUUID(userId)));
        return Optional.ofNullable(entitlements);
    }

    @Override
    protected Class<EntitlementsEntity> getEntityClass() {
        return EntitlementsEntity.class;
    }

    @Override
    protected CrudRepository<EntitlementsEntity, String> getCrudRepository() {
        return entitlementsRepository;
    }
}
