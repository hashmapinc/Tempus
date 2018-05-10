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
package com.hashmapinc.server.dao.attributes;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.common.data.DeviceDataSet;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.kv.AttributeKvEntry;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.service.Validator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Service
public class BaseAttributesService implements AttributesService {

    @Autowired
    private AttributesDao attributesDao;

    @Override
    public ListenableFuture<Optional<AttributeKvEntry>> find(EntityId entityId, String scope, String attributeKey) {
        validate(entityId, scope);
        Validator.validateString(attributeKey, "Incorrect attribute key " + attributeKey);
        return attributesDao.find(entityId, scope, attributeKey);
    }

    @Override
    public ListenableFuture<List<AttributeKvEntry>> find(EntityId entityId, String scope, Collection<String> attributeKeys) {
        validate(entityId, scope);
        attributeKeys.forEach(attributeKey -> Validator.validateString(attributeKey, "Incorrect attribute key " + attributeKey));
        return attributesDao.find(entityId, scope, attributeKeys);
    }

    @Override
    public ListenableFuture<List<AttributeKvEntry>> findAll(EntityId entityId, String scope) {
        validate(entityId, scope);
        return attributesDao.findAll(entityId, scope);
    }

    @Override
    public ListenableFuture<List<Void>> save(EntityId entityId, String scope, List<AttributeKvEntry> attributes) {
        validate(entityId, scope);
        attributes.forEach(attribute -> validate(attribute));
        List<ListenableFuture<Void>> futures = Lists.newArrayListWithExpectedSize(attributes.size());
        for (AttributeKvEntry attribute : attributes) {
            futures.add(attributesDao.save(entityId, scope, attribute));
        }
        return Futures.allAsList(futures);
    }

    @Override
    public ListenableFuture<List<Void>> removeAll(EntityId entityId, String scope, List<String> keys) {
        validate(entityId, scope);
        return attributesDao.removeAll(entityId, scope, keys);
    }

    @Override
    public DeviceDataSet findAll(EntityId entityId) {
        validate(entityId);
        return attributesDao.findAll(entityId);
    }

    private static void validate(EntityId id, String scope) {
        Validator.validateId(id.getId(), "Incorrect id " + id);
        Validator.validateString(scope, "Incorrect scope " + scope);
    }

    private static void validate(AttributeKvEntry kvEntry) {
        if (kvEntry == null) {
            throw new IncorrectParameterException("Key value entry can't be null");
        } else if (kvEntry.getDataType() == null) {
            throw new IncorrectParameterException("Incorrect kvEntry. Data type can't be null");
        } else {
            Validator.validateString(kvEntry.getKey(), "Incorrect kvEntry. Key can't be empty");
            Validator.validatePositiveNumber(kvEntry.getLastUpdateTs(), "Incorrect last update ts. Ts should be positive");
        }
    }

    private static void validate(EntityId entityId) {
        Validator.validateEntityId(entityId, "Incorrect entityId " + entityId);
    }

}
