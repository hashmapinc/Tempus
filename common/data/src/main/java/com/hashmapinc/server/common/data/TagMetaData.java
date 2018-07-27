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
package com.hashmapinc.server.common.data;

public class TagMetaData {

    private String entityId;
    private EntityType entityType;
    private String key;
    private String unit;
    private double avgFrequency;
    private double maxFrequency;
    private double minFrequency;
    private double meanFrequency;
    private double medianFrequency;
    private String source;

    public TagMetaData(){
        this.avgFrequency = 0.0;
        this.maxFrequency = 0.0;
        this.meanFrequency = 0.0;
        this.minFrequency = 0.0;
        this.medianFrequency = 0.0;
        this.unit = "no unit";
        this.source = "no source";
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
