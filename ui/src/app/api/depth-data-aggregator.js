/*
 * Copyright © 2016-2017 Hashmap, Inc
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

export default class DepthDataAggregator {

    constructor(onDataCb, dsKeyNames, startDs, limit, aggregationType, depthWindow, interval,
                stateData, types, $timeout, $filter) {
        this.onDataCb = onDataCb;
        this.dsKeyNames = dsKeyNames;
        this.dataBuffer = {};
        for (var k = 0; k < dsKeyNames.length; k++) {
            this.dataBuffer[dsKeyNames[k]] = [];
        }
        this.startDs = startDs;
        this.aggregationType = aggregationType;
        this.types = types;
        this.$timeout = $timeout;
        this.$filter = $filter;
        this.dataReceived = false;
        this.resetPending = false;
        this.noAggregation = aggregationType === types.aggregation.none.value;
        this.limit = limit;
        this.depthWindow = depthWindow;
        this.interval = interval;
        this.stateData = stateData;
        if (this.stateData) {
            this.lastPrevKvPairData = {};
        }

        // as of now only none is supported for depthseries data so all will point to none
        this.aggregationTimeout = Math.max(this.interval, 1000);
        switch (aggregationType) {
            case types.aggregation.min.value:
                this.aggFunction = min;
                break;
            case types.aggregation.max.value:
                this.aggFunction = max;
                break;
            case types.aggregation.avg.value:
                this.aggFunction = avg;
                break;
            case types.aggregation.sum.value:
                this.aggFunction = sum;
                break;
            case types.aggregation.count.value:
                this.aggFunction = count;
                break;
            case types.aggregation.none.value:
                this.aggFunction = none;
                break;
            default:
                this.aggFunction = none;
        }
    }

    reset(startDs, depthWindow, interval) {
        if (this.intervalTimeoutHandle) {
            this.$timeout.cancel(this.intervalTimeoutHandle);
            this.intervalTimeoutHandle = null;
        }
        this.intervalScheduledTime = currentTime();
        this.startDs = startDs;
        this.depthWindow = depthWindow;
        this.interval = interval;
        this.endDs = this.startDs + this.depthWindow;
        this.elapsed = 0;
        this.aggregationTimeout = Math.max(this.interval, 1000);
        this.resetPending = true;
        var self = this;
        this.intervalTimeoutHandle = this.$timeout(function() {
            self.onInterval();
        }, this.aggregationTimeout, false);
    }

    onData(data, update, history, apply) {
        if (!this.dataReceived || this.resetPending) {
            var updateIntervalScheduledTime = true;
            if (!this.dataReceived) {
                this.elapsed = 0;
                this.dataReceived = true;
                this.endDs = this.startDs + this.depthWindow;
            }
            if (this.resetPending) {
                this.resetPending = false;
                updateIntervalScheduledTime = false;
            }
            if (update) {
                this.aggregationMap = {};
                updateAggregatedData(this.aggregationMap, this.aggregationType === this.types.aggregation.count.value,
                    this.noAggregation, this.aggFunction, data.data, this.interval, this.startDs);
            } else {
                this.aggregationMap = processAggregatedData(data.data, this.aggregationType === this.types.aggregation.count.value, this.noAggregation);
            }
            if (updateIntervalScheduledTime) {
                this.intervalScheduledTime = currentTime();
            }
            this.onInterval(history, apply);
        } else {
            updateAggregationMap(this.aggregationMap, this.data, this.startDs, this.depthWindow);
            updateAggregatedData(this.aggregationMap, this.aggregationType === this.types.aggregation.count.value,
                this.noAggregation, this.aggFunction, data.data, this.interval, this.startDs);
            if (history) {
                this.intervalScheduledTime = currentTime();
                this.onInterval(history, apply);
            }
        }
    }

    onInterval(history, apply) {
        if (this.intervalTimeoutHandle) {
            this.$timeout.cancel(this.intervalTimeoutHandle);
            this.intervalTimeoutHandle = null;
        }
        this.data = this.updateData();
        if (this.onDataCb) {
            this.onDataCb(this.data, apply);
        }

        var self = this;
        if (!history) {
            this.intervalTimeoutHandle = this.$timeout(function() {
                self.onInterval();
            }, this.aggregationTimeout, false);
        }
    }

    updateData() {
        for (var k = 0; k < this.dsKeyNames.length; k++) {
            this.dataBuffer[this.dsKeyNames[k]] = [];
        }
        for (var key in this.aggregationMap) {
            var aggKeyData = this.aggregationMap[key];
            var keyData = this.dataBuffer[key];
            var aggDepthdatum;
            for (aggDepthdatum in aggKeyData) {
                var aggData = aggKeyData[aggDepthdatum];
                var kvPair = [Number(aggDepthdatum), aggData.aggValue];
                keyData.push(kvPair);
            }
            this.startDs = Number(aggDepthdatum);
            keyData = this.$filter('orderBy')(keyData, '+this[0]');
            if (this.stateData) {
                this.updateStateBounds(keyData, angular.copy(this.lastPrevKvPairData[key]));
            }
            if (keyData.length > this.limit) {
                keyData = keyData.slice(keyData.length - this.limit);
            }
            this.dataBuffer[key] = keyData;
        }
        return this.dataBuffer;
    }

    updateStateBounds(keyData, lastPrevKvPair) {
        if (lastPrevKvPair) {
            lastPrevKvPair[0] = this.startDs;
        }
        var firstKvPair;
        if (!keyData.length) {
            if (lastPrevKvPair) {
                firstKvPair = lastPrevKvPair;
                keyData.push(firstKvPair);
            }
        } else {
            firstKvPair = keyData[0];
        }
        if (firstKvPair && firstKvPair[0] > this.startDs) {
            if (lastPrevKvPair) {
                keyData.unshift(lastPrevKvPair);
            }
        }
        if (keyData.length) {
            var lastKvPair = keyData[keyData.length-1];
            if (lastKvPair[0] < this.endDs) {
                lastKvPair = angular.copy(lastKvPair);
                lastKvPair[0] = this.endDs;
                keyData.push(lastKvPair);
            }
        }
    }

    destroy() {
        if (this.intervalTimeoutHandle) {
            this.$timeout.cancel(this.intervalTimeoutHandle);
            this.intervalTimeoutHandle = null;
        }
        this.aggregationMap = null;
    }

}

/* eslint-disable */
function currentTime() {
    return window.performance && window.performance.now ?
        window.performance.now() : Date.now();
}
/* eslint-enable */

