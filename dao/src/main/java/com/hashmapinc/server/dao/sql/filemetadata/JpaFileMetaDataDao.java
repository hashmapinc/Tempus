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
package com.hashmapinc.server.dao.sql.filemetadata;

import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.upload.FileMetaData;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.filemetadata.FileMetaDataDao;
import com.hashmapinc.server.dao.model.sql.FileMetaDataCompositeKey;
import com.hashmapinc.server.dao.model.sql.FileMetaDataEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class JpaFileMetaDataDao extends JpaAbstractDaoListeningExecutorService implements FileMetaDataDao {

    @Autowired
    FileMetaDataRepository fileMetaDataRepository;

    @Override
    public FileMetaData save(FileMetaData fileMetaData) {
        FileMetaDataEntity fileMetaDataEntity = new FileMetaDataEntity(fileMetaData);
        return fileMetaDataRepository.save(fileMetaDataEntity).toData();
    }

    @Override
    public List<FileMetaData> getFilesByTenantAndRelatedEntity(TenantId tenantId, EntityId entityId) {
        List<FileMetaData> fileMetaData = DaoUtil.convertDataList(fileMetaDataRepository.findByTenantIdAndRelatedEntityId(UUIDConverter.fromTimeUUID(tenantId.getId()),
                UUIDConverter.fromTimeUUID(entityId.getId())));
        return  fileMetaData;
    }

    @Override
    public List<FileMetaData> getFileMetaData(TenantId tenantId, EntityId entityId, String fileName, String extension) {
        List<FileMetaData> fileMetaDataList = new ArrayList<>();
        Optional<FileMetaDataEntity> entityOptional = fileMetaDataRepository.findById(new FileMetaDataCompositeKey(
                UUIDConverter.fromTimeUUID(tenantId.getId()),
                UUIDConverter.fromTimeUUID(entityId.getId()),
                fileName, extension));
        if (entityOptional.isPresent())
             fileMetaDataList.add(entityOptional.get().toData());
        return fileMetaDataList;
    }

    @Override
    public void delete(TenantId tenantId, EntityId entityId, String fileName, String extension) {
        fileMetaDataRepository.deleteById(new FileMetaDataCompositeKey(
                UUIDConverter.fromTimeUUID(tenantId.getId()),
                UUIDConverter.fromTimeUUID(entityId.getId()),
                fileName, extension));
    }

}
