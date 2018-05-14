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

import com.hashmapinc.server.common.data.TagMetaDataQuality;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.dao.model.ToData;
import lombok.Data;

import javax.persistence.*;

import static com.hashmapinc.server.dao.model.ModelConstants.*;

@Data
@Entity
@Table(name = "tag_metadata_quality")
@IdClass(TagMetaDataQualityCompositeKey.class)
public class TagMetaDataQualityEntity implements ToData<TagMetaDataQuality>{

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

    @Column(name = TAG_METADATA_QUALITY_UNIT)
    private String unit;

    @Column(name = TAG_METADATA_QUALITY_AVG_FREQUENCY)
    private double avgFrequency;

    @Column(name = TAG_METADATA_QUALITY_MAX_FREQUENCY)
    private double maxFrequency;

    @Column(name = TAG_METADATA_QUALITY_MIN_FREQUENCY)
    private double minFrequency;

    @Column(name = TAG_METADATA_QUALITY_MEAN_FREQUENCY)
    private double meanFrequency;

    @Column(name = TAG_METADATA_QUALITY_MEDIAN_FREQUENCY)
    private double medianFrequency;

    @Column(name = TAG_METADATA_QUALITY_SOURCE)
    private String source;

    public TagMetaDataQualityEntity(){

    }

    public TagMetaDataQualityEntity(TagMetaDataQuality tagMetaDataQuality){
        if(tagMetaDataQuality.getEntityId() != null && tagMetaDataQuality.getEntityType() != null
                && tagMetaDataQuality.getKey() != null){
            entityId = tagMetaDataQuality.getEntityId();
            entityType = tagMetaDataQuality.getEntityType();
            key = tagMetaDataQuality.getKey();
        }

        unit = tagMetaDataQuality.getUnit();
        avgFrequency = tagMetaDataQuality.getAvgFrequency();
        minFrequency = tagMetaDataQuality.getMinFrequency();
        maxFrequency = tagMetaDataQuality.getMaxFrequency();
        meanFrequency = tagMetaDataQuality.getMeanFrequency();
        medianFrequency = tagMetaDataQuality.getMedianFrequency();
        source = tagMetaDataQuality.getSource();
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
    public TagMetaDataQuality toData(){

        TagMetaDataQuality tagMetaDataQuality = new TagMetaDataQuality();
        if(this.entityId != null && this.entityType != null && this.key != null){
            tagMetaDataQuality.setEntityId(this.entityId);
            tagMetaDataQuality.setEntityType(this.entityType);
            tagMetaDataQuality.setKey(this.key);
        }
        else
            return null;
        tagMetaDataQuality.setAvgFrequency(this.avgFrequency);
        tagMetaDataQuality.setMaxFrequency(this.maxFrequency);
        tagMetaDataQuality.setMinFrequency(this.minFrequency);
        tagMetaDataQuality.setMeanFrequency(this.meanFrequency);
        tagMetaDataQuality.setMedianFrequency(this.medianFrequency);
        tagMetaDataQuality.setSource(this.source);

        return tagMetaDataQuality;
    }
}
