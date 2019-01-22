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
package com.hashmapinc.server.dao.sql.template;

import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.page.PaginatedResult;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.template.TemplateMetadata;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.sql.TemplateMetadataEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTextDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

@Component
public class JpaBaseTemplateDao extends JpaAbstractSearchTextDao<TemplateMetadataEntity, TemplateMetadata> {

    @Autowired
    private TemplateRepository templateRepository;

    @Override
    protected Class<TemplateMetadataEntity> getEntityClass() {
        return TemplateMetadataEntity.class;
    }

    @Override
    protected CrudRepository<TemplateMetadataEntity, String> getCrudRepository() {
        return templateRepository;
    }

    @Override
    public TemplateMetadata save(TemplateMetadata templateMetadata) {
        validateForDuplicateName(templateMetadata);
        return super.save(templateMetadata);
    }

    public List<TemplateMetadata> findByPageLink(TextPageLink pageLink) {
        return DaoUtil.convertDataList(
                templateRepository.findTemplate(Objects.toString(pageLink.getTextSearch(), ""),
                                pageLink.getIdOffset() == null ? ModelConstants.NULL_UUID_STR : UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                                PageRequest.of(0, pageLink.getLimit())));

    }

    public PaginatedResult<TemplateMetadata> findByPageNumber(int limit, int pageNum, String searchText) {
        PageRequest pageable = PageRequest.of(pageNum, limit, Sort.by(new Sort.Order(Sort.Direction.ASC, ModelConstants.ID_PROPERTY)));
        Page<TemplateMetadataEntity> resultPage = templateRepository.findAll(searchText, pageable);
        if(resultPage == null) {
            return new PaginatedResult<>(Collections.emptyList(), pageNum, 0, 0, false, false);
        }

        List<TemplateMetadata> templates = DaoUtil.convertDataList(resultPage.getContent());
        return new PaginatedResult<>(templates, pageNum, resultPage.getTotalElements(),
                resultPage.getTotalPages(), resultPage.hasNext(), resultPage.hasPrevious());
    }

    private void validateForDuplicateName(TemplateMetadata templateMetadata) {
        boolean duplicateNamePresent = StreamSupport.stream(templateRepository.findAll().spliterator(), false)
                .map(TemplateMetadataEntity::getName)
                .anyMatch(t -> Objects.equals(t, templateMetadata.getName()));
        if(duplicateNamePresent) {
            throw new DataValidationException("Name already present! Provide a different name");
        }
    }
}
