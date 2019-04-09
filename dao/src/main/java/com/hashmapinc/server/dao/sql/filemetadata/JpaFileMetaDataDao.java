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

import com.hashmapinc.server.common.data.FileCriteriaSpec;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.PaginatedResult;
import com.hashmapinc.server.common.data.upload.FileMetaData;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.filemetadata.FileMetaDataDao;
import com.hashmapinc.server.dao.model.sql.FileMetaDataCompositeKey;
import com.hashmapinc.server.dao.model.sql.FileMetaDataEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

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
    public List<FileMetaData> getFiles(TenantId tenantId, EntityId entityId) {
        List<FileMetaData> fileMetaData = DaoUtil.convertDataList(fileMetaDataRepository.findByTenantIdAndRelatedEntityId(UUIDConverter.fromTimeUUID(tenantId.getId()),
                UUIDConverter.fromTimeUUID(entityId.getId())));
        return  fileMetaData;
    }

    @Override
    public List<FileMetaData> getFiles(TenantId tenantId, EntityId entityId, String fileName, String extension) {
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
    public PaginatedResult<FileMetaData> findByTenantIdAndRelatedEntityIdAndSearchText(TenantId tenantId, EntityId entityId , FileCriteriaSpec fileCriteriaSpec) {
        String limit = fileCriteriaSpec.getLimit().orElse(FILE_META_DATA_DEFAULT_LIMIT);
        String pageNum = fileCriteriaSpec.getPageNum().orElse(FILE_META_DATA_DEFAULT_PAGE_NUM);
        String sortBy = fileCriteriaSpec.getSortBy().orElse(FILE_META_DATA_DEFAULT_SORT_BY);
        String orderBy = fileCriteriaSpec.getOrderBy().orElse(FILE_META_DATA_DEFAULT_ORDER_BY);
        String searchText = fileCriteriaSpec.getSearchText().orElse(FILE_META_DATA_DEFAULT_SEARCH_TEXT);

        final Pageable pageable = PageRequest.of(Integer.parseInt(pageNum), Integer.parseInt(limit), Sort.Direction.fromString(orderBy), sortBy);

        final Page<FileMetaDataEntity> page = fileMetaDataRepository.findByTenantIdAndRelatedEntityIdAndFileNameStartingWithIgnoreCase(UUIDConverter.fromTimeUUID(tenantId.getId()),
                UUIDConverter.fromTimeUUID(entityId.getId()),
                searchText,
                pageable);
        final List<FileMetaData> fileMetaData = page != null ? DaoUtil.convertDataList(page.getContent()) : Collections.emptyList();
        final long totalElements = page != null ? page.getTotalElements() : 0;
        final int totalPages = page != null ? page.getTotalPages() : 0;
        final boolean hasNext = page != null && page.hasNext();
        final boolean hasPrevious = page != null && page.hasPrevious();
        return new PaginatedResult<>(fileMetaData, Integer.parseInt(pageNum), totalElements, totalPages, hasNext, hasPrevious);
    }

    @Override
    public void delete(TenantId tenantId, EntityId entityId, String fileName, String extension) {
        fileMetaDataRepository.deleteById(new FileMetaDataCompositeKey(
                UUIDConverter.fromTimeUUID(tenantId.getId()),
                UUIDConverter.fromTimeUUID(entityId.getId()),
                fileName, extension));
    }

}
