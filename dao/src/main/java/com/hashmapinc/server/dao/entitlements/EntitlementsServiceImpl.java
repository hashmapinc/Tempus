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
package com.hashmapinc.server.dao.entitlements;

import com.hashmapinc.server.common.data.Entitlements;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.EntitledServices;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Slf4j
@Service
public class EntitlementsServiceImpl extends AbstractEntityService implements EntitlementsService {

    @Autowired
    private EntitlementsDao entitlementsDao;

    @Override
    public Optional<Entitlements> findEntitlementsForUserId(UserId userId) {
        log.trace("Executing findEntitlementsByUserId [{}]", userId);
        validateId(userId, "Incorrect User Id " + userId);
        return entitlementsDao.findEntitlementsByUserId(userId.getId());
    }

    @Override
    public Entitlements save(Entitlements entitlements) {
        return entitlementsDao.save(entitlements);
    }
}
