/*
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
import thingsboardApiDevice from './device.service';
import thingsboardApiTelemetryWebsocket from './telemetry-websocket.service';
import thingsboardTypes from '../common/types.constant';
import thingsboardUtils from '../common/utils.service';
import DataAggregator from './data-aggregator';
import DepthDataAggregator from './depth-data-aggregator';

export default angular.module('thingsboard.api.datasource', [thingsboardApiDevice, thingsboardApiTelemetryWebsocket, thingsboardTypes, thingsboardUtils])
    .factory('datasourceService', DatasourceService)
    .name;

const YEAR = 1000 * 60 * 60 * 24 * 365;

/*@ngInject*/
function DatasourceService($timeout, $filter, telemetryWebsocketService, types, utils) {

    var subscriptions = {};

    var service = {
        subscribeToDatasource: subscribeToDatasource,
        unsubscribeFromDatasource: unsubscribeFromDatasource
    }

    return service;

    function subscribeToDatasource(listener) {
        var datasource = listener.datasource;

        if (datasource.type === types.datasourceType.entity && (!listener.entityId || !listener.entityType)) {
            return;
        }

        var subscriptionDataKeys = [];
        for (var d = 0; d < datasource.dataKeys.length; d++) {
            var dataKey = datasource.dataKeys[d];
            var subscriptionDataKey = {
                name: dataKey.name,
                type: dataKey.type,
                funcBody: dataKey.funcBody,
                postFuncBody: dataKey.postFuncBody
            }
            subscriptionDataKeys.push(subscriptionDataKey);
        }

        var datasourceSubscription = {
            datasourceType: datasource.type,
            dataKeys: subscriptionDataKeys,
            type: listener.subscriptionType
        };

        if (listener.subscriptionType === types.widgetType.timeseries.value) {
            datasourceSubscription.subscriptionTimewindow = angular.copy(listener.subscriptionTimewindow);
        }

        if (listener.subscriptionType === types.widgetType.depthseries.value) {
            datasourceSubscription.subscriptionDepthwindow = angular.copy(listener.subscriptionDepthwindow);
        }

        if (datasourceSubscription.datasourceType === types.datasourceType.entity) {
            datasourceSubscription.entityType = listener.entityType;
            datasourceSubscription.entityId = listener.entityId;
        }

        listener.datasourceSubscriptionKey = utils.objectHashCode(datasourceSubscription);
        var subscription;
        if (subscriptions[listener.datasourceSubscriptionKey]) {
            subscription = subscriptions[listener.datasourceSubscriptionKey];
            subscription.syncListener(listener);
        } else {
            subscription = new DatasourceSubscription(datasourceSubscription, telemetryWebsocketService, $timeout, $filter, types, utils);
            subscriptions[listener.datasourceSubscriptionKey] = subscription;
            subscription.start();
        }
        subscription.addListener(listener);
    }

    function unsubscribeFromDatasource(listener) {
        if (listener.datasourceSubscriptionKey) {
            if (subscriptions[listener.datasourceSubscriptionKey]) {
                var subscription = subscriptions[listener.datasourceSubscriptionKey];
                subscription.removeListener(listener);
                if (!subscription.hasListeners()) {
                    subscription.unsubscribe();
                    delete subscriptions[listener.datasourceSubscriptionKey];
                }
            }
            listener.datasourceSubscriptionKey = null;
        }
    }

}

