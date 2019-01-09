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

import com.hashmapinc.server.common.data.id.TemplateId;
import com.hashmapinc.server.common.data.template.TemplateMetadata;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = ModelConstants.TEMPLATE_COLUMN_FAMILY_NAME)
public class TemplateMetadataEntity extends BaseSqlEntity<TemplateMetadata> implements SearchTextEntity<TemplateMetadata> {

    @Column(name = ModelConstants.TEMPLATE_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.TEMPLATE_BODY_PROPERTY)
    private String body;

    public TemplateMetadataEntity(TemplateMetadata templateMetadata) {
        if (templateMetadata.getId() != null) {
            this.setId(templateMetadata.getId().getId());
        }
        this.name = templateMetadata.getName();
        this.body = templateMetadata.getBody();
    }

    @Override
    public String getSearchTextSource() {
        return name;
    }

    @Override
    public void setSearchText(String searchText) {
        this.name = searchText;
    }

    @Override
    public TemplateMetadata toData() {
        TemplateMetadata templateMetadata = new TemplateMetadata(new TemplateId(getId()));
        templateMetadata.setName(name);
        templateMetadata.setBody(body);
        return templateMetadata;
    }
}
