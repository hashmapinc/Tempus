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

import com.hashmapinc.server.common.data.TagMetaData;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.dao.model.ToData;
import lombok.Data;

import javax.persistence.*;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Data
@Entity
@Table(name = "tag_metadata")
@IdClass(TagMetaDataCompositeKey.class)
public class TagMetaDataEntity implements ToData<TagMetaData>{

    @Id
    @Column(name = ENTITY_ID_COLUMN)
    private String entityId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = ENTITY_TYPE_COLUMN)
    private EntityType entityType;

    @Id
    @Column(name = KEY_COLUMN)
    private String key;

    @Column(name = TAG_METADATA_UNIT)
    private String unit;

    @Column(name = TAG_METADATA_AVG_FREQUENCY)
    private double avgFrequency;

    @Column(name = TAG_METADATA_MAX_FREQUENCY)
    private double maxFrequency;

    @Column(name = TAG_METADATA_MIN_FREQUENCY)
    private double minFrequency;

    @Column(name = TAG_METADATA_MEAN_FREQUENCY)
    private double meanFrequency;

    @Column(name = TAG_METADATA_MEDIAN_FREQUENCY)
    private double medianFrequency;

    @Column(name = TAG_METADATA_SOURCE)
    private String source;

    public TagMetaDataEntity(){

    }

    public TagMetaDataEntity(TagMetaData tagMetaData){
        if(tagMetaData.getEntityId() != null && tagMetaData.getEntityType() != null
                && tagMetaData.getKey() != null){
            entityId = tagMetaData.getEntityId();
            entityType = tagMetaData.getEntityType();
            key = tagMetaData.getKey();
        }

        unit = tagMetaData.getUnit();
        avgFrequency = tagMetaData.getAvgFrequency();
        minFrequency = tagMetaData.getMinFrequency();
        maxFrequency = tagMetaData.getMaxFrequency();
        meanFrequency = tagMetaData.getMeanFrequency();
        medianFrequency = tagMetaData.getMedianFrequency();
        source = tagMetaData.getSource();
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getAvgFrequency() {
        return avgFrequency;
    }

    public void setAvgFrequency(double avgFrequency) {
        this.avgFrequency = avgFrequency;
    }

    public double getMaxFrequency() {
        return maxFrequency;
    }

    public void setMaxFrequency(double maxFrequency) {
        this.maxFrequency = maxFrequency;
    }

    public double getMinFrequency() {
        return minFrequency;
    }

    public void setMinFrequency(double minFrequency) {
        this.minFrequency = minFrequency;
    }

    public double getMeanFrequency() {
        return meanFrequency;
    }

    public void setMeanFrequency(double meanFrequency) {
        this.meanFrequency = meanFrequency;
    }

    public double getMedianFrequency() {
        return medianFrequency;
    }

    public void setMedianFrequency(double medianFrequency) {
        this.medianFrequency = medianFrequency;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public TagMetaData toData(){

        TagMetaData tagMetaData = new TagMetaData();
        if(this.entityId != null && this.entityType != null && this.key != null){
            tagMetaData.setEntityId(this.entityId);
            tagMetaData.setEntityType(this.entityType);
            tagMetaData.setKey(this.key);
        }
        else
            return null;
        tagMetaData.setAvgFrequency(this.avgFrequency);
        tagMetaData.setMaxFrequency(this.maxFrequency);
        tagMetaData.setMinFrequency(this.minFrequency);
        tagMetaData.setMeanFrequency(this.meanFrequency);
        tagMetaData.setMedianFrequency(this.medianFrequency);
        tagMetaData.setSource(this.source);

        return tagMetaData;
    }
}
