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
package com.hashmapinc.server.common.data.page;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class PaginatedResult<T>{
    private final List<T> data;
    private final int page;
    private long totalElements;
    private long totalPages;
    private boolean hasNext;
    private boolean hasPrevious;


    public PaginatedResult(@JsonProperty("data") List<T> data,
                           @JsonProperty("page") int page,
                           @JsonProperty("totalElements") long totalElements,
                           @JsonProperty("totalPages") long totalPages,
                           @JsonProperty("hasNext") boolean hasNext,
                           @JsonProperty("hasPrevious") boolean hasPrevious) {
        this.data = data;
        this.page = page;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

}
