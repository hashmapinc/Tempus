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
package com.hashmapinc.server.dao.sql.audit;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hashmapinc.server.common.data.id.EntityId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.page.TimePageLink;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.audit.AuditLogDao;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.dao.model.sql.AuditLogEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDao;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.audit.AuditLog;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.dao.sql.JpaAbstractSearchTimeDao;

import javax.annotation.PreDestroy;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import static org.springframework.data.jpa.domain.Specifications.where;

@Component
@SqlDao
public class JpaAuditLogDao extends JpaAbstractDao<AuditLogEntity, AuditLog> implements AuditLogDao {

    private ListeningExecutorService insertService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    protected Class<AuditLogEntity> getEntityClass() {
        return AuditLogEntity.class;
    }

    @Override
    protected CrudRepository<AuditLogEntity, String> getCrudRepository() {
        return auditLogRepository;
    }

    @PreDestroy
    void onDestroy() {
        insertService.shutdown();
    }

    @Override
    public ListenableFuture<Void> saveByTenantId(AuditLog auditLog) {
        return insertService.submit(() -> {
            save(auditLog);
            return null;
        });
    }

    @Override
    public ListenableFuture<Void> saveByTenantIdAndEntityId(AuditLog auditLog) {
        return insertService.submit(() -> null);
    }

    @Override
    public ListenableFuture<Void> saveByTenantIdAndCustomerId(AuditLog auditLog) {
        return insertService.submit(() -> null);
    }

    @Override
    public ListenableFuture<Void> saveByTenantIdAndUserId(AuditLog auditLog) {
        return insertService.submit(() -> null);
    }

    @Override
    public ListenableFuture<Void> savePartitionsByTenantId(AuditLog auditLog) {
        return insertService.submit(() -> null);
    }

    @Override
    public List<AuditLog> findAuditLogsByTenantIdAndEntityId(UUID tenantId, EntityId entityId, TimePageLink pageLink) {
        return findAuditLogs(tenantId, entityId, null, null, pageLink);
    }

    @Override
    public List<AuditLog> findAuditLogsByTenantIdAndCustomerId(UUID tenantId, CustomerId customerId, TimePageLink pageLink) {
        return findAuditLogs(tenantId, null, customerId, null, pageLink);
    }

    @Override
    public List<AuditLog> findAuditLogsByTenantIdAndUserId(UUID tenantId, UserId userId, TimePageLink pageLink) {
        return findAuditLogs(tenantId, null, null, userId, pageLink);
    }

    @Override
    public List<AuditLog> findAuditLogsByTenantId(UUID tenantId, TimePageLink pageLink) {
        return findAuditLogs(tenantId, null, null, null, pageLink);
    }

    private List<AuditLog> findAuditLogs(UUID tenantId, EntityId entityId, CustomerId customerId, UserId userId, TimePageLink pageLink) {
        Specification<AuditLogEntity> timeSearchSpec = JpaAbstractSearchTimeDao.getTimeSearchPageSpec(pageLink, "id");
        Specification<AuditLogEntity> fieldsSpec = getEntityFieldsSpec(tenantId, entityId, customerId, userId);
        Sort.Direction sortDirection = pageLink.isAscOrder() ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = new PageRequest(0, pageLink.getLimit(), sortDirection, ModelConstants.ID_PROPERTY);
        return DaoUtil.convertDataList(auditLogRepository.findAll(where(timeSearchSpec).and(fieldsSpec), pageable).getContent());
    }

    private Specification<AuditLogEntity> getEntityFieldsSpec(UUID tenantId, EntityId entityId, CustomerId customerId, UserId userId) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tenantId != null) {
                Predicate tenantIdPredicate = criteriaBuilder.equal(root.get("tenantId"), UUIDConverter.fromTimeUUID(tenantId));
                predicates.add(tenantIdPredicate);
            }
            if (entityId != null) {
                Predicate entityTypePredicate = criteriaBuilder.equal(root.get("entityType"), entityId.getEntityType());
                predicates.add(entityTypePredicate);
                Predicate entityIdPredicate = criteriaBuilder.equal(root.get("entityId"), UUIDConverter.fromTimeUUID(entityId.getId()));
                predicates.add(entityIdPredicate);
            }
            if (customerId != null) {
                Predicate tenantIdPredicate = criteriaBuilder.equal(root.get("customerId"), UUIDConverter.fromTimeUUID(customerId.getId()));
                predicates.add(tenantIdPredicate);
            }
            if (userId != null) {
                Predicate tenantIdPredicate = criteriaBuilder.equal(root.get("userId"), UUIDConverter.fromTimeUUID(userId.getId()));
                predicates.add(tenantIdPredicate);
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
        };
    }
}
