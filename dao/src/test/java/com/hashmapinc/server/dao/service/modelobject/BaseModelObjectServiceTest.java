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
package com.hashmapinc.server.dao.service.modelobject;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.ModelObject;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.ModelId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class BaseModelObjectServiceTest extends AbstractServiceTest {

    @Test
    public void createModelObject() throws Exception {
        ModelObject modelObject = new ModelObject();
        modelObject.setName("well");
        modelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        modelObject.setTenantId(new TenantId(UUIDs.timeBased()));
        modelObject.setModelId(new ModelId(UUIDs.timeBased()));
        modelObject.setParentId(null);

        ModelObject result = modelObjectService.save(modelObject);
        ModelObject result2 = modelObjectService.findById(result.getId());
        assertNotNull(result2);
        assertEquals(result.getName(), result2.getName());
    }

    @Test
    public void findAllModelObjectsForTenant() throws Exception {

        CustomerId customerId = new CustomerId(UUIDs.timeBased());
        TenantId tenantId = new TenantId(UUIDs.timeBased());
        ModelId modelId = new ModelId(UUIDs.timeBased());
        ModelObject modelObject = new ModelObject();

        modelObject.setName("well-1");
        modelObject.setCustomerId(customerId);
        modelObject.setTenantId(tenantId);
        modelObject.setModelId(modelId);
        modelObject.setParentId(null);

        modelObjectService.save(modelObject);

        modelObject.setName("well-2");
        modelObject.setCustomerId(customerId);
        modelObject.setTenantId(tenantId);
        modelObject.setModelId(modelId);
        modelObject.setParentId(null);

        modelObjectService.save(modelObject);

        List<ModelObject> modelObjectList= modelObjectService.findByTenantId(tenantId);
        assertEquals(2, modelObjectList.size());
    }

    @Test
    public void deleteModelObjectById() throws Exception {
        ModelObject modelObject = new ModelObject();
        modelObject.setName("well");
        modelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        modelObject.setTenantId(new TenantId(UUIDs.timeBased()));
        modelObject.setModelId(new ModelId(UUIDs.timeBased()));
        modelObject.setParentId(null);

        ModelObject result = modelObjectService.save(modelObject);
        boolean status = modelObjectService.deleteById(result.getId());

        assertEquals(true, status);
    }
}
