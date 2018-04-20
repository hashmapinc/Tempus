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
package org.thingsboard.server.extensions.core.plugin.telemetry.cmd;

import java.util.List;

public class TelemetryPluginCmdsWrapper {

    private List<AttributesSubscriptionCmd> attrSubCmds;

    private List<TimeseriesSubscriptionCmd> tsSubCmds;

    private List<DepthSeriesSubscriptionCmd> dsSubCmds;

    private List<GetHistoryCmd> historyCmds;

    private List <GetDepthHistoryCmd> depthHistoryCmds;

    public TelemetryPluginCmdsWrapper() {
        super();
    }

    public List<GetDepthHistoryCmd> getDepthHistoryCmds() {
        return depthHistoryCmds;
    }

    public void setDepthHistoryCmds(List<GetDepthHistoryCmd> depthHistoryCmds) {
        this.depthHistoryCmds = depthHistoryCmds;
    }

    public List<AttributesSubscriptionCmd> getAttrSubCmds() {
        return attrSubCmds;
    }

    public void setAttrSubCmds(List<AttributesSubscriptionCmd> attrSubCmds) {
        this.attrSubCmds = attrSubCmds;
    }

    public List<TimeseriesSubscriptionCmd> getTsSubCmds() {
        return tsSubCmds;
    }

    public void setTsSubCmds(List<TimeseriesSubscriptionCmd> tsSubCmds) {
        this.tsSubCmds = tsSubCmds;
    }

    public List<GetHistoryCmd> getHistoryCmds() {
        return historyCmds;
    }

    public void setHistoryCmds(List<GetHistoryCmd> historyCmds) {
        this.historyCmds = historyCmds;
    }

    public List<DepthSeriesSubscriptionCmd> getDsSubCmds() { return dsSubCmds; }

    public void setDsSubCmds(List<DepthSeriesSubscriptionCmd> dsSubCmds) { this.dsSubCmds = dsSubCmds; }
}
