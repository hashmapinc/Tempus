package org.thingsboard.server.dao.computations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.entity.AbstractEntityService;
import org.thingsboard.server.dao.model.sql.ComputationsEntity;

import java.util.List;

import static org.thingsboard.server.dao.service.Validator.validateId;
import static org.thingsboard.server.dao.service.Validator.validatePageLink;

@Service
@Slf4j
public class BaseComputationsService extends AbstractEntityService implements ComputationsService {

    @Autowired
    ComputationsDao computationsDao;

    @Override
    public List<Computations> findAll() {
        return computationsDao.findAll();
    }

    @Override
    public Computations findByName(String name) {
        Computations computations = computationsDao.findByName(name);
        return computations;
    }

    @Override
    public Computations findById(ComputationId id) {
        return computationsDao.findById(id.getId());
    }

    @Override
    public void save(Computations computations) {
        ComputationsEntity computationsEntity = new ComputationsEntity(computations);
        computationsDao.save(computationsEntity);
    }

    @Override
    public void deleteByJarName(String name) {
        computationsDao.deleteByJarName(name);
    }

    @Override
    public TextPageData<Computations> findTenantComputations(TenantId tenantId, TextPageLink pageLink) {
        validateId(tenantId, "Incorrect tenant id for search computation request.");
        validatePageLink(pageLink, "Incorrect PageLink object for search computation request.");
        List<Computations> computations = computationsDao.findByTenantIdAndPageLink(tenantId, pageLink);
        return new TextPageData<>(computations, pageLink);
    }
}
