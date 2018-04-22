/*
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
export default angular.module('tempus.api.depth', [])
    .factory('depthService', DepthService)
    .name;

const DECI_FT = 0;
const AVG_LIMIT = 200;
const MAX_LIMIT = 500;
const MIN_LIMIT = 10;
const MIN_INTEVAL = 10;
const MAX_INTEVAL = 30;
const START_DS = -10000;
var startDpt = DECI_FT;


/*@ngInject*/
function DepthService($translate, types) {

    var predefIntervals = [
        {
            name: $translate.instant('depthinterval.feet-interval', {feet: 1000}, 'messageformat'),
            value: 1000
        },
        {
            name: $translate.instant('depthinterval.feet-interval', {feet: 2000}, 'messageformat'),
            value: 2000
        },
        {
            name: $translate.instant('depthinterval.feet-interval', {feet: 3000}, 'messageformat'),
            value: 3000
        }
    ]

    var service = {
        minIntervalLimit: minIntervalLimit,
        maxIntervalLimit: maxIntervalLimit,
        getIntervals: getIntervals,
        defaultDepthwindow: defaultDepthwindow,
        toHistoryDepthwindow: toHistoryDepthwindow,
        matchesExistingInterval: matchesExistingInterval,
        createSubscriptionDepthwindow: createSubscriptionDepthwindow,
        avgAggregationLimit: function () {
            return AVG_LIMIT;
        }
    }

    return service;

    function defaultDepthwindow() {
        var depthwindow = {
            displayValue: "",
            selectedTab: 0,
            realtime: {
                interval: DECI_FT,
                depthwindowFt: 1000 // 10 Feet by default by default
            },
            history: {
                historyType: 0,
                interval: DECI_FT,
                depthwindowFt: 100,
                fixedDepthwindow: {
                    startDepthFt: 0,
                    endDepthFt: 3000
                }
            },
            aggregation: {
                type: types.aggregation.none.value,
                limit: 200
            }
        }
        return depthwindow;
    }

    function toHistoryDepthwindow(depthwindow, startDepthFt, endDepthFt) {

        var interval = 0;
        if (depthwindow.history) {
            interval = depthwindow.history.interval;
        } else if (depthwindow.realtime) {
            interval = depthwindow.realtime.interval;
        }

        var aggType;
        if (depthwindow.aggregation) {
            aggType = depthwindow.aggregation.type || types.aggregation.none.value;
        } else {
            aggType = types.aggregation.none.value;
        }

        var historyDepthwindow = {
            history: {
                fixedDepthwindow: {
                    startDepthFt: startDepthFt,
                    endDepthFt: endDepthFt
                },
                interval: boundIntervalToDepthwindow(endDepthFt - startDepthFt, interval, aggType)
            },
            aggregation: {
                type: aggType
            }
        }

        return historyDepthwindow;
    }

    function createSubscriptionDepthwindow(depthwindow, stDiff, stateData) {

        var subscriptionDepthwindow = {
            fixedWindow: null,
            realtimeWindowFt: null,
            aggregation: {
                interval: DECI_FT,
                limit: AVG_LIMIT,
                type: types.aggregation.none.value
            }
        };
        var aggDepthwindow = 0;
        if (stateData) {
            subscriptionDepthwindow.aggregation = {
                interval: DECI_FT,
                limit: MAX_LIMIT,
                type: types.aggregation.none.value,
                stateData: true
            };
        } else {
            subscriptionDepthwindow.aggregation = {
                interval: DECI_FT,
                limit: AVG_LIMIT,
                type: types.aggregation.none.value
            };
        }

        if (angular.isDefined(depthwindow.aggregation) && !stateData) {
            subscriptionDepthwindow.aggregation = {
                type: depthwindow.aggregation.type || types.aggregation.none.value,
                limit: depthwindow.aggregation.limit || AVG_LIMIT
            };
        }
        if (angular.isDefined(depthwindow.realtime)) {
            subscriptionDepthwindow.realtimeWindowFt = depthwindow.realtime.depthwindowFt;
            subscriptionDepthwindow.aggregation.interval =
                boundIntervalToDepthwindow(subscriptionDepthwindow.realtimeWindowFt, depthwindow.realtime.interval,
                    subscriptionDepthwindow.aggregation.type);

            subscriptionDepthwindow.startDs = START_DS;

            var startDiff = subscriptionDepthwindow.startDs % subscriptionDepthwindow.aggregation.interval;
            aggDepthwindow = subscriptionDepthwindow.realtimeWindowFt;
            if (startDiff) {
                subscriptionDepthwindow.startDs -= startDiff;
                aggDepthwindow += subscriptionDepthwindow.aggregation.interval;
            }
        } else if (angular.isDefined(depthwindow.history)) {
            if (angular.isDefined(depthwindow.history.depthwindowFt)) {
                subscriptionDepthwindow.fixedWindow = {
                    startDepthFt: startDpt - depthwindow.history.depthwindowFt,
                    endDepthFt: startDpt
                }
                startDpt = startDpt + depthwindow.history.depthwindowFt;
                aggDepthwindow = depthwindow.history.depthwindowFt;

            } else {
                subscriptionDepthwindow.fixedWindow = {
                    startDepthFt: depthwindow.history.fixedDepthwindow.startDepthFt,
                    endDepthFt: depthwindow.history.fixedDepthwindow.endDepthFt
                }
                aggDepthwindow = subscriptionDepthwindow.fixedWindow.endDepthFt - subscriptionDepthwindow.fixedWindow.startDepthFt;
            }
            subscriptionDepthwindow.startDs = subscriptionDepthwindow.fixedWindow.startDepthFt;
            subscriptionDepthwindow.aggregation.interval =
                boundIntervalToDepthwindow(aggDepthwindow, depthwindow.history.interval, subscriptionDepthwindow.aggregation.type);
        }
        var aggregation = subscriptionDepthwindow.aggregation;
        aggregation.depthWindow = aggDepthwindow;
        if (aggregation.type !== types.aggregation.none.value) {
            aggregation.limit = Math.ceil(aggDepthwindow / subscriptionDepthwindow.aggregation.interval);
        }
        return subscriptionDepthwindow;
    }

    function matchesExistingInterval(intervalFt) {
        for (var interval in predefIntervals){
            if(intervalFt === predefIntervals[interval].value)
                return false;
        }
        return true;
    }

    function getIntervals() {
        var intervals = [];
        for (var i in predefIntervals) {
            var interval = predefIntervals[i];
                intervals.push(interval);
        }
        return intervals;
    }

    function minIntervalLimit(depthwindow) {
        depthwindow / MAX_LIMIT;
        return MIN_INTEVAL;
    }

    function maxIntervalLimit(depthwindow) {
        depthwindow / MIN_LIMIT;
        return MAX_INTEVAL;
    }


    function boundIntervalToDepthwindow(depthwindow, intervalFt, aggType) {
        if (aggType === types.aggregation.none.value) {
            return DECI_FT;
        }
        return Number(intervalFt);
    }


}