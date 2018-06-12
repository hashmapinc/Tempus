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
import com.hashmapinc.server.common.data.DataModel;
import com.hashmapinc.server.common.data.DataModelObject.AttributeDefinition;
import com.hashmapinc.server.common.data.DataModelObject.DataModelObject;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kv.DataType;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class BaseAttributeDefinitionServiceTest extends AbstractServiceTest {

    private TenantId tenantId;
    private DataModelId dataModelId;
    private DataModelObjectId dataModelObjectId;
    private AttributeDefinition attributeDefinition;

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

        DataModelObject dataModelObject = new DataModelObject();
        dataModelObject.setName("well");
        dataModelObject.setCustomerId(new CustomerId(UUIDs.timeBased()));
        dataModelObject.setDataModelId(dataModelId);

        DataModelObject savedDataModelObj = dataModelObjectService.save(dataModelObject);
        Assert.assertNotNull(savedDataModelObj);
        dataModelObjectId = savedDataModelObj.getId();

        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setDataModelObjectId(dataModelObjectId);
        attributeDef.setName("Version");
        attributeDef.setValue("1.0");
        attributeDef.setValueType(DataType.STRING.name());

        attributeDefinition = attributeDefinitionService.save(attributeDef);
        assertNotNull(attributeDefinition);
    }

    @After
    public void after() {
        tenantService.deleteTenant(tenantId);
        dataModelObjectService.deleteById(dataModelObjectId);
        attributeDefinitionService.deleteById(attributeDefinition.getId());
    }

    @Test
    public void testFindById() throws Exception {
        AttributeDefinition foundAttributeDefinitionById = attributeDefinitionService.findById(attributeDefinition.getId());
        assertEquals(attributeDefinition.getName(), foundAttributeDefinitionById.getName());
    }

    @Test
    public void testFindByDataModelObjectId() throws Exception {
        List<AttributeDefinition> attributeDefinitions = attributeDefinitionService.findByDataModelObjectId(dataModelObjectId);
        assertEquals(1, attributeDefinitions.size());
    }

    @Test
    public void testSaveAttributeDefinitionWithInvalidName() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("Attribute name should be specified!");

        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setDataModelObjectId(dataModelObjectId);
        attributeDef.setName(null);
        attributeDef.setValue("1.0");
        attributeDef.setValueType(DataType.STRING.name());

        attributeDefinitionService.save(attributeDef);
    }

    @Test
    public void testSaveAttributeDefinitionWithInvalidValue() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("Attribute value should be specified!");

        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setDataModelObjectId(dataModelObjectId);
        attributeDef.setName("Version");
        attributeDef.setValue("");
        attributeDef.setValueType(DataType.STRING.name());

        attributeDefinitionService.save(attributeDef);
    }

    @Test
    public void testSaveAttributeDefinitionWithoutValueType() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("A Valid attribute value type should be specified!");

        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setDataModelObjectId(dataModelObjectId);
        attributeDef.setName("Version");
        attributeDef.setValue("1.0");

        attributeDefinitionService.save(attributeDef);
    }

    @Test
    public void testSaveAttributeDefinitionWithInvalidValueType() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("A Valid attribute value type should be specified!");

        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setDataModelObjectId(dataModelObjectId);
        attributeDef.setName("Version");
        attributeDef.setValue("1.0");
        attributeDef.setValueType("invalid type");

        attributeDefinitionService.save(attributeDef);
    }

    @Test
    public void testSaveAttributeDefinitionWithoutModelObjectId() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("Attribute definition should be assigned to a data model object!");

        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setName("Version");
        attributeDef.setValue("1.0");
        attributeDef.setValueType(DataType.STRING.name());

        attributeDefinitionService.save(attributeDef);
    }

    @Test
    public void testSaveAttributeDefinitionWithInvalidModelObjectId() throws Exception {
        expectedEx.expect(DataValidationException.class);
        expectedEx.expectMessage("Attribute definition is referencing to non-existent data model object!");

        AttributeDefinition attributeDef = new AttributeDefinition();
        attributeDef.setDataModelObjectId(new DataModelObjectId(UUIDs.timeBased()));
        attributeDef.setName("Version");
        attributeDef.setValue("1.0");
        attributeDef.setValueType(DataType.STRING.name());

        attributeDefinitionService.save(attributeDef);
    }
}
