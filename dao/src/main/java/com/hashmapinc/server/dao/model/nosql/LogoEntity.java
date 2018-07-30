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

package com.hashmapinc.server.dao.model.nosql;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.hashmapinc.server.common.data.Logo;
import com.hashmapinc.server.common.data.id.LogoId;
import com.hashmapinc.server.dao.model.BaseEntity;
import com.hashmapinc.server.dao.model.ModelConstants;

import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Column;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;
import java.nio.ByteBuffer;

import static com.hashmapinc.server.dao.model.ModelConstants.ID_PROPERTY;

@Table(name = ModelConstants.LOGO_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString

public class LogoEntity implements BaseEntity<Logo>{

    @PartitionKey
    @Column(name = ID_PROPERTY)
    private UUID id;


    @Column(name = ModelConstants.LOGO_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.LOGO_DISPLAY_PROPERTY)
    private boolean display;


    @Column(name = ModelConstants.LOGO_FILE_PROPERTY)
    private ByteBuffer file;


    public LogoEntity() {
        super();
    }

    public LogoEntity(Logo logo) {
        if (logo.getId() != null) {
            this.setId(logo.getId().getId());
        }

        this.name = logo.getName();
        this.display = logo.isDisplay();
        if (logo.getFile() != null) {
            this.file = ByteBuffer.wrap(logo.getFile());
        }

    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public ByteBuffer getFile() {
        return file;
    }

    public void setFile(ByteBuffer file) {
        this.file = file;
    }

    @Override
    public Logo toData() {
        Logo logo = new Logo(new LogoId(getId()));
        logo.setName(name);
        logo.setDisplay(display);
        if (file != null) {
            byte[] fileByteArray = new byte[file.remaining()];
            file.get(fileByteArray);
            logo.setFile(fileByteArray);
        }
        return logo;
    }


}
