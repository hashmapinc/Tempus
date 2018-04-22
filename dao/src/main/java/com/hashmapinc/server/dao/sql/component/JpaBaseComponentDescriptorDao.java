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
package com.hashmapinc.server.dao.sql.component;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.plugin.ComponentDescriptor;
import com.hashmapinc.server.common.data.plugin.ComponentScope;
import com.hashmapinc.server.common.data.plugin.ComponentType;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.sql.ComponentDescriptorEntity;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.ComponentDescriptorId;
import com.hashmapinc.server.dao.component.ComponentDescriptorDao;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
@Component
@SqlDao
public class JpaBaseComponentDescriptorDao extends JpaAbstractSearchTextDao<ComponentDescriptorEntity, ComponentDescriptor>
        implements ComponentDescriptorDao {

    @Autowired
    private ComponentDescriptorRepository componentDescriptorRepository;

    @Override
    protected Class<ComponentDescriptorEntity> getEntityClass() {
        return ComponentDescriptorEntity.class;
    }

    @Override
    protected CrudRepository<ComponentDescriptorEntity, String> getCrudRepository() {
        return componentDescriptorRepository;
    }

    @Override
    public Optional<ComponentDescriptor> saveIfNotExist(ComponentDescriptor component) {
        if (component.getId() == null) {
            component.setId(new ComponentDescriptorId(UUIDs.timeBased()));
        }
        if (componentDescriptorRepository.findOne(UUIDConverter.fromTimeUUID(component.getId().getId())) == null) {
            return Optional.of(save(component));
        }
        return Optional.empty();
    }

    @Override
    public ComponentDescriptor findById(ComponentDescriptorId componentId) {
        return findById(componentId.getId());
    }

    @Override
    public ComponentDescriptor findByClazz(String clazz) {
        return DaoUtil.getData(componentDescriptorRepository.findByClazz(clazz));
    }

    @Override
    public List<ComponentDescriptor> findByTypeAndPageLink(ComponentType type, TextPageLink pageLink) {
        return DaoUtil.convertDataList(componentDescriptorRepository
                .findByType(
                        type,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR : UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                        new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    public List<ComponentDescriptor> findByScopeAndTypeAndPageLink(ComponentScope scope, ComponentType type, TextPageLink pageLink) {
        return DaoUtil.convertDataList(componentDescriptorRepository
                .findByScopeAndType(
                        type,
                        scope,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR : UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                        new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    @Transactional
    public void deleteById(ComponentDescriptorId componentId) {
        removeById(componentId.getId());
    }

    @Override
    @Transactional
    public void deleteByClazz(String clazz) {
        componentDescriptorRepository.deleteByClazz(clazz);
    }
}
