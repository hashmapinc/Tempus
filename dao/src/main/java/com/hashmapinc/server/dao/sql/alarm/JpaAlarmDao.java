/**
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
package com.hashmapinc.server.dao.sql.alarm;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.alarm.Alarm;
import com.hashmapinc.server.common.data.alarm.AlarmInfo;
import com.hashmapinc.server.common.data.alarm.AlarmQuery;
import com.hashmapinc.server.common.data.alarm.AlarmSearchStatus;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.relation.EntityRelation;
import com.hashmapinc.server.common.data.relation.RelationTypeGroup;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.alarm.AlarmDao;
import com.hashmapinc.server.dao.relation.RelationDao;
import com.hashmapinc.server.dao.sql.JpaAbstractDao;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.dao.alarm.BaseAlarmService;
import com.hashmapinc.server.dao.model.sql.AlarmEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Valerii Sosliuk on 5/19/2017.
 */
@Slf4j
@Component
@SqlDao
public class JpaAlarmDao extends JpaAbstractDao<AlarmEntity, Alarm> implements AlarmDao {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private RelationDao relationDao;

    @Override
    protected Class<AlarmEntity> getEntityClass() {
        return AlarmEntity.class;
    }

    @Override
    protected CrudRepository<AlarmEntity, String> getCrudRepository() {
        return alarmRepository;
    }

    @Override
    public ListenableFuture<Alarm> findLatestByOriginatorAndType(TenantId tenantId, EntityId originator, String type) {
        return service.submit(() -> {
            List<AlarmEntity> latest = alarmRepository.findLatestByOriginatorAndType(
                    UUIDConverter.fromTimeUUID(tenantId.getId()),
                    UUIDConverter.fromTimeUUID(originator.getId()),
                    originator.getEntityType(),
                    type,
                    new PageRequest(0, 1));
            return latest.isEmpty() ? null : DaoUtil.getData(latest.get(0));
        });
    }

    @Override
    public ListenableFuture<Alarm> findAlarmByIdAsync(UUID key) {
        return findByIdAsync(key);
    }

    @Override
    public ListenableFuture<List<AlarmInfo>> findAlarms(AlarmQuery query) {
        log.trace("Try to find alarms by entity [{}], status [{}] and pageLink [{}]", query.getAffectedEntityId(), query.getStatus(), query.getPageLink());
        EntityId affectedEntity = query.getAffectedEntityId();
        String searchStatusName;
        if (query.getSearchStatus() == null && query.getStatus() == null) {
            searchStatusName = AlarmSearchStatus.ANY.name();
        } else if (query.getSearchStatus() != null) {
            searchStatusName = query.getSearchStatus().name();
        } else {
            searchStatusName = query.getStatus().name();
        }
        String relationType = BaseAlarmService.ALARM_RELATION_PREFIX + searchStatusName;
        ListenableFuture<List<EntityRelation>> relations = relationDao.findRelations(affectedEntity, relationType, RelationTypeGroup.ALARM, EntityType.ALARM, query.getPageLink());
        return Futures.transform(relations, (AsyncFunction<List<EntityRelation>, List<AlarmInfo>>) input -> {
            List<ListenableFuture<AlarmInfo>> alarmFutures = new ArrayList<>(input.size());
            for (EntityRelation relation : input) {
                alarmFutures.add(Futures.transform(
                        findAlarmByIdAsync(relation.getTo().getId()),
                        (Function<Alarm, AlarmInfo>) AlarmInfo::new));
            }
            return Futures.successfulAsList(alarmFutures);
        });
    }
}
