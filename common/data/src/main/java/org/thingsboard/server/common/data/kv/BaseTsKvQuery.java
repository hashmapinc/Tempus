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
package org.thingsboard.server.common.data.kv;

import lombok.Data;

@Data
public class BaseTsKvQuery implements TsKvQuery {

    private final String key;
    private final long startTs;
    private final long endTs;
    private final long interval;
    private final int limit;
    private final Aggregation aggregation;

    public BaseTsKvQuery(String key, long startTs, long endTs, long interval, int limit, Aggregation aggregation) {
        this.key = key;
        this.startTs = startTs;
        this.endTs = endTs;
        this.interval = interval;
        this.limit = limit;
        this.aggregation = aggregation;
    }

    public BaseTsKvQuery(String key, long startTs, long endTs) {
        this(key, startTs, endTs, endTs-startTs, 1, Aggregation.AVG);
    }

}
