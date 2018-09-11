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
package com.hashmapinc.server.dao.service.datamodel;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.datamodel.AttributeDefinition;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.DataType;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class BaseDataModelObjectServiceTest extends AbstractServiceTest {

    private TenantId tenantId;
    private DataModelId dataModelId;
    private DataModelObject dataModelObject;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void before() {
        createTenant();
        createDataModel();
        createDataModelObject();
    }

    @After
    public void after() {
        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testCreateModelObject() throws Exception {
        DataModelObject result = dataModelObjectService.findById(dataModelObject.getId());
        assertNotNull(result);
        assertEquals(dataModelObject.getName(), result.getName());
        assertEquals(1, dataModelObject.getAttributeDefinitions().size());
    }

    @Test
    public void testFindAllModelObjectsForDataModel() throws Exception {
        CustomerId customerId = new CustomerId(UUIDs.timeBased());
        DataModelObject dataModelObject = new DataModelObject();

        dataModelObject.setName("well-2");
        dataModelObject.setCustomerId(customerId);
        dataModelObject.setDataModelId(dataModelId);
        dataModelObject.setParentId(null);

        dataModelObjectService.save(dataModelObject);

        List<DataModelObject> dataModelObjectList = dataModelObjectService.findByDataModelId(dataModelId);
        assertEquals(2, dataModelObjectList.size());
    }

    @Test(expected = IncorrectParameterException.class)
    public void testFindByNullModelObjectId() throws Exception {
        dataModelObjectService.findById(null);
    }

    @Test
    public void testSaveWithEmptyDataModelObjectName() throws Exception {
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
        dataModelObject.setName("well-3");
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
        dataModelObject.setName("well-4");
        dataModelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObject.setDataModelId(new DataModelId(UUIDs.timeBased()));
        dataModelObject.setParentId(null);
        dataModelObjectService.save(dataModelObject);
    }

    @Test
    public void testSaveAttributeDefinitionWithInvalidName() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("Attribute name should be specified!");

        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setName(null);
        attributeDef.setValue("1.0");
        attributeDef.setValueType(DataType.STRING.name());

        DataModelObject dataModelObject = getDataModelObjectWithOneAttributeDef(attributeDef);
        dataModelObject.setName("well-2");
        dataModelObjectService.save(dataModelObject);
    }

    @Test
    public void testSaveAttributeDefinitionWithoutValueType() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("A Valid attribute value type should be specified!");

        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setName("Version");
        attributeDef.setValue("1.0");

        DataModelObject dataModelObject = getDataModelObjectWithOneAttributeDef(attributeDef);
        dataModelObject.setName("well-2");
        dataModelObjectService.save(dataModelObject);
    }

    @Test
    public void testSaveAttributeDefinitionWithInvalidValueType() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("A Valid attribute value type should be specified!");

        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setName("Version");
        attributeDef.setValue("1.0");
        attributeDef.setValueType("invalid type");

        DataModelObject dataModelObject = getDataModelObjectWithOneAttributeDef(attributeDef);
        dataModelObject.setName("well-2");
        dataModelObjectService.save(dataModelObject);
    }

    @Test
    public void testRemoveByIdShouldAlsoDeleteAttributeDefinition() {
        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setName("vikash");
        attributeDef.setValue("1.0");
        attributeDef.setValueType(DataType.STRING.name());

        DataModelObject dataModelObject = getDataModelObjectWithOneAttributeDef(attributeDef);
        dataModelObject.setName("well-2");
        DataModelObject savedDataModelObject = dataModelObjectService.save(dataModelObject);

        dataModelObjectService.removeById(savedDataModelObject.getId());
        Assert.assertNull(attributeDefinitionDao.findByNameAndDataModelObjectId("lat",savedDataModelObject.getDataModelId().getId()));
    }

    @Test
    public void testDeleteDataModelObjectsByDataModelId() {
        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("rig");
        dataModelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObject.setDataModelId(dataModelId);
        dataModelObject.setParentId(null);
        dataModelObjectService.save(dataModelObject);

        List<DataModelObject> savedDataModelObjects = dataModelObjectService.findByDataModelId(dataModelId);
        Assert.assertEquals(2,savedDataModelObjects.size());

        dataModelObjectService.deleteDataModelObjectsByDataModelId(dataModelId);
        List<DataModelObject> foundDataModelObjects = dataModelObjectService.findByDataModelId(dataModelId);
        Assert.assertEquals(0,foundDataModelObjects.size());
    }

    private void createDataModelObject() {
        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setName("Version");
        attributeDef.setValue("1.0");
        attributeDef.setValueType(DataType.STRING.name());

        DataModelObject dataModelObj = getDataModelObjectWithOneAttributeDef(attributeDef);
        dataModelObj.setName("well-1");
        dataModelObject = dataModelObjectService.save(dataModelObj);
        Assert.assertNotNull(dataModelObject);
        assertEquals(1, dataModelObject.getAttributeDefinitions().size());
    }

    private DataModelObject getDataModelObjectWithOneAttributeDef(AttributeDefinition attributeDefinition) {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(attributeDefinition);

        DataModelObject dataModelObj = new DataModelObject();
        dataModelObj.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObj.setType("well-type");
        dataModelObj.setDataModelId(dataModelId);
        dataModelObj.setParentId(null);
        dataModelObj.setAttributeDefinitions(attributeDefinitions);
        return dataModelObj;
    }

    private void createDataModel() {
        DataModel dataModel1 = new DataModel();
        dataModel1.setName("Test Data Model");
        dataModel1.setLastUpdatedTs(System.currentTimeMillis());
        dataModel1.setTenantId(tenantId);
        DataModel dataModel = dataModel1;
        DataModel savedDataModel = dataModelService.saveDataModel(dataModel);
        Assert.assertNotNull(savedDataModel);
        dataModelId = savedDataModel.getId();
    }

    private void createTenant() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();
    }

}
