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
package org.thingsboard.server.extensions.core.plugin.telemetry;

public class TsData implements Comparable<TsData>{

    private final long ts;
    private final String value;

    public TsData(long ts, String value) {
        super();
        this.ts = ts;
        this.value = value;
    }

    public long getTs() {
        return ts;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(TsData o) {
        return Long.compare(ts, o.ts);
    }

}
