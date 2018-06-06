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
package com.hashmapinc.server.dao.modelobjects;

import com.hashmapinc.server.common.data.ModelObject;
import com.hashmapinc.server.common.data.id.ModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseModelObjectService implements  ModelObjectService{

    @Autowired
    ModelObjectDao modelObjectDao;

    @Override
    public ModelObject save(ModelObject modelObject) {
        return modelObjectDao.save(modelObject);
    }

    @Override
    public ModelObject findById(ModelObjectId modelObjectId) {
        return modelObjectDao.findById(modelObjectId);
    }

    @Override
    public List<ModelObject> findByTenantId(TenantId tenantId) {
        return modelObjectDao.findByTenantId(tenantId);
    }

    @Override
    public boolean deleteById(ModelObjectId modelObjectId) {
        return modelObjectDao.removeById(modelObjectId.getId());
    }
}
