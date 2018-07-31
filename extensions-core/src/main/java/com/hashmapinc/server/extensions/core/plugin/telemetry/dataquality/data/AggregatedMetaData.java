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
package com.hashmapinc.server.extensions.core.plugin.telemetry.dataquality.data;

public class AggregatedMetaData {
    private double avgFrequency;
    private double maxFrequency;
    private double minFrequency;
    private double meanFrequency;
    private double medianFrequency;

    public AggregatedMetaData(double avgFrequency, double maxFrequency, double minFrequency, double meanFrequency, double medianFrequency) {
        this.avgFrequency = avgFrequency;
        this.maxFrequency = maxFrequency;
        this.minFrequency = minFrequency;
        this.meanFrequency = meanFrequency;
        this.medianFrequency = medianFrequency;
    }

    public double getAvgFrequency() {
        return avgFrequency;
    }

    public double getMaxFrequency() {
        return maxFrequency;
    }

    public double getMinFrequency() {
        return minFrequency;
    }

    public double getMeanFrequency() {
        return meanFrequency;
    }

    public double getMedianFrequency() {
        return medianFrequency;
    }
}
