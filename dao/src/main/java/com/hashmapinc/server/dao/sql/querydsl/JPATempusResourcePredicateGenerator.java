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
package com.hashmapinc.server.dao.sql.querydsl;

import com.hashmapinc.server.dao.querybuilder.ResourceCriteria;
import com.hashmapinc.server.dao.querybuilder.TempusResourcePredicate;
import com.hashmapinc.server.dao.querybuilder.TempusResourcePredicateGenerator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;

import java.beans.Introspector;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public class JPATempusResourcePredicateGenerator implements TempusResourcePredicateGenerator<BooleanExpression> {

    private final Class<?> clazz;

    public JPATempusResourcePredicateGenerator(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public TempusResourcePredicate<BooleanExpression> generatePredicate(ResourceCriteria resourceCriteria) {
        final String className = Introspector.decapitalize(clazz.getSimpleName());
        final PathBuilder<?> entityPath = new PathBuilder<>(clazz, className);
        final String criteriaValue = resourceCriteria.getValue().toString().trim();
        if (isNumeric(criteriaValue)) {
            NumberPath<Integer> path = entityPath.getNumber(resourceCriteria.getKey(), Integer.class);
            int value = Integer.parseInt(criteriaValue);
            switch (resourceCriteria.getOperation()) {
                case EQUALS:
                    return new TempusResourcePredicate<>(path.eq(value));
                case GREATER_THAN:
                    return new TempusResourcePredicate<>(path.gt(value));
                case LESS_THAN:
                    return new TempusResourcePredicate<>(path.lt(value));
            }
        }
        else {
            StringPath path = entityPath.getString(resourceCriteria.getKey());
            switch (resourceCriteria.getOperation()) {
                case EQUALS:
                    return new TempusResourcePredicate<>(path.eq(criteriaValue));
                case LIKE:
                    return new TempusResourcePredicate<>((path.like("%" + criteriaValue.toLowerCase() + "%")));
                case CONTAINS:
                    final List<String> strings = Arrays.asList((String[]) resourceCriteria.getValue());
                    return new TempusResourcePredicate<>((path.in(strings)));
                case GREATER_THAN:
                    return new TempusResourcePredicate<>(path.gt(criteriaValue));
                case LESS_THAN:
                    return new TempusResourcePredicate<>(path.lt(criteriaValue));
            }
        }
        return null;
    }

}
