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
package org.thingsboard.server.dao.alarm;


import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.alarm.*;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TimePageData;
import org.thingsboard.server.common.data.page.TimePageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.dao.entity.AbstractEntityService;
import org.thingsboard.server.dao.entity.EntityService;
import org.thingsboard.server.dao.exception.DataValidationException;
import org.thingsboard.server.common.data.relation.EntityRelationsQuery;
import org.thingsboard.server.common.data.relation.EntitySearchDirection;
import org.thingsboard.server.common.data.relation.RelationsSearchParameters;
import org.thingsboard.server.dao.service.DataValidator;
import org.thingsboard.server.dao.tenant.TenantDao;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.thingsboard.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class BaseAlarmService extends AbstractEntityService implements AlarmService {

    public static final String ALARM_RELATION_PREFIX = "ALARM_";

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private EntityService entityService;

    protected ExecutorService readResultsProcessingExecutor;

    @PostConstruct
    public void startExecutor() {
        readResultsProcessingExecutor = Executors.newCachedThreadPool();
    }

    @PreDestroy
    public void stopExecutor() {
        if (readResultsProcessingExecutor != null) {
            readResultsProcessingExecutor.shutdownNow();
        }
    }

    @Override
    public Alarm createOrUpdateAlarm(Alarm alarm) {
        alarmDataValidator.validate(alarm);
        try {
            if (alarm.getStartTs() == 0L) {
                alarm.setStartTs(System.currentTimeMillis());
            }
            if (alarm.getEndTs() == 0L) {
                alarm.setEndTs(alarm.getStartTs());
            }
            if (alarm.getId() == null) {
                Alarm existing = alarmDao.findLatestByOriginatorAndType(alarm.getTenantId(), alarm.getOriginator(), alarm.getType()).get();
                if (existing == null || existing.getStatus().isCleared()) {
                    return createAlarm(alarm);
                } else {
                    return updateAlarm(existing, alarm);
                }
            } else {
                return updateAlarm(alarm).get();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ListenableFuture<Alarm> findLatestByOriginatorAndType(TenantId tenantId, EntityId originator, String type) {
        return alarmDao.findLatestByOriginatorAndType(tenantId, originator, type);
    }

    private Alarm createAlarm(Alarm alarm) throws InterruptedException, ExecutionException {
        log.debug("New Alarm : {}", alarm);
        Alarm saved = alarmDao.save(alarm);
        createAlarmRelations(saved);
        return saved;
    }

    private void createAlarmRelations(Alarm alarm) throws InterruptedException, ExecutionException {
        if (alarm.isPropagate()) {
            EntityRelationsQuery query = new EntityRelationsQuery();
            query.setParameters(new RelationsSearchParameters(alarm.getOriginator(), EntitySearchDirection.TO, Integer.MAX_VALUE));
            List<EntityId> parentEntities = relationService.findByQuery(query).get().stream().map(r -> r.getFrom()).collect(Collectors.toList());
            for (EntityId parentId : parentEntities) {
                createAlarmRelation(parentId, alarm.getId(), alarm.getStatus(), true);
            }
        }
        createAlarmRelation(alarm.getOriginator(), alarm.getId(), alarm.getStatus(), true);
    }

    private ListenableFuture<Alarm> updateAlarm(Alarm update) {
        alarmDataValidator.validate(update);
        return getAndUpdate(update.getId(), new Function<Alarm, Alarm>() {
            @Nullable
            @Override
            public Alarm apply(@Nullable Alarm alarm) {
                if (alarm == null) {
                    return null;
                } else {
                    return updateAlarm(alarm, update);
                }
            }
        });
    }

    private Alarm updateAlarm(Alarm oldAlarm, Alarm newAlarm) {
        AlarmStatus oldStatus = oldAlarm.getStatus();
        AlarmStatus newStatus = newAlarm.getStatus();
        boolean oldPropagate = oldAlarm.isPropagate();
        boolean newPropagate = newAlarm.isPropagate();
        Alarm result = alarmDao.save(merge(oldAlarm, newAlarm));
        if (!oldPropagate && newPropagate) {
            try {
                createAlarmRelations(result);
            } catch (InterruptedException | ExecutionException e) {
                log.warn("Failed to update alarm relations [{}]", result, e);
                throw new RuntimeException(e);
            }
        } else if (oldStatus != newStatus) {
            updateRelations(oldAlarm, oldStatus, newStatus);
        }
        return result;
    }

    @Override
    public ListenableFuture<Boolean> ackAlarm(AlarmId alarmId, long ackTime) {
        return getAndUpdate(alarmId, new Function<Alarm, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable Alarm alarm) {
                if (alarm == null || alarm.getStatus().isAck()) {
                    return false;
                } else {
                    AlarmStatus oldStatus = alarm.getStatus();
                    AlarmStatus newStatus = oldStatus.isCleared() ? AlarmStatus.CLEARED_ACK : AlarmStatus.ACTIVE_ACK;
                    alarm.setStatus(newStatus);
                    alarm.setAckTs(ackTime);
                    alarmDao.save(alarm);
                    updateRelations(alarm, oldStatus, newStatus);
                    return true;
                }
            }
        });
    }

    @Override
    public ListenableFuture<Boolean> clearAlarm(AlarmId alarmId, long clearTime) {
        return getAndUpdate(alarmId, new Function<Alarm, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable Alarm alarm) {
                if (alarm == null || alarm.getStatus().isCleared()) {
                    return false;
                } else {
                    AlarmStatus oldStatus = alarm.getStatus();
                    AlarmStatus newStatus = oldStatus.isAck() ? AlarmStatus.CLEARED_ACK : AlarmStatus.CLEARED_UNACK;
                    alarm.setStatus(newStatus);
                    alarm.setClearTs(clearTime);
                    alarmDao.save(alarm);
                    updateRelations(alarm, oldStatus, newStatus);
                    return true;
                }
            }
        });
    }

    @Override
    public ListenableFuture<Alarm> findAlarmByIdAsync(AlarmId alarmId) {
        log.trace("Executing findAlarmById [{}]", alarmId);
        validateId(alarmId, "Incorrect alarmId " + alarmId);
        return alarmDao.findAlarmByIdAsync(alarmId.getId());
    }

    @Override
    public ListenableFuture<AlarmInfo> findAlarmInfoByIdAsync(AlarmId alarmId) {
        log.trace("Executing findAlarmInfoByIdAsync [{}]", alarmId);
        validateId(alarmId, "Incorrect alarmId " + alarmId);
        return Futures.transform(alarmDao.findAlarmByIdAsync(alarmId.getId()),
                (AsyncFunction<Alarm, AlarmInfo>) alarm1 -> {
                    AlarmInfo alarmInfo = new AlarmInfo(alarm1);
                    return Futures.transform(
                            entityService.fetchEntityNameAsync(alarmInfo.getOriginator()), (Function<String, AlarmInfo>)
                                    originatorName -> {
                                        alarmInfo.setOriginatorName(originatorName);
                                        return alarmInfo;
                                    }
                    );
                });
    }

    @Override
    public ListenableFuture<TimePageData<AlarmInfo>> findAlarms(AlarmQuery query) {
        ListenableFuture<List<AlarmInfo>> alarms = alarmDao.findAlarms(query);
        if (query.getFetchOriginator() != null && query.getFetchOriginator().booleanValue()) {
            alarms = Futures.transform(alarms, (AsyncFunction<List<AlarmInfo>, List<AlarmInfo>>) input -> {
                List<ListenableFuture<AlarmInfo>> alarmFutures = new ArrayList<>(input.size());
                for (AlarmInfo alarmInfo : input) {
                    alarmFutures.add(Futures.transform(
                            entityService.fetchEntityNameAsync(alarmInfo.getOriginator()), (Function<String, AlarmInfo>)
                                    originatorName -> {
                                        if (originatorName == null) {
                                            originatorName = "Deleted";
                                        }
                                        alarmInfo.setOriginatorName(originatorName);
                                        return alarmInfo;
                                    }
                    ));
                }
                return Futures.successfulAsList(alarmFutures);
            });
        }
        return Futures.transform(alarms, new Function<List<AlarmInfo>, TimePageData<AlarmInfo>>() {
            @Nullable
            @Override
            public TimePageData<AlarmInfo> apply(@Nullable List<AlarmInfo> alarms) {
                return new TimePageData<>(alarms, query.getPageLink());
            }
        });
    }

    @Override
    public AlarmSeverity findHighestAlarmSeverity(EntityId entityId, AlarmSearchStatus alarmSearchStatus,
                                                  AlarmStatus alarmStatus) {
        TimePageLink nextPageLink = new TimePageLink(100);
        boolean hasNext = true;
        AlarmSeverity highestSeverity = null;
        AlarmQuery query;
        while (hasNext && AlarmSeverity.CRITICAL != highestSeverity) {
            query = new AlarmQuery(entityId, nextPageLink, alarmSearchStatus, alarmStatus, false);
            List<AlarmInfo> alarms;
            try {
                alarms = alarmDao.findAlarms(query).get();
            } catch (ExecutionException | InterruptedException e) {
                log.warn("Failed to find highest alarm severity. EntityId: [{}], AlarmSearchStatus: [{}], AlarmStatus: [{}]",
                        entityId, alarmSearchStatus, alarmStatus);
                throw new RuntimeException(e);
            }
            hasNext = alarms.size() == nextPageLink.getLimit();
            if (hasNext) {
                nextPageLink = new TimePageData<>(alarms, nextPageLink).getNextPageLink();
            }
            AlarmSeverity severity = detectHighestSeverity(alarms);
            if (severity == null) {
                continue;
            }
            if (severity == AlarmSeverity.CRITICAL || highestSeverity == null) {
                highestSeverity = severity;
            } else {
                highestSeverity = highestSeverity.compareTo(severity) < 0 ? highestSeverity : severity;
            }
        }
        return highestSeverity;
    }

    private AlarmSeverity detectHighestSeverity(List<AlarmInfo> alarms) {
        if (!alarms.isEmpty()) {
            List<AlarmInfo> sorted = new ArrayList(alarms);
            sorted.sort((p1, p2) -> p1.getSeverity().compareTo(p2.getSeverity()));
            return sorted.get(0).getSeverity();
        } else {
            return null;
        }
    }

    private void deleteRelation(EntityRelation alarmRelation) throws ExecutionException, InterruptedException {
        log.debug("Deleting Alarm relation: {}", alarmRelation);
        relationService.deleteRelationAsync(alarmRelation).get();
    }

    private void createRelation(EntityRelation alarmRelation) throws ExecutionException, InterruptedException {
        log.debug("Creating Alarm relation: {}", alarmRelation);
        relationService.saveRelationAsync(alarmRelation).get();
    }

    private Alarm merge(Alarm existing, Alarm alarm) {
        if (alarm.getStartTs() > existing.getEndTs()) {
            existing.setEndTs(alarm.getStartTs());
        }
        if (alarm.getEndTs() > existing.getEndTs()) {
            existing.setEndTs(alarm.getEndTs());
        }
        if (alarm.getClearTs() > existing.getClearTs()) {
            existing.setClearTs(alarm.getClearTs());
        }
        if (alarm.getAckTs() > existing.getAckTs()) {
            existing.setAckTs(alarm.getAckTs());
        }
        existing.setStatus(alarm.getStatus());
        existing.setSeverity(alarm.getSeverity());
        existing.setDetails(alarm.getDetails());
        existing.setPropagate(existing.isPropagate() || alarm.isPropagate());
        return existing;
    }

    private void updateRelations(Alarm alarm, AlarmStatus oldStatus, AlarmStatus newStatus) {
        try {
            List<EntityRelation> relations = relationService.findByToAsync(alarm.getId(), RelationTypeGroup.ALARM).get();
            Set<EntityId> parents = relations.stream().map(EntityRelation::getFrom).collect(Collectors.toSet());
            for (EntityId parentId : parents) {
                updateAlarmRelation(parentId, alarm.getId(), oldStatus, newStatus);
            }
        } catch (ExecutionException | InterruptedException e) {
            log.warn("[{}] Failed to update relations. Old status: [{}], New status: [{}]", alarm.getId(), oldStatus, newStatus);
            throw new RuntimeException(e);
        }
    }

    private void createAlarmRelation(EntityId entityId, EntityId alarmId, AlarmStatus status, boolean createAnyRelation) {
        try {
            if (createAnyRelation) {
                createRelation(new EntityRelation(entityId, alarmId, ALARM_RELATION_PREFIX + AlarmSearchStatus.ANY.name(), RelationTypeGroup.ALARM));
            }
            createRelation(new EntityRelation(entityId, alarmId, ALARM_RELATION_PREFIX + status.name(), RelationTypeGroup.ALARM));
            createRelation(new EntityRelation(entityId, alarmId, ALARM_RELATION_PREFIX + status.getClearSearchStatus().name(), RelationTypeGroup.ALARM));
            createRelation(new EntityRelation(entityId, alarmId, ALARM_RELATION_PREFIX + status.getAckSearchStatus().name(), RelationTypeGroup.ALARM));
        } catch (ExecutionException | InterruptedException e) {
            log.warn("[{}] Failed to create relation. Status: [{}]", alarmId, status);
            throw new RuntimeException(e);
        }
    }

    private void deleteAlarmRelation(EntityId entityId, EntityId alarmId, AlarmStatus status) {
        try {
            deleteRelation(new EntityRelation(entityId, alarmId, ALARM_RELATION_PREFIX + status.name(), RelationTypeGroup.ALARM));
            deleteRelation(new EntityRelation(entityId, alarmId, ALARM_RELATION_PREFIX + status.getClearSearchStatus().name(), RelationTypeGroup.ALARM));
            deleteRelation(new EntityRelation(entityId, alarmId, ALARM_RELATION_PREFIX + status.getAckSearchStatus().name(), RelationTypeGroup.ALARM));
        } catch (ExecutionException | InterruptedException e) {
            log.warn("[{}] Failed to delete relation. Status: [{}]", alarmId, status);
            throw new RuntimeException(e);
        }
    }

    private void updateAlarmRelation(EntityId entityId, EntityId alarmId, AlarmStatus oldStatus, AlarmStatus newStatus) {
        deleteAlarmRelation(entityId, alarmId, oldStatus);
        createAlarmRelation(entityId, alarmId, newStatus, false);
    }

    private <T> ListenableFuture<T> getAndUpdate(AlarmId alarmId, Function<Alarm, T> function) {
        validateId(alarmId, "Alarm id should be specified!");
        ListenableFuture<Alarm> entity = alarmDao.findAlarmByIdAsync(alarmId.getId());
        return Futures.transform(entity, function, readResultsProcessingExecutor);
    }

    private DataValidator<Alarm> alarmDataValidator =
            new DataValidator<Alarm>() {

                @Override
                protected void validateDataImpl(Alarm alarm) {
                    if (StringUtils.isEmpty(alarm.getType())) {
                        throw new DataValidationException("Alarm type should be specified!");
                    }
                    if (alarm.getOriginator() == null) {
                        throw new DataValidationException("Alarm originator should be specified!");
                    }
                    if (alarm.getSeverity() == null) {
                        throw new DataValidationException("Alarm severity should be specified!");
                    }
                    if (alarm.getStatus() == null) {
                        throw new DataValidationException("Alarm status should be specified!");
                    }
                    if (alarm.getTenantId() == null) {
                        throw new DataValidationException("Alarm should be assigned to tenant!");
                    } else {
                        Tenant tenant = tenantDao.findById(alarm.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("Alarm is referencing to non-existent tenant!");
                        }
                    }
                }
            };
}
