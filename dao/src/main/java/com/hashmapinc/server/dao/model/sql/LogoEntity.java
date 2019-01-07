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

import com.hashmapinc.server.common.data.Logo;
import com.hashmapinc.server.common.data.id.LogoId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.LOGO_TABLE_NAME)

public class LogoEntity extends BaseSqlEntity<Logo>implements BaseEntity<Logo> {

    @Column(name = ModelConstants.LOGO_NAME)
    private String name;

    @Column(name = ModelConstants.LOGO_DISPLAY)
    private boolean display;


    @Column(name = ModelConstants.LOGO_FILE)
    private byte[] file;


    public LogoEntity() {
        super();
    }

    public LogoEntity(Logo logo) {
        if (logo.getId() != null) {
            this.setId(logo.getId().getId());
        }

        this.name = logo.getName();
        this.display = logo.isDisplay();
        this.file = logo.getFile();
    }

    @Override
    public Logo toData() {
        Logo logo = new Logo(new LogoId(getId()));
        logo.setName(name);
        logo.setDisplay(display);
        logo.setFile(file);
        return logo;
    }

}
