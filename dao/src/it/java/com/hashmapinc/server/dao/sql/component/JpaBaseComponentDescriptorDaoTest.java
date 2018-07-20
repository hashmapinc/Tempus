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
import com.hashmapinc.server.dao.AbstractJpaDaoTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.hashmapinc.server.common.data.id.ComponentDescriptorId;
import com.hashmapinc.server.dao.component.ComponentDescriptorDao;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
public class JpaBaseComponentDescriptorDaoTest extends AbstractJpaDaoTest {

    @Autowired
    private ComponentDescriptorDao componentDescriptorDao;

    @Test
    public void findByType() {
        for (int i = 0; i < 20; i++) {
            createComponentDescriptor(ComponentType.PLUGIN, ComponentScope.SYSTEM, i);
            createComponentDescriptor(ComponentType.ACTION, ComponentScope.TENANT, i + 20);
        }

        TextPageLink pageLink1 = new TextPageLink(15, "COMPONENT_");
        List<ComponentDescriptor> components1 = componentDescriptorDao.findByTypeAndPageLink(ComponentType.PLUGIN, pageLink1);
        assertEquals(15, components1.size());

        TextPageLink pageLink2 = new TextPageLink(15, "COMPONENT_", components1.get(14).getId().getId(), null);
        List<ComponentDescriptor> components2 = componentDescriptorDao.findByTypeAndPageLink(ComponentType.PLUGIN, pageLink2);
        assertEquals(5, components2.size());
    }

    @Test
    public void findByTypeAndSocpe() {
        for (int i = 0; i < 20; i++) {
            createComponentDescriptor(ComponentType.PLUGIN, ComponentScope.SYSTEM, i);
            createComponentDescriptor(ComponentType.ACTION, ComponentScope.TENANT, i + 20);
            createComponentDescriptor(ComponentType.FILTER, ComponentScope.SYSTEM, i + 40);
        }

        TextPageLink pageLink1 = new TextPageLink(15, "COMPONENT_");
        List<ComponentDescriptor> components1 = componentDescriptorDao.findByScopeAndTypeAndPageLink(
                ComponentScope.SYSTEM, ComponentType.FILTER, pageLink1);
        assertEquals(15, components1.size());

        TextPageLink pageLink2 = new TextPageLink(15, "COMPONENT_", components1.get(14).getId().getId(), null);
        List<ComponentDescriptor> components2 = componentDescriptorDao.findByScopeAndTypeAndPageLink(
                ComponentScope.SYSTEM, ComponentType.FILTER, pageLink2);
        assertEquals(5, components2.size());
    }

    private void createComponentDescriptor(ComponentType type, ComponentScope scope, int index) {
        ComponentDescriptor component = new ComponentDescriptor();
        component.setId(new ComponentDescriptorId(UUIDs.timeBased()));
        component.setType(type);
        component.setScope(scope);
        component.setName("COMPONENT_" + index);
        componentDescriptorDao.save(component);
    }

}
