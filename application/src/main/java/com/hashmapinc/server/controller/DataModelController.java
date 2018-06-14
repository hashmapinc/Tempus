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
package com.hashmapinc.server.controller;

import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.exception.TempusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Slf4j
public class DataModelController extends BaseController {

    public static final String DATA_MODEL_ID = "dataModelId";

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/data-model", method = RequestMethod.POST)
    @ResponseBody
    public DataModel saveDataModel(@RequestBody DataModel dataModel) throws TempusException {
        dataModel.setTenantId(getCurrentUser().getTenantId());
        dataModel.setLastUpdatedTs(System.currentTimeMillis());
        try {
            DataModel savedDataModel = checkNotNull(dataModelService.saveDataModel(dataModel));
            logEntityAction(savedDataModel.getId(), savedDataModel,
                    null,
                    dataModel.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);
            return savedDataModel;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.DATA_MODEL), dataModel,
                    null, dataModel.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/data-model/{dataModelId}/objects", method = RequestMethod.POST)
    @ResponseBody
    public DataModelObject saveDataModelObject(@RequestBody DataModelObject dataModelObject,
                                               @PathVariable String dataModelId) throws TempusException {

        dataModelObject.setCustomerId(getCurrentUser().getCustomerId());
        dataModelObject.setDataModelId(new DataModelId(toUUID(dataModelId)));

        try {
            DataModelObject savedDataModelObj = checkNotNull(dataModelObjectService.save(dataModelObject));
            logEntityAction(savedDataModelObj.getId(), savedDataModelObj,
                    savedDataModelObj.getCustomerId(),
                    dataModelObject.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);
            return savedDataModelObj;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.DATA_MODEL_OBJECT), dataModelObject,
                    dataModelObject.getCustomerId(), dataModelObject.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/data-model/{dataModelId}/objects", method = RequestMethod.GET)
    @ResponseBody
    public List<DataModelObject> fetchDataModelObjectsByModel(@PathVariable("dataModelId") String dataModelId) throws TempusException {
        try {
            checkParameter("dataModelId", dataModelId);
            List<DataModelObject> dataModelObjects = dataModelObjectService.
                    findByDataModelId(new DataModelId(toUUID(dataModelId)));
            dataModelObjects = dataModelObjects.stream().filter(dataModelObject -> !dataModelObject.getId().equals(ModelConstants.NULL_UUID)).collect(Collectors.toList());
            return dataModelObjects;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/data-model/objects/{objectId}", method = RequestMethod.GET)
    @ResponseBody
    public DataModelObject fetchDataModelObjectById(@PathVariable("objectId") String objectId)
            throws TempusException {
        try {
            checkParameter("objectId", objectId);
            return  dataModelObjectService.findById(new DataModelObjectId(toUUID(objectId)));
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
