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

import com.fasterxml.jackson.databind.JsonNode;
import com.hashmapinc.server.common.data.id.DashboardId;

public class Dashboard extends DashboardInfo {

    private static final long serialVersionUID = 872682138346187503L;
    
    private transient JsonNode configuration;

    private DashboardType type;
    
    public Dashboard() {
        super();
    }

    public Dashboard(DashboardId id) {
        super(id);
    }

    public Dashboard(DashboardInfo dashboardInfo) {
        super(dashboardInfo);
    }

    public DashboardType getType() {
        return type;
    }

    public void setType(DashboardType type) {
        this.type = type;
    }

    public Dashboard(Dashboard dashboard) {
        super(dashboard);
        this.configuration = dashboard.getConfiguration();
    }

    public JsonNode getConfiguration() {
        return configuration;
    }

    public void setConfiguration(JsonNode configuration) {
        this.configuration = configuration;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Dashboard other = (Dashboard) obj;
        if (configuration == null) {
            if (other.configuration != null)
                return false;
        } else if(type == null){
            if (other.type != null)
                return false;
        }
        else if (!configuration.equals(other.configuration))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Dashboard [tenantId=");
        builder.append(getTenantId());
        builder.append(", title=");
        builder.append(getTitle());
        builder.append(", type=");
        builder.append(getType());
        builder.append(", configuration=");
        builder.append(configuration);
        builder.append("]");
        return builder.toString();
    }
}
