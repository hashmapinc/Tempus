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
package com.hashmapinc.server.dao.service.datamodelobject;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.DataModelObject.DataModelObject;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class DataDataModelObjectServiceImpTest extends AbstractServiceTest {

    @Test
    public void createModelObject() throws Exception {
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("well");
        dataModelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObject.setTenantId(new TenantId(UUIDs.timeBased()));
        dataModelObject.setDataModelId(new DataModelId(UUIDs.timeBased()));
        dataModelObject.setParentId(null);

        DataModelObject result = dataModelObjectService.save(dataModelObject);
        DataModelObject result2 = dataModelObjectService.findById(result.getId());
        assertNotNull(result2);
        assertEquals(result.getName(), result2.getName());
    }

    @Test
    public void findAllModelObjectsForTenant() throws Exception {

        CustomerId customerId = new CustomerId(UUIDs.timeBased());
        TenantId tenantId = new TenantId(UUIDs.timeBased());
        DataModelId dataModelId = new DataModelId(UUIDs.timeBased());
        DataModelObject dataModelObject = new DataModelObject();

        dataModelObject.setName("well-1");
        dataModelObject.setCustomerId(customerId);
        dataModelObject.setTenantId(tenantId);
        dataModelObject.setDataModelId(dataModelId);
        dataModelObject.setParentId(null);

        dataModelObjectService.save(dataModelObject);

        dataModelObject.setName("well-2");
        dataModelObject.setCustomerId(customerId);
        dataModelObject.setTenantId(tenantId);
        dataModelObject.setDataModelId(dataModelId);
        dataModelObject.setParentId(null);

        dataModelObjectService.save(dataModelObject);

        List<DataModelObject> dataModelObjectList = dataModelObjectService.findByTenantId(tenantId);
        assertEquals(2, dataModelObjectList.size());
    }

    @Test
    public void deleteModelObjectById() throws Exception {
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("well");
        dataModelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObject.setTenantId(new TenantId(UUIDs.timeBased()));
        dataModelObject.setDataModelId(new DataModelId(UUIDs.timeBased()));
        dataModelObject.setParentId(null);

        DataModelObject result = dataModelObjectService.save(dataModelObject);
        boolean status = dataModelObjectService.deleteById(result.getId());

        assertEquals(true, status);
    }
}
