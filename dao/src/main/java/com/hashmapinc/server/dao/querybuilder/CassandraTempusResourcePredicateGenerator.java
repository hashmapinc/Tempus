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
package com.hashmapinc.server.dao.querybuilder;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CassandraTempusResourcePredicateGenerator implements TempusResourcePredicateGenerator<Clause> {

    @Override
    public TempusResourcePredicate<Clause> generatePredicate(ResourceCriteria resourceCriteria) {
        //QueryBuilder
        final String resourceCriteriaKey = resourceCriteria.getKey();
        final Object criteriaValue = resourceCriteria.getValue();
        switch (resourceCriteria.getOperation()) {
            case EQUALS:
                return new TempusResourcePredicate<>(QueryBuilder.eq(resourceCriteriaKey, criteriaValue));
            case GREATER_THAN:
                return new TempusResourcePredicate<>(QueryBuilder.gt(resourceCriteriaKey, criteriaValue));
            case LESS_THAN:
                return new TempusResourcePredicate<>(QueryBuilder.lt(resourceCriteriaKey, criteriaValue));
            case CONTAINS:
                final List<UUID> strings = Arrays.asList((UUID[]) resourceCriteria.getValue());
                return new TempusResourcePredicate<>(QueryBuilder.in(resourceCriteriaKey, strings));
            /*case LIKE:
                return new TempusResourcePredicate<>(QueryBuilder.like(resourceCriteriaKey, "%" +criteriaValue + "%" ));*/
        }
        return null;
    }
}
