/**
 * Copyright © 2016-2018 Hashmap, Inc
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
package org.thingsboard.server.dao.model.sql;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.thingsboard.server.common.data.UUIDConverter;
import org.thingsboard.server.common.data.id.EntityIdFactory;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.dao.model.ToData;
import org.thingsboard.server.dao.util.mapping.JsonStringType;

import javax.persistence.*;

import static org.thingsboard.server.dao.model.ModelConstants.*;

@Data
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = RELATION_COLUMN_FAMILY_NAME)
@IdClass(RelationCompositeKey.class)
public final class RelationEntity implements ToData<EntityRelation> {

    @Id
    @Column(name = RELATION_FROM_ID_PROPERTY)
    private String fromId;

    @Id
    @Column(name = RELATION_FROM_TYPE_PROPERTY)
    private String fromType;

    @Id
    @Column(name = RELATION_TO_ID_PROPERTY)
    private String toId;

    @Id
    @Column(name = RELATION_TO_TYPE_PROPERTY)
    private String toType;

    @Id
    @Column(name = RELATION_TYPE_GROUP_PROPERTY)
    private String relationTypeGroup;

    @Id
    @Column(name = RELATION_TYPE_PROPERTY)
    private String relationType;

    @Type(type = "json")
    @Column(name = ADDITIONAL_INFO_PROPERTY)
    private JsonNode additionalInfo;

    public RelationEntity() {
        super();
    }

    public RelationEntity(EntityRelation relation) {
        if (relation.getTo() != null) {
            this.toId = UUIDConverter.fromTimeUUID(relation.getTo().getId());
            this.toType = relation.getTo().getEntityType().name();
        }
        if (relation.getFrom() != null) {
            this.fromId = UUIDConverter.fromTimeUUID(relation.getFrom().getId());
            this.fromType = relation.getFrom().getEntityType().name();
        }
        this.relationType = relation.getType();
        this.relationTypeGroup = relation.getTypeGroup().name();
        this.additionalInfo = relation.getAdditionalInfo();
    }

    @Override
    public EntityRelation toData() {
        EntityRelation relation = new EntityRelation();
        if (toId != null && toType != null) {
            relation.setTo(EntityIdFactory.getByTypeAndUuid(toType, UUIDConverter.fromString(toId)));
        }
        if (fromId != null && fromType != null) {
            relation.setFrom(EntityIdFactory.getByTypeAndUuid(fromType, UUIDConverter.fromString(fromId)));
        }
        relation.setType(relationType);
        relation.setTypeGroup(RelationTypeGroup.valueOf(relationTypeGroup));
        relation.setAdditionalInfo(additionalInfo);
        return relation;
    }

}