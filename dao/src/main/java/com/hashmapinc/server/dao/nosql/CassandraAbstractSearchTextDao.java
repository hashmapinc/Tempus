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
package com.hashmapinc.server.dao.nosql;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.SearchTextEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Slf4j
public abstract class CassandraAbstractSearchTextDao<E extends SearchTextEntity<D>, D> extends CassandraAbstractModelDao<E, D> {

    @Override
    protected E updateSearchTextIfPresent(E entity) {
        if (entity.getSearchTextSource() != null) {
            entity.setSearchText(entity.getSearchTextSource().toLowerCase());
        } else {
            log.trace("Entity [{}] has null SearchTextSource", entity);
        }
        return entity;
    }

    protected List<E> findPageWithTextSearch(String searchView, List<Clause> clauses, TextPageLink pageLink) {
        return _findPageWithTextSearch(searchView, clauses, pageLink, false);
    }

    private List<E> _findPageWithTextSearch(String searchView, List<Clause> clauses, TextPageLink pageLink, boolean idsInClausePresent) {
        Select select = select().from(searchView);
        Where query = select.where();
        for (Clause clause : clauses) {
            query.and(clause);
        }
        query.limit(pageLink.getLimit());
        if (!StringUtils.isEmpty(pageLink.getTextOffset())) {
            query.and(eq(ModelConstants.SEARCH_TEXT_PROPERTY, pageLink.getTextOffset()));
            if(!idsInClausePresent)
                query.and(QueryBuilder.lt(ModelConstants.ID_PROPERTY, pageLink.getIdOffset()));
            List<E> result = findListByStatement(query);
            if (result.size() < pageLink.getLimit()) {
                select = select().from(searchView);
                query = select.where();
                for (Clause clause : clauses) {
                    query.and(clause);
                }
                final String textPropertyLowerBound = idsInClausePresent ? pageLink.getTextSearch() : pageLink.getTextOffset();
                query.and(QueryBuilder.gt(ModelConstants.SEARCH_TEXT_PROPERTY, textPropertyLowerBound));
                if (!StringUtils.isEmpty(pageLink.getTextSearch())) {
                    query.and(QueryBuilder.lt(ModelConstants.SEARCH_TEXT_PROPERTY, pageLink.getTextSearchBound()));
                }
                int limit = pageLink.getLimit() - result.size();
                query.limit(limit);
                result.addAll(findListByStatement(query));
            }
            return result;
        } else if (!StringUtils.isEmpty(pageLink.getTextSearch())) {
            query.and(QueryBuilder.gte(ModelConstants.SEARCH_TEXT_PROPERTY, pageLink.getTextSearch()));
            query.and(QueryBuilder.lt(ModelConstants.SEARCH_TEXT_PROPERTY, pageLink.getTextSearchBound()));
            return findListByStatement(query);
        } else {
            return findListByStatement(query);
        }
    }

    protected List<E> findPageWithTextSearchAndNoIdCompare(String searchView, List<Clause> clauses, TextPageLink pageLink) {
        return _findPageWithTextSearch(searchView, clauses, pageLink, true);
    }


}