function DatasourceSubscription(datasourceSubscription, telemetryWebsocketService, $timeout, $filter, types, utils) {

    var listeners = [];
    var datasourceType = datasourceSubscription.datasourceType;
    var datasourceData = {};
    var dataKeys = {};
    var subscribers = [];
    var history = datasourceSubscription.subscriptionTimewindow &&
        datasourceSubscription.subscriptionTimewindow.fixedWindow;
    var realtime = datasourceSubscription.subscriptionTimewindow &&
        datasourceSubscription.subscriptionTimewindow.realtimeWindowMs;
    var historyDepth = datasourceSubscription.subscriptionDepthwindow &&
        datasourceSubscription.subscriptionDepthwindow.fixedWindow;
    //var realtimeDepth = datasourceSubscription.subscriptionDepthwindow &&
    //    datasourceSubscription.subscriptionTimewindow.realtimeWindowMs;
    var timer;
    var frequency;
    var tickElapsed = 0;
    var tickScheduledTime = 0;
    var dataAggregator;

    var subscription = {
        addListener: addListener,
        hasListeners: hasListeners,
        removeListener: removeListener,
        syncListener: syncListener,
        start: start,
        unsubscribe: unsubscribe
    }

    initializeSubscription();

    return subscription;

    function initializeSubscription() {
        for (var i = 0; i < datasourceSubscription.dataKeys.length; i++) {
            var dataKey = angular.copy(datasourceSubscription.dataKeys[i]);
            dataKey.index = i;
            var key;
            if (datasourceType === types.datasourceType.function) {
                if (!dataKey.func) {
                    dataKey.func = new Function("time", "prevValue", dataKey.funcBody);
                }
            } else {
                if (dataKey.postFuncBody && !dataKey.postFunc) {
                    dataKey.postFunc = new Function("time", "value", "prevValue", dataKey.postFuncBody);
                }
            }
            if (datasourceType === types.datasourceType.entity || datasourceSubscription.type === types.widgetType.timeseries.value
                || datasourceSubscription.type === types.widgetType.depthseries.value ) {
                if (datasourceType === types.datasourceType.function) {
                    key = dataKey.name + '_' + dataKey.index + '_' + dataKey.type;
                } else {
                    key = dataKey.name + '_' + dataKey.type;
                }
                var dataKeysList = dataKeys[key];
                if (!dataKeysList) {
                    dataKeysList = [];
                    dataKeys[key] = dataKeysList;
                }
                var index = dataKeysList.push(dataKey) - 1;
                datasourceData[key + '_' + index] = {
                    data: []
                };
            } else {
                key = utils.objectHashCode(dataKey);
                datasourceData[key] = {
                    data: []
                };
                dataKeys[key] = dataKey;
            }

            dataKey.key = key;
        }
        if (datasourceType === types.datasourceType.function) {
            frequency = 1000;
            if (datasourceSubscription.type === types.widgetType.timeseries.value) {
                frequency = Math.min(datasourceSubscription.subscriptionTimewindow.aggregation.interval, 5000);
            }
            if (datasourceSubscription.type === types.widgetType.depthseries.value) {
                frequency = Math.min(datasourceSubscription.subscriptionDepthwindow.aggregation.interval, 5000);
            }
        }
    }

    function addListener(listener) {
        listeners.push(listener);
        if (history || historyDepth) {
            start();
        }
    }

    function hasListeners() {
        return listeners.length > 0;
    }

    function removeListener(listener) {
        listeners.splice(listeners.indexOf(listener), 1);
    }

    function syncListener(listener) {
        var key;
        var dataKey;
        if (datasourceType === types.datasourceType.entity || datasourceSubscription.type === types.widgetType.timeseries.value ||
            datasourceSubscription.type === types.widgetType.depthseries.value ) {
            for (key in dataKeys) {
                var dataKeysList = dataKeys[key];
                for (var i = 0; i < dataKeysList.length; i++) {
                    dataKey = dataKeysList[i];
                    var datasourceKey = key + '_' + i;
                    listener.dataUpdated(datasourceData[datasourceKey],
                        listener.datasourceIndex,
                        dataKey.index, false);
                }
            }
        } else {
            for (key in dataKeys) {
                dataKey = dataKeys[key];
                listener.dataUpdated(datasourceData[key],
                    listener.datasourceIndex,
                    dataKey.index, false);
            }
        }
    }

    function start() {
        if (history && historyDepth && !hasListeners()) {
            return;
        }
        var subsTw = datasourceSubscription.subscriptionTimewindow;
        var subsDw = datasourceSubscription.subscriptionDepthwindow;
        var tsKeyNames = [];
        //##### ADDING DEPTH KEY NAMES
        var dsKeyNames = [];
        var dataKey;

        if (datasourceType === types.datasourceType.entity) {

            //send subscribe command

            var tsKeys = '';
            var attrKeys = '';
            //##### ADDING DEPTH KEYS
            var dsKeys = '';

            for (var key in dataKeys) {
                var dataKeysList = dataKeys[key];
                dataKey = dataKeysList[0];
                if (dataKey.type === types.dataKeyType.timeseries) {
                    if (tsKeys.length > 0) {
                        tsKeys += ',';
                    }
                    tsKeys += dataKey.name;
                    tsKeyNames.push(dataKey.name);
                } else if (dataKey.type === types.dataKeyType.depthSeries) {
                    //##### ADDING DEPTH HANDLING
                    if (dsKeys.length > 0) {
                        dsKeys += ',';
                    }
                    dsKeys += dataKey.name;
                    dsKeyNames.push(dataKey.name);
                } else if (dataKey.type === types.dataKeyType.attribute) {
                    if (attrKeys.length > 0) {
                        attrKeys += ',';
                    }
                    attrKeys += dataKey.name;
                }
            }

            if (tsKeys.length > 0) {

                var subscriber;
                if (history) {

                    var historyCommand = {
                        entityType: datasourceSubscription.entityType,
                        entityId: datasourceSubscription.entityId,
                        keys: tsKeys,
                        startTs: subsTw.fixedWindow.startTimeMs,
                        endTs: subsTw.fixedWindow.endTimeMs,
                        interval: subsTw.aggregation.interval,
                        limit: subsTw.aggregation.limit,
                        agg: subsTw.aggregation.type
                    };

                    subscriber = {
                        historyCommands: [ historyCommand ],
                        type: types.dataKeyType.timeseries,
                        subsTw: subsTw
                    };

                    if (subsTw.aggregation.stateData) {
                        subscriber.firstStateHistoryCommand = createFirstStateHistoryCommand(subsTw.fixedWindow.startTimeMs, tsKeys);
                        subscriber.historyCommands.push(subscriber.firstStateHistoryCommand);
                    }

                    subscriber.onData = function (data, subscriptionId) {
                        if (this.subsTw.aggregation.stateData &&
                            this.firstStateHistoryCommand && this.firstStateHistoryCommand.cmdId == subscriptionId) {
                            if (this.data) {
                                onStateHistoryData(data, this.data, this.subsTw.aggregation.limit,
                                    subsTw.fixedWindow.startTimeMs, this.subsTw.fixedWindow.endTimeMs,
                                    (data) => {
                                        onData(data.data, types.dataKeyType.timeseries, true);
                                    });
                            } else {
                                this.firstStateData = data;
                            }
                        } else {
                            if (this.subsTw.aggregation.stateData) {
                                if (this.firstStateData) {
                                    onStateHistoryData(this.firstStateData, data, this.subsTw.aggregation.limit,
                                        this.subsTw.fixedWindow.startTimeMs, this.subsTw.fixedWindow.endTimeMs,
                                        (data) => {
                                            onData(data.data, types.dataKeyType.timeseries, true);
                                        });
                                } else {
                                    this.data = data;
                                }
                            } else {
                                for (key in data.data) {
                                    var keyData = data.data[key];
                                    data.data[key] = $filter('orderBy')(keyData, '+this[0]');
                                }
                                onData(data.data, types.dataKeyType.timeseries, true);
                            }
                        }
                    };
                    subscriber.onReconnected = function() {};
                    telemetryWebsocketService.subscribe(subscriber);
                    subscribers.push(subscriber);

                } else {

                    var subscriptionCommand = {
                        entityType: datasourceSubscription.entityType,
                        entityId: datasourceSubscription.entityId,
                        keys: tsKeys
                    };

                    subscriber = {
                        subscriptionCommands: [subscriptionCommand],
                        type: types.dataKeyType.timeseries
                    };

                    if (datasourceSubscription.type === types.widgetType.timeseries.value) {
                        subscriber.subsTw = subsTw;
                        updateRealtimeSubscriptionCommand(subscriptionCommand, subsTw);

                        if (subsTw.aggregation.stateData) {
                            subscriber.firstStateSubscriptionCommand = createFirstStateHistoryCommand(subsTw.startTs, tsKeys);
                            subscriber.historyCommands = [subscriber.firstStateSubscriptionCommand];
                        }
                        dataAggregator = createRealtimeDataAggregator(subsTw, tsKeyNames, types.dataKeyType.timeseries);
                        subscriber.onData = function(data, subscriptionId) {
                            if (this.subsTw.aggregation.stateData &&
                                this.firstStateSubscriptionCommand && this.firstStateSubscriptionCommand.cmdId == subscriptionId) {
                                if (this.data) {
                                    onStateHistoryData(data, this.data, this.subsTw.aggregation.limit,
                                        this.subsTw.startTs, this.subsTw.startTs + this.subsTw.aggregation.timeWindow,
                                        (data) => {
                                            dataAggregator.onData(data, false, false, true);
                                        });
                                    this.stateDataReceived = true;
                                } else {
                                    this.firstStateData = data;
                                }
                            } else {
                                if (this.subsTw.aggregation.stateData && !this.stateDataReceived) {
                                    if (this.firstStateData) {
                                        onStateHistoryData(this.firstStateData, data, this.subsTw.aggregation.limit,
                                            this.subsTw.startTs, this.subsTw.startTs + this.subsTw.aggregation.timeWindow,
                                            (data) => {
                                                dataAggregator.onData(data, false, false, true);
                                            });
                                        this.stateDataReceived = true;
                                    } else {
                                        this.data = data;
                                    }
                                } else {
                                    dataAggregator.onData(data, false, false, true);
                                }
                            }
                        }
                        subscriber.onReconnected = function() {
                            var newSubsTw = null;
                            for (var i2 = 0; i2 < listeners.length; i2++) {
                                var listener = listeners[i2];
                                if (!newSubsTw) {
                                    newSubsTw = listener.updateRealtimeSubscription();
                                } else {
                                    listener.setRealtimeSubscription(newSubsTw);
                                }
                            }
                            this.subsTw = newSubsTw;
                            this.firstStateData = null;
                            this.data = null;
                            this.stateDataReceived = false;
                            updateRealtimeSubscriptionCommand(this.subscriptionCommands[0], this.subsTw);
                            if (this.subsTw.aggregation.stateData) {
                                updateFirstStateHistoryCommand(this.firstStateSubscriptionCommand, this.subsTw.startTs);
                            }
                            dataAggregator.reset(newSubsTw.startTs,  newSubsTw.aggregation.timeWindow, newSubsTw.aggregation.interval);
                        }
                    } else {
                        subscriber.onReconnected = function() {}
                        subscriber.onData = function(data) {
                            if (data.data) {
                                onData(data.data, types.dataKeyType.timeseries, true);
                            }
                        }
                    }

                    telemetryWebsocketService.subscribe(subscriber);
                    subscribers.push(subscriber);

                }
            }

            if (dsKeys.length > 0) {

                if (historyDepth) {

                    historyCommand = {
                        entityType: datasourceSubscription.entityType,
                        entityId: datasourceSubscription.entityId,
                        keys: dsKeys,
                        startDs: subsDw.fixedWindow.startDepthFt,
                        endDs: subsDw.fixedWindow.endDepthFt,
                        interval: subsDw.aggregation.interval,
                        limit: subsDw.aggregation.limit,
                        agg: subsDw.aggregation.type
                    };

                    subscriber = {
                        historyCommands: [ historyCommand ],
                        type: types.dataKeyType.depthSeries,
                        subsDw: subsDw
                    };

                    if (subsDw.aggregation.stateData) {
                        subscriber.firstStateHistoryCommand = createFirstStateDepthHistoryCommand(subsDw.fixedWindow.startDepthFt, dsKeys);
                        subscriber.historyCommands.push(subscriber.firstStateHistoryCommand);
                    }

                    subscriber.onData = function (data, subscriptionId) {
                        if (this.subsDw.aggregation.stateData &&
                            this.firstStateHistoryCommand && this.firstStateHistoryCommand.cmdId == subscriptionId) {
                            if (this.data) {
                                onStateDepthHistoryData(data, this.data, this.subsDw.aggregation.limit,
                                    subsDw.fixedWindow.startDepthFt, this.subsDw.fixedWindow.endDepthFt,
                                    (data) => {
                                        onData(data.data, types.dataKeyType.depthSeries, true);
                                    });
                            } else {
                                this.firstStateData = data;
                            }
                        } else {
                            if (this.subsDw.aggregation.stateData) {
                                if (this.firstStateData) {
                                    onStateDepthHistoryData(this.firstStateData, data, this.subsDw.aggregation.limit,
                                        this.subsDw.fixedWindow.startDepthFt, this.subsDw.fixedWindow.endDepthFt,
                                        (data) => {
                                            onData(data.data, types.dataKeyType.depthSeries, true);
                                        });
                                } else {
                                    this.data = data;
                                }
                            } else {
                                for (key in data.data) {
                                    var keyData = data.data[key];
                                    data.data[key] = $filter('orderBy')(keyData, '+this[0]');
                                }
                                onData(data.data, types.dataKeyType.depthSeries, true);
                            }
                        }
                    };
                    subscriber.onReconnected = function() {};
                    telemetryWebsocketService.subscribe(subscriber);
                    subscribers.push(subscriber);

                }
                else {

                    subscriptionCommand = {
                        entityType: datasourceSubscription.entityType,
                        entityId: datasourceSubscription.entityId,
                        keys: dsKeys
                    };

                    subscriber = {
                        subscriptionCommands: [subscriptionCommand],
                        type: types.dataKeyType.depthSeries
                    };

                    if (datasourceSubscription.type === types.widgetType.depthseries.value) {
                        subscriber.subsDw = subsDw;
                        updateRealtimeDepthSubscriptionCommand(subscriptionCommand, subsDw);

                        if (subsDw.aggregation.stateData) {
                            subscriber.firstStateSubscriptionCommand = createFirstStateDepthHistoryCommand(subsDw.startDs, dsKeys);
                            subscriber.historyCommands = [subscriber.firstStateSubscriptionCommand];
                        }
                        dataAggregator = createRealtimeDepthDataAggregator(subsDw, dsKeyNames, types.dataKeyType.depthSeries);
                        subscriber.onData = function(data, subscriptionId) {
                            if (this.subsDw.aggregation.stateData &&
                                this.firstStateSubscriptionCommand && this.firstStateSubscriptionCommand.cmdId == subscriptionId) {
                                if (this.data) {
                                    onStateDepthHistoryData(data, this.data, this.subsDw.aggregation.limit,
                                        this.subsDw.startDs, this.subsDw.startDs + this.subsDw.aggregation.depthWindow,
                                        (data) => {
                                            dataAggregator.onData(data, false, false, true);
                                        });
                                    this.stateDataReceived = true;
                                } else {
                                    this.firstStateData = data;
                                }
                            } else {
                                if (this.subsDw.aggregation.stateData && !this.stateDataReceived) {
                                    if (this.firstStateData) {
                                        onStateDepthHistoryData(this.firstStateData, data, this.subsDw.aggregation.limit,
                                            this.subsDw.startDs, this.subsDw.startDs + this.subsDw.aggregation.depthWindow,
                                            (data) => {
                                                dataAggregator.onData(data, false, false, true);
                                            });
                                        this.stateDataReceived = true;
                                    } else {
                                        this.data = data;
                                    }
                                } else {
                                    dataAggregator.onData(data, false, false, true);
                                }
                            }
                        }
                        subscriber.onReconnected = function() {
                            var newSubsDw = null;
                            for (var i2 = 0; i2 < listeners.length; i2++) {
                                var listener = listeners[i2];
                                if (!newSubsDw) {
                                    newSubsDw = listener.updateRealtimeDepthSubscription();
                                } else {
                                    listener.setRealtimeDepthSubscription(newSubsDw);
                                }
                            }
                            this.subsDw = newSubsDw;
                            this.firstStateData = null;
                            this.data = null;
                            this.stateDataReceived = false;
                            updateRealtimeDepthSubscriptionCommand(this.subscriptionCommands[0], this.subsDw);
                            if (this.subsDw.aggregation.stateData) {
                                updateFirstStateDepthHistoryCommand(this.firstStateSubscriptionCommand, this.subsDw.startDs);
                            }
                            dataAggregator.reset(newSubsDw.startDs,  newSubsDw.aggregation.depthWindow, newSubsDw.aggregation.interval);
                        }
                    }
                    else {
                        subscriber.onReconnected = function() {}
                        subscriber.onData = function(data) {
                            if (data.data) {
                                onData(data.data, types.dataKeyType.depthSeries, true);
                            }
                        }
                    }

                    telemetryWebsocketService.subscribe(subscriber);
                    subscribers.push(subscriber);

                }
            }

            if (attrKeys.length > 0) {

                var attrsSubscriptionCommand = {
                    entityType: datasourceSubscription.entityType,
                    entityId: datasourceSubscription.entityId,
                    keys: attrKeys
                };

                subscriber = {
                    subscriptionCommands: [attrsSubscriptionCommand],
                    type: types.dataKeyType.attribute,
                    onData: function (data) {
                        if (data.data) {
                            onData(data.data, types.dataKeyType.attribute, true);
                        }
                    },
                    onReconnected: function() {}
                };

                telemetryWebsocketService.subscribe(subscriber);
                subscribers.push(subscriber);

            }

        } else if (datasourceType === types.datasourceType.function) {
            if (datasourceSubscription.type === types.widgetType.timeseries.value) {
                for (key in dataKeys) {
                    var dataKeyList = dataKeys[key];
                    for (var index = 0; index < dataKeyList.length; index++) {
                        dataKey = dataKeyList[index];
                        tsKeyNames.push(dataKey.name+'_'+dataKey.index);
                    }
                }
                dataAggregator = createRealtimeDataAggregator(subsTw, tsKeyNames, types.dataKeyType.function);
            }
            tickScheduledTime = currentTime();
            if (history) {
                onTick(false);
            } else {
                timer = $timeout(
                    function() {
                        onTick(true)
                    },
                    0,
                    false
                );
            }
        }
    }

    function createFirstStateHistoryCommand(startTs, tsKeys) {
        return {
            entityType: datasourceSubscription.entityType,
            entityId: datasourceSubscription.entityId,
            keys: tsKeys,
            startTs: startTs - YEAR,
            endTs: startTs,
            interval: 1000,
            limit: 1,
            agg: types.aggregation.none.value
        };
    }

    function createFirstStateDepthHistoryCommand(startDs, dsKeys) {
        return {
            entityType: datasourceSubscription.entityType,
            entityId: datasourceSubscription.entityId,
            keys: dsKeys,
            startDs: startDs - 10,
            endDs: startDs,
            interval: 1000,
            limit: 1,
            agg: types.aggregation.none.value
        };
    }

    function updateFirstStateHistoryCommand(stateHistoryCommand, startTs) {
        stateHistoryCommand.startTs = startTs - YEAR;
        stateHistoryCommand.endTs = startTs;
    }

    function updateFirstStateDepthHistoryCommand(stateHistoryCommand, startDs) {
        stateHistoryCommand.startDs = startDs - 10;
        stateHistoryCommand.endDs = startDs;
    }

    function onStateHistoryData(firstStateData, data, limit, startTs, endTs, onData) {
        for (var key in data.data) {
            var keyData = data.data[key];
            data.data[key] = $filter('orderBy')(keyData, '+this[0]');
            keyData = data.data[key];
            if (keyData.length < limit) {
                var firstStateKeyData = firstStateData.data[key];
                if (firstStateKeyData.length) {
                    var firstStateDataTsKv = firstStateKeyData[0];
                    firstStateDataTsKv[0] = startTs;
                    firstStateKeyData = [
                        [ startTs, firstStateKeyData[0][1] ]
                    ];
                    keyData.unshift(firstStateDataTsKv);
                }
            }
            if (keyData.length) {
                var lastTsKv = angular.copy(keyData[keyData.length-1]);
                lastTsKv[0] = endTs;
                keyData.push(lastTsKv);
            }
        }
        onData(data);
    }

    function onStateDepthHistoryData(firstStateData, data, limit, startDs, endDs, onData) {
        for (var key in data.data) {
            var keyData = data.data[key];
            data.data[key] = $filter('orderBy')(keyData, '+this[0]');
            keyData = data.data[key];
            if (keyData.length < limit) {
                var firstStateKeyData = firstStateData.data[key];
                if (firstStateKeyData.length) {
                    var firstStateDataDsKv = firstStateKeyData[0];
                    firstStateDataDsKv[0] = startDs;
                    firstStateKeyData = [
                        [ startDs, firstStateKeyData[0][1] ]
                    ];
                    keyData.unshift(firstStateDataDsKv);
                }
            }
            if (keyData.length) {
                var lastDsKv = angular.copy(keyData[keyData.length-1]);
                lastDsKv[0] = endDs;
                keyData.push(lastDsKv);
            }
        }
        onData(data);
    }

    function createRealtimeDataAggregator(subsTw, tsKeyNames, dataKeyType) {
        return new DataAggregator(
            function(data, apply) {
                onData(data, dataKeyType, apply);
            },
            tsKeyNames,
            subsTw.startTs,
            subsTw.aggregation.limit,
            subsTw.aggregation.type,
            subsTw.aggregation.timeWindow,
            subsTw.aggregation.interval,
            subsTw.aggregation.stateData,
            types,
            $timeout,
            $filter
        );
    }

    function createRealtimeDepthDataAggregator(subsDw, dsKeyNames, dataKeyType) {
        return new DepthDataAggregator(
            function(data, apply) {
                onData(data, dataKeyType, apply);
            },
            dsKeyNames,
            subsDw.startDs,
            subsDw.aggregation.limit,
            subsDw.aggregation.type,
            subsDw.aggregation.depthWindow,
            subsDw.aggregation.interval,
            subsDw.aggregation.stateData,
            types,
            $timeout,
            $filter
        );
    }

    function updateRealtimeSubscriptionCommand(subscriptionCommand, subsTw) {
        subscriptionCommand.startTs = subsTw.startTs;
        //subscriptionCommand.startDs = subsTw.startTs;
        subscriptionCommand.timeWindow = subsTw.aggregation.timeWindow;
        //subscriptionCommand.depthWindow = subsTw.aggregation.timeWindow;
        subscriptionCommand.interval = subsTw.aggregation.interval;
        subscriptionCommand.limit = subsTw.aggregation.limit;
        subscriptionCommand.agg = subsTw.aggregation.type;
    }

    function updateRealtimeDepthSubscriptionCommand(subscriptionCommand, subsDw) {
        subscriptionCommand.startDs = subsDw.startDs;
        subscriptionCommand.depthWindow = subsDw.aggregation.depthWindow;
        subscriptionCommand.interval = subsDw.aggregation.interval;
        subscriptionCommand.limit = subsDw.aggregation.limit;
        subscriptionCommand.agg = subsDw.aggregation.type;
    }

    function unsubscribe() {
        if (timer) {
            $timeout.cancel(timer);
            timer = null;
        }
        if (datasourceType === types.datasourceType.entity) {
            for (var i=0;i<subscribers.length;i++) {
                var subscriber = subscribers[i];
                telemetryWebsocketService.unsubscribe(subscriber);
                if (subscriber.onDestroy) {
                    subscriber.onDestroy();
                }
            }
            subscribers.length = 0;
        }
        if (dataAggregator) {
            dataAggregator.destroy();
            dataAggregator = null;
        }
    }

    function generateSeries(dataKey, index, startTime, endTime) {
        var data = [];
        var prevSeries;
        var datasourceDataKey = dataKey.key + '_' + index;
        var datasourceKeyData = datasourceData[datasourceDataKey].data;
        if (datasourceKeyData.length > 0) {
            prevSeries = datasourceKeyData[datasourceKeyData.length - 1];
        } else {
            prevSeries = [0, 0];
        }
        for (var time = startTime; time <= endTime && (timer || history); time += frequency) {
            var series = [];
            series.push(time);
            var value = dataKey.func(time, prevSeries[1]);
            series.push(value);
            data.push(series);
            prevSeries = series;
        }
        if (data.length > 0) {
            dataKey.lastUpdateTime = data[data.length - 1][0];
        }
        return data;
    }

    function generateLatest(dataKey, apply) {
        var prevSeries;
        var datasourceKeyData = datasourceData[dataKey.key].data;
        if (datasourceKeyData.length > 0) {
            prevSeries = datasourceKeyData[datasourceKeyData.length - 1];
        } else {
            prevSeries = [0, 0];
        }
        var series = [];
        var time = (new Date).getTime();
        series.push(time);
        var value = dataKey.func(time, prevSeries[1]);
        series.push(value);
        datasourceData[dataKey.key].data = [series];
        for (var i = 0; i < listeners.length; i++) {
            var listener = listeners[i];
            listener.dataUpdated(datasourceData[dataKey.key],
                listener.datasourceIndex,
                dataKey.index, apply);
        }
    }

    /* eslint-disable */
    function currentTime() {
        return window.performance && window.performance.now ?
            window.performance.now() : Date.now();
    }
    /* eslint-enable */


    function onTick(apply) {

        var now = currentTime();
        tickElapsed += now - tickScheduledTime;
        tickScheduledTime = now;

        if (timer) {
            $timeout.cancel(timer);
        }

        var key;
        if (datasourceSubscription.type === types.widgetType.timeseries.value) {
            var startTime;
            var endTime;
            var delta;
            var generatedData = {
                data: {
                }
            };
            if (!history) {
                delta = Math.floor(tickElapsed / frequency);
            }
            var deltaElapsed = history ? frequency : delta * frequency;
            tickElapsed = tickElapsed - deltaElapsed;
            for (key in dataKeys) {
                var dataKeyList = dataKeys[key];
                for (var index = 0; index < dataKeyList.length && (timer || history); index ++) {
                    var dataKey = dataKeyList[index];
                    if (!startTime) {
                        if (realtime) {
                            if (dataKey.lastUpdateTime) {
                                startTime = dataKey.lastUpdateTime + frequency;
                                endTime = dataKey.lastUpdateTime + deltaElapsed;
                            } else {
                                startTime = datasourceSubscription.subscriptionTimewindow.startTs;
                                endTime = startTime + datasourceSubscription.subscriptionTimewindow.realtimeWindowMs + frequency;
                                if (datasourceSubscription.subscriptionTimewindow.aggregation.type == types.aggregation.none.value) {
                                    var time = endTime - frequency * datasourceSubscription.subscriptionTimewindow.aggregation.limit;
                                    startTime = Math.max(time, startTime);
                                }
                            }
                        } else {
                            startTime = datasourceSubscription.subscriptionTimewindow.fixedWindow.startTimeMs;
                            endTime = datasourceSubscription.subscriptionTimewindow.fixedWindow.endTimeMs;
                        }
                    }
                    var data = generateSeries(dataKey, index, startTime, endTime);
                    generatedData.data[dataKey.name+'_'+dataKey.index] = data;
                }
            }
            if (dataAggregator) {
                dataAggregator.onData(generatedData, true, history, apply);
            }
        } else if (datasourceSubscription.type === types.widgetType.latest.value) {
            for (key in dataKeys) {
                generateLatest(dataKeys[key], apply);
            }
        }

        if (!history) {
            timer = $timeout(function() {onTick(true)}, frequency, false);
        }
    }

    function isNumeric(val) {
        return (val - parseFloat( val ) + 1) >= 0;
    }

    function convertValue(val) {
        if (val && isNumeric(val)) {
            return Number(val);
        } else {
            return val;
        }
    }

    function onData(sourceData, type, apply) {
        for (var keyName in sourceData) {
            var keyData = sourceData[keyName];
            var key = keyName + '_' + type;
            var dataKeyList = dataKeys[key];
            for (var keyIndex = 0; dataKeyList && keyIndex < dataKeyList.length; keyIndex++) {
                var datasourceKey = key + "_" + keyIndex;
                if (datasourceData[datasourceKey].data) {
                    var dataKey = dataKeyList[keyIndex];
                    var data = [];
                    var prevSeries;
                    var datasourceKeyData;
                    var update = false;
                    if (realtime) {
                        datasourceKeyData = [];
                    } else {
                        datasourceKeyData = datasourceData[datasourceKey].data;
                    }
                    if (datasourceKeyData.length > 0) {
                        prevSeries = datasourceKeyData[datasourceKeyData.length - 1];
                    } else {
                        prevSeries = [0, 0];
                    }
                    if (datasourceSubscription.type === types.widgetType.timeseries.value) { // || condition here for depthseries.
                        var series, time, value;
                        for (var i = 0; i < keyData.length; i++) {
                            series = keyData[i];
                            time = series[0];
                            value = convertValue(series[1]);
                            if (dataKey.postFunc) {
                                value = dataKey.postFunc(time, value, prevSeries[1]);
                            }
                            series = [time, value];
                            data.push(series);
                            prevSeries = series;
                        }
                        update = true;
                    }else if(datasourceSubscription.type === types.widgetType.depthseries.value) {
                        var depth;
                        for (i = 0; i < keyData.length; i++) {
                            series = keyData[i];
                            depth = series[0];
                            value = convertValue(series[1]);
                            if (dataKey.postFunc) {
                                value = dataKey.postFunc(depth, value, prevSeries[1]);
                            }
                            series = [depth, value];
                            data.push(series);
                            prevSeries = series;
                        }
                        update = true;
                    } else if (datasourceSubscription.type === types.widgetType.latest.value) {
                        if (keyData.length > 0) {
                            series = keyData[0];
                            time = series[0];
                            value = convertValue(series[1]);
                            if (dataKey.postFunc) {
                                value = dataKey.postFunc(time, value, prevSeries[1]);
                            }
                            series = [time, value];
                            data.push(series);
                        }
                        update = true;
                    }
                    if (update) {
                        datasourceData[datasourceKey].data = data;
                        for (var i2 = 0; i2 < listeners.length; i2++) {
                            var listener = listeners[i2];
                            if (angular.isFunction(listener))
                              continue;
                            listener.dataUpdated(datasourceData[datasourceKey],
                                listener.datasourceIndex,
                                dataKey.index, apply);
                        }
                    }
                }
            }
        }
    }
}