function processAggregatedData(data, isCount, noAggregation) {
    var aggregationMap = {};
    for (var key in data) {
        var aggKeyData = aggregationMap[key];
        if (!aggKeyData) {
            aggKeyData = {};
            aggregationMap[key] = aggKeyData;
        }
        var keyData = data[key];
        for (var i = 0; i < keyData.length; i++) {
            var kvPair = keyData[i];
            var depthdatum = kvPair[0];
            var value = convertValue(kvPair[1], noAggregation);
            var aggKey = depthdatum;
            var aggData = {
                count: isCount ? value : 1,
                sum: value,
                aggValue: value
            }
            aggKeyData[aggKey] = aggData;
        }
    }
    return aggregationMap;
}

function updateAggregatedData(aggregationMap, isCount, noAggregation, aggFunction, data, interval, startDs) {
    for (var key in data) {
        var aggKeyData = aggregationMap[key];
        if (!aggKeyData) {
            aggKeyData = {};
            aggregationMap[key] = aggKeyData;
        }
        var keyData = data[key];
        for (var i = 0; i < keyData.length; i++) {
            var kvPair = keyData[i];
            var depthdatum = kvPair[0];
            var value = convertValue(kvPair[1], noAggregation);
            var aggDepthdatum = noAggregation ? depthdatum : (startDs + Math.floor((depthdatum - startDs) / interval) * interval + interval/2);
            var aggData = aggKeyData[aggDepthdatum];
            if (!aggData) {
                aggData = {
                    count: 1,
                    sum: value,
                    aggValue: isCount ? 1 : value
                }
                aggKeyData[aggDepthdatum] = aggData;
            } else {
                aggFunction(aggData, value);
            }
        }
    }
}

function updateAggregationMap(aggregationMap, data, startDs, depthWindow) {
    for (var key in data) {
        var aggKeyData = aggregationMap[key];
        // added to delete stale data points.
        for (var aggrDepth in aggKeyData) {
            if (aggrDepth <= (startDs - depthWindow))
                delete aggKeyData[aggrDepth];
        }
    }
}

function convertValue(value, noAggregation) {
    if (!noAggregation || value && isNumeric(value)) {
        return Number(value);
    } else {
        return value;
    }
}

function isNumeric(value) {
    return (value - parseFloat( value ) + 1) >= 0;
}

function avg(aggData, value) {
    aggData.count++;
    aggData.sum += value;
    aggData.aggValue = aggData.sum / aggData.count;
}

function min(aggData, value) {
    aggData.aggValue = Math.min(aggData.aggValue, value);
}

function max(aggData, value) {
    aggData.aggValue = Math.max(aggData.aggValue, value);
}

function sum(aggData, value) {
    aggData.aggValue = aggData.aggValue + value;
}

function count(aggData) {
    aggData.count++;
    aggData.aggValue = aggData.count;
}

function none(aggData, value) {
    aggData.aggValue = value;
}
