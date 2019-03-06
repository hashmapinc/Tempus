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
package com.hashmapinc.server.dao.model.sql;

import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.upload.FileMetaData;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.ToData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;


@Data
@EqualsAndHashCode
@Entity
@Table(name = ModelConstants.FILE_META_DATA_TABLE_NAME)
@IdClass(FileMetaDataCompositeKey.class)
public class FileMetaDataEntity implements ToData<FileMetaData>, Serializable {

    @Id
    @Column(name = ModelConstants.TENANT_ID_PROPERTY)
    private String tenantId;

    @Id
    @Column(name = ModelConstants.FILE_META_DATA_RELATED_ENTITY)
    private String relatedEntity;

    @Id
    @Column(name = ModelConstants.FILE_META_DATA_FILE_NAME)
    private String fileName;

    @Column(name = ModelConstants.FILE_META_DATA_FILE_EXETENSION)
    private String extension;

    @Column(name = ModelConstants.FILE_META_DATA_FILE_LAST_UPDATED)
    private long lastUpdated;

    @Column(name = ModelConstants.FILE_META_DATA_FILE_SIZE)
    private double size;

    public FileMetaDataEntity() { }

    public FileMetaDataEntity(FileMetaData fileMetaData) {
        if (fileMetaData.getFileName() != null)
            this.fileName = fileMetaData.getFileName();
        if (fileMetaData.getExtension() != null)
            this.extension = fileMetaData.getExtension();
        if (fileMetaData.getRelatedEntity() != null)
            this.relatedEntity = UUIDConverter.fromTimeUUID(fileMetaData.getRelatedEntity().getId());
        if (fileMetaData.getTenantId() != null)
            this.tenantId = UUIDConverter.fromTimeUUID(fileMetaData.getTenantId().getId());
        this.lastUpdated = fileMetaData.getLastUpdated();
        this.size = fileMetaData.getSize();
    }

    @Override
    public FileMetaData toData() {
        FileMetaData fileMetaData = new FileMetaData();
        if (tenantId != null)
            fileMetaData.setTenantId(new TenantId(UUIDConverter.fromString(tenantId)));
        if (relatedEntity != null)
            fileMetaData.setRelatedEntity(null);
        if (fileName != null)
            fileMetaData.setFileName(fileName);
        if (extension != null)
            fileMetaData.setExtension(extension);

        fileMetaData.setLastUpdated(lastUpdated);
        fileMetaData.setSize(size);
        return fileMetaData;
    }

    private EntityId createRelatedEntity(String relatedEntity) {
        return new EntityId() {
            @Override
            public UUID getId() {
                return UUIDConverter.fromString(relatedEntity);
            }

            @Override
            public EntityType getEntityType() {
                return null;
            }
        };
    }
}
