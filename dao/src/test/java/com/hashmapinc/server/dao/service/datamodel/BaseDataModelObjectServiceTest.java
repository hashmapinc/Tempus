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
package com.hashmapinc.server.dao.service.datamodel;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class BaseDataModelObjectServiceTest extends AbstractServiceTest {

    private TenantId tenantId;
    private DataModelId dataModelId;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void before() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();

        DataModel dataModel = new DataModel();
        dataModel.setName("Test Data Model");
        dataModel.setLastUpdatedTs(System.currentTimeMillis());
        dataModel.setTenantId(tenantId);
        DataModel savedDataModel = dataModelService.saveDataModel(dataModel);
        Assert.assertNotNull(savedDataModel);
        dataModelId = savedDataModel.getId();
    }

    @After
    public void after() {
        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testCreateModelObject() throws Exception {
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("well");
        dataModelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObject.setType("well-type");
        dataModelObject.setDataModelId(dataModelId);
        dataModelObject.setParentId(null);

        DataModelObject result = dataModelObjectService.save(dataModelObject);
        DataModelObject result2 = dataModelObjectService.findById(result.getId());
        assertNotNull(result2);
        assertEquals(result.getName(), result2.getName());
    }

    @Test
    public void testFindAllModelObjectsForDataModel() throws Exception {

        CustomerId customerId = new CustomerId(UUIDs.timeBased());
        DataModelObject dataModelObject = new DataModelObject();

        dataModelObject.setName("well-1");
        dataModelObject.setCustomerId(customerId);
        dataModelObject.setDataModelId(dataModelId);
        dataModelObject.setParentId(null);

        dataModelObjectService.save(dataModelObject);

        dataModelObject.setName("well-2");
        dataModelObject.setCustomerId(customerId);
        dataModelObject.setDataModelId(dataModelId);
        dataModelObject.setParentId(null);

        dataModelObjectService.save(dataModelObject);

        List<DataModelObject> dataModelObjectList = dataModelObjectService.findByDataModelId(dataModelId);
        assertEquals(2, dataModelObjectList.size());
    }

    @Test
    public void testDeleteModelObjectById() throws Exception {
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("well");
        dataModelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObject.setDataModelId(dataModelId);
        dataModelObject.setParentId(null);

        DataModelObject result = dataModelObjectService.save(dataModelObject);
        boolean status = dataModelObjectService.deleteById(result.getId());

        assertEquals(true, status);
    }

    @Test(expected = IncorrectParameterException.class)
    public void testFindByNullModelObjectId() throws Exception {
        dataModelObjectService.findById(null);
    }

    @Test
    public void testSaveWithEmptyName() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("Data Model object name should be specified!");

        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("");
        dataModelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObject.setDataModelId(dataModelId);
        dataModelObject.setParentId(null);

        dataModelObjectService.save(dataModelObject);
    }

    @Test
    public void testSaveWithNullDataModelId() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("Data Model object should be assigned to a data model!");

        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("well");
        dataModelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObject.setDataModelId(null);
        dataModelObject.setParentId(null);

        dataModelObjectService.save(dataModelObject);
    }

    @Test
    public void testSaveWithInvalidDataModelId() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("Data Model object is referencing to non-existent data model!");

        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("well");
        dataModelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObject.setDataModelId(new DataModelId(UUIDs.timeBased()));
        dataModelObject.setParentId(null);
        dataModelObjectService.save(dataModelObject);
    }

}
