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
package com.hashmapinc.models.triggers;

import com.google.gson.annotations.SerializedName;
import com.hashmapinc.models.Spec;

import java.util.Objects;

public class V1beta1CronJobTriggerSpec implements Spec{

    @SerializedName("schedule")
    private String schedule;

    @SerializedName("function-name")
    private String functionName;

    public V1beta1CronJobTriggerSpec schedule(String schedule){
        this.schedule = schedule;
        return this;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public V1beta1CronJobTriggerSpec functionName(String functionName){
        this.functionName = functionName;
        return this;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1beta1CronJobTriggerSpec)) return false;
        V1beta1CronJobTriggerSpec that = (V1beta1CronJobTriggerSpec) o;
        return Objects.equals(getSchedule(), that.getSchedule()) &&
                Objects.equals(getFunctionName(), that.getFunctionName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSchedule(), getFunctionName());
    }

    @Override
    public String toString() {
        return "V1beta1CronJobTriggerSpec{" +
                "schedule='" + schedule + '\'' +
                ", functionName='" + functionName + '\'' +
                '}';
    }
}
