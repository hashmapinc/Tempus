package com.hashmapinc.server.dao.computations;

import com.hashmapinc.server.common.data.page.TextPageLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.dao.entity.AbstractEntityService;

import java.util.List;
import java.util.Optional;

import static com.hashmapinc.server.dao.service.Validator.validateId;
import static com.hashmapinc.server.dao.service.Validator.validatePageLink;

@Service
@Slf4j
public class BaseComputationsService extends AbstractEntityService implements ComputationsService {

    @Autowired
    ComputationsDao computationsDao;

    @Override
    public Computations findByName(String name) {
        Computations computations = computationsDao.findByName(name);
        return computations;
    }

    @Override
    public Optional<Computations> findByTenantIdAndName(TenantId tenantId, String name) {
        return computationsDao.findByTenantIdAndName(tenantId, name);
    }

    @Override
    public Computations findById(ComputationId id) {
        return computationsDao.findById(id.getId());
    }

    @Override
    public Computations save(Computations computations) {
        return computationsDao.save(computations);
    }

    @Override
    public TextPageData<Computations> findTenantComputations(TenantId tenantId, TextPageLink pageLink) {
        validateId(tenantId, "Incorrect tenant id for search computation request.");
        validatePageLink(pageLink, "Incorrect PageLink object for search computation request.");
        List<Computations> computations = computationsDao.findByTenantIdAndPageLink(tenantId, pageLink);
        return new TextPageData<>(computations, pageLink);
    }

    @Override
    public void deleteById(ComputationId computationId) {
        computationsDao.deleteById(computationId);
    }

    @Override
    public List<Computations> findAllTenantComputationsByTenantId(TenantId tenantId) {
        return computationsDao.findByTenantId(tenantId);
    }


}
