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
import com.hashmapinc.server.dao.querybuilder.TempusResourceQueryBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JPATempusResourcePredicateBuilder implements TempusResourceQueryBuilder<JPATempusResourcePredicateBuilder, BooleanExpression> {
    private final List<ResourceCriteria> params;
    private final Class<?> clazz ;
    private Map<String, String> fieldToColumnName = Collections.emptyMap();

    private final TempusResourcePredicateGenerator<BooleanExpression> tempusResourcePredicateGenerator;

    public JPATempusResourcePredicateBuilder(Class<?> clazz) {
        params = new ArrayList<>();
        this.clazz = clazz;
        this.tempusResourcePredicateGenerator = new JPATempusResourcePredicateGenerator(clazz);
    }

    @Override
    public JPATempusResourcePredicateBuilder with(
            String key, ResourceCriteria.Operation operation, Object value) {

        params.add(new ResourceCriteria(key, operation, value));
        return this;
    }

    @Override
    public JPATempusResourcePredicateBuilder with(ResourceCriteria resourceCriteria) {
        params.add(resourceCriteria);
        return this;
    }

    @Override
    public List<TempusResourcePredicate<BooleanExpression>> build() {
        if (params.isEmpty()) {
            return Collections.emptyList();
        }

        if(fieldToColumnName.isEmpty()){
            fieldToColumnName = getAllFieldsToColumnNameMapping();
        }

           return params.stream()
                .map(transformColumnNameToFieldName(fieldToColumnName))
                .map(tempusResourcePredicateGenerator::generatePredicate)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    private Map<String, String> getAllFieldsToColumnNameMapping() {
        final Map<String, String> fieldToColumnName = generateFieldToColumnMap(clazz);
        Class<?> current = clazz;
        while(!current.getSuperclass().equals(Object.class)){ // as we don't want to process Object.class
            current = current.getSuperclass();
            fieldToColumnName.putAll(generateFieldToColumnMap(current));
        }
        return fieldToColumnName;
    }

    private Function<ResourceCriteria, ResourceCriteria> transformColumnNameToFieldName(Map<String, String> fieldToColumnName) {
        return param -> new ResourceCriteria(fieldToColumnName.get(param.getKey()), param.getOperation(), param.getValue());
    }

    private Map<String, String> generateFieldToColumnMap(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toMap(this::getColumnName, Field::getName));
    }

    private String getColumnName(Field field) {
        final List<String> columnName = Arrays.stream(field.getDeclaredAnnotations())
                .filter(annotation -> annotation instanceof Column)
                .map(annotation -> ((Column) annotation).name())
                .collect(Collectors.toList());
        return !columnName.isEmpty() ? columnName.get(0) : convertFieldNameToDBColumnName(field.getName());

    }

    private String convertFieldNameToDBColumnName(String fieldName) {
        return fieldName.replaceAll("([A-Z])", "_$1").toLowerCase();
    }
}
