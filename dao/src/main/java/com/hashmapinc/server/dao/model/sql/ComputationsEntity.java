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
package com.hashmapinc.server.dao.model.sql;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.model.BaseSqlEntity;
import com.hashmapinc.server.dao.util.mapping.JsonStringType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import static com.hashmapinc.server.common.data.UUIDConverter.fromString;
import static com.hashmapinc.server.common.data.UUIDConverter.fromTimeUUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.COMPUTATIONS_TABLE_NAME)
public class ComputationsEntity extends BaseSqlEntity<Computations> implements SearchTextEntity<Computations> {
    @Transient
    private static final long serialVersionUID = -4873737406462009031L;

    @Column(name = ModelConstants.COMPUTATIONS_NAME)
    private String name;

    @Column(name = ModelConstants.COMPUTATIONS_JAR_PATH)
    private String jarPath;

    @Column(name = ModelConstants.COMPUTATIONS_JAR)
    private String jarName;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = ModelConstants.COMPUTATIONS_MAIN_CLASS)
    private String mainClass;

    @Column(name = ModelConstants.COMPUTATIONS_ARGS_FORMAT)
    private String argsFormat;

    @Type(type = "json")
    @Column(name = ModelConstants.COMPUTATIONS_DESCRIPTOR)
    private JsonNode jsonDescriptor;

    @Column(name = ModelConstants.COMPUTATIONS_TENANT_ID)
    private String tenantId;

    @Column(name = ModelConstants.COMPUTATIONS_ARGS_TYPE)
    private String argsType;

    @Override
    public String getSearchTextSource() {
        return name;
    }

    public ComputationsEntity() {
        super();
    }

    public ComputationsEntity(Computations computations){
        if(computations.getId() != null) {
            this.setId(computations.getId().getId());
        }
        if(computations.getName() != null) {
            this.name = computations.getName();
        }
        if(computations.getJarPath() != null) {
            this.jarPath = computations.getJarPath();
        }
        if(computations.getJarPath() != null) {
            this.jarName = computations.getJarName();
        }
        if(computations.getArgsformat() != null) {
            this.argsFormat = computations.getArgsformat();
        }
        if(computations.getJsonDescriptor() != null) {
            this.jsonDescriptor = computations.getJsonDescriptor();
        }
        if(computations.getMainClass() != null) {
            this.mainClass = computations.getMainClass();
        }
        if(computations.getTenantId() != null) {
            this.tenantId = fromTimeUUID(computations.getTenantId().getId());
        }
        if(computations.getArgsType() != null) {
            this.argsType = computations.getArgsType();
        }
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ComputationsEntity that = (ComputationsEntity) o;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (searchText != null ? !searchText.equals(that.searchText) : that.searchText != null) return false;
        if (jarPath != null ? !jarPath.equals(that.jarPath) : that.jarPath != null) return false;
        if (jarName != null ? !jarName.equals(that.jarName) : that.jarName != null) return false;

        if (mainClass != null ? !mainClass.equals(that.mainClass) : that.mainClass != null) return false;
        if (jsonDescriptor != null ? !jsonDescriptor.equals(that.jsonDescriptor) : that.jsonDescriptor != null) return false;
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
        if (argsFormat != null ? !argsFormat.equals(that.argsFormat) : that.argsFormat != null) return false;
        if (argsType != null ? !argsType.equals(that.argsType) : that.argsType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (searchText != null ? searchText.hashCode() : 0);
        result = 31 * result + (jarPath != null ? jarPath.hashCode() : 0);
        result = 31 * result + (jarName != null ? jarName.hashCode() : 0);
        result = 31 * result + (mainClass != null ? mainClass.hashCode() : 0);
        result = 31 * result + (jsonDescriptor != null ? jsonDescriptor.hashCode() : 0);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (argsFormat != null ? argsFormat.hashCode() : 0);
        result = 31 * result + (argsType != null ? argsType.hashCode() : 0);
        return result;
    }

    @Override
    public Computations toData() {
        Computations computations = new Computations(new ComputationId(getId()));
        computations.setCreatedTime(UUIDs.unixTimestamp(getId()));
        computations.setName(name);
        computations.setJarPath(jarPath);
        computations.setJarName(jarName);
        computations.setArgsformat(argsFormat);
        computations.setArgsType(argsType);
        if (tenantId != null) {
            computations.setTenantId(new TenantId(fromString(tenantId)));
        }
        computations.setMainClass(mainClass);
        computations.setJsonDescriptor(jsonDescriptor);
        return computations;
    }

}
