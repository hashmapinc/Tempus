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
package com.hashmapinc.server.dao.datamodel;


import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.datamodel.DataModel;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.datamodel.DataModelObject;
import com.hashmapinc.server.common.data.id.DataModelId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.asset.AssetDao;
import com.hashmapinc.server.dao.dashboard.DashboardService;
import com.hashmapinc.server.dao.device.DeviceService;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.service.DataValidator;
import com.hashmapinc.server.dao.tenant.TenantDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class DataModelServiceImpl extends AbstractEntityService implements DataModelService {

    public static final String INCORRECT_DATA_MODEL_ID = "Incorrect dataModelId ";
    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";

    @Autowired
    private DataModelDao dataModelDao;

    @Autowired
    private DataModelObjectService dataModelObjectService;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private AssetDao assetDao;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DeviceService deviceService;

    private Map<UUID,Boolean> marked;
    private Stack<UUID> topologicalOrder; //NOSONAR (Needed synchronous deletion of DataModelObject


    @Override
    public DataModel saveDataModel(DataModel dataModel) {
        log.trace("Executing saveDataModel [{}]", dataModel);
        dataModelValidator.validate(dataModel);
        return dataModelDao.save(dataModel);
    }

    @Override
    public DataModel findById(DataModelId id) {
        log.trace("Executing DataModelServiceImpl.findById [{}]", id);
        validateId(id, INCORRECT_DATA_MODEL_ID + id);
        return dataModelDao.findById(id.getId());
    }

    @Override
    public List<DataModel> findByTenantId(TenantId tenantId) {
        log.trace("Executing DataModelServiceImpl.findByTenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return dataModelDao.findByTenantId(tenantId.getId());
    }

    @Override
    public void deleteById(DataModelId dataModelId) {
        log.trace("Executing DataModelServiceImpl.deleteById [{}]", dataModelId);
        validateId(dataModelId, INCORRECT_DATA_MODEL_ID + dataModelId);
        removeDependenciesOfDataModel(dataModelId);
        dataModelDao.removeById(dataModelId.getId());
    }

    private void removeDependenciesOfDataModel(DataModelId dataModelId) {
        List<DataModelObject> dataModelObjects = dataModelObjectService.findByDataModelId(dataModelId);
        dataModelObjects.forEach(dataModelObject -> {
            if(dataModelObject != null){
                List<Asset> assets = assetDao.findAssetsByDataModelObjectId(dataModelObject.getId().getId());
                if(!assets.isEmpty())
                    throw new DataValidationException("Cannot delete the dataModel because one or more assets are associated with it's dataModelObjects");
                List<Device> devices = deviceService.findDeviceByDataModelObjectId(dataModelObject.getId());
                if(!devices.isEmpty())
                    throw new DataValidationException("Cannot delete dataModel because one or more devices are associated with it");

            }
        });

        generateTopologicalOrder(dataModelObjects);
        deleteDependencies(topologicalOrder);
    }

    private void deleteDependencies(Stack<UUID> topologicalOrder) {
        topologicalOrder.forEach(uuid -> dataModelObjectService.removeById(new DataModelObjectId(uuid)));
    }

    private void generateTopologicalOrder(List<DataModelObject> dataModelObjects) {
        Map<UUID, List<UUID>> adjList = getAdjacencyList(dataModelObjects);
        marked  = new HashMap<>();
        topologicalOrder = new Stack<>();
        //marked the processed node
        adjList.forEach((k,v) -> marked.put(k,false));
        //if graph have more than one parent
        marked.forEach((k,v) -> {
            if(!v){
                visit(k,adjList);
            }
        });
    }

    private Map<UUID, List<UUID>> getAdjacencyList(List<DataModelObject> dataModelObjects) {
        Map<UUID, List<UUID>> adjList = new HashMap<>();

        for(DataModelObject vertex : dataModelObjects) {
            if(!adjList.containsKey(vertex.getUuidId())) {
                List<UUID> neighbors = getNeighbors(dataModelObjects, vertex);
                adjList.put(vertex.getUuidId(),neighbors);
            }
        }
        return adjList;
    }

    private  List<UUID> getNeighbors(List<DataModelObject> nodes, DataModelObject vertex) {
        List<UUID> neighbors = new LinkedList<>();

        for (DataModelObject node : nodes) {
            if (node.getParentId() != null && node.getParentId().equals(vertex.getId())) {
                neighbors.add(node.getUuidId());
            }

        }
        return neighbors;
    }

    private void visit(UUID node ,Map<UUID, List<UUID>> adjList) {
        marked.put(node,true);
        for(UUID vertex : adjList.get(node)) {
            if(!marked.get(vertex))
                 visit(vertex,adjList);
        }
        topologicalOrder.push(node);
    }

    @Override
    public void deleteDataModelsByTenantId(TenantId tenantId) {
        log.trace("Executing DataModelServiceImpl.deleteDataModelsByTenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        removeEntities(tenantId);
    }

    private void removeEntities(TenantId tenantId) {
        List<DataModel> dataModels = dataModelDao.findByTenantId(tenantId.getId());
        dataModels.forEach(dataModel -> {
            if(dataModel != null){
                deleteById(dataModel.getId());
            }
        });
    }

    private DataValidator<DataModel> dataModelValidator = new DataValidator<DataModel>() {
        @Override
        protected void validateCreate(DataModel dataModel) {
            dataModelDao.findDataModelByTenantIdAndName(dataModel.getTenantId().getId(), dataModel.getName()).ifPresent(
                    d -> {
                        throw new DataValidationException("Data Model with such name already exists!");
                    }
            );
        }

        @Override
        protected void validateUpdate(DataModel dataModel) {
            dataModelDao.findDataModelByTenantIdAndName(dataModel.getTenantId().getId(), dataModel.getName()).ifPresent(
                    d -> {
                        if (!d.getUuidId().equals(dataModel.getUuidId())) {
                            throw new DataValidationException("Device with such name already exists!");
                        }
                    }
            );
        }
        @Override
        protected void validateDataImpl(DataModel dataModel) {
            if(StringUtils.isEmpty(dataModel.getName())) {
                throw new DataValidationException("Data Model name should be specified");
            }

            if (dataModel.getTenantId() == null) {
                throw new DataValidationException("Data Model should be assigned to tenant!");
            } else {
                Tenant tenant = tenantDao.findById(dataModel.getTenantId().getId());
                if (tenant == null) {
                    throw new DataValidationException("Data Model is referencing to non-existent tenant!");
                }
            }

            if(dataModel.getLastUpdatedTs() == null) {
                throw new DataValidationException("Data Model last updated time should be specified!");
            }
        }

    };
}
