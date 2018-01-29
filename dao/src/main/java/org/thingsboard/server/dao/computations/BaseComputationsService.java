package org.thingsboard.server.dao.computations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.Computations;
import org.thingsboard.server.dao.model.sql.ComputationsEntity;

import java.util.List;

@Service
@Slf4j
public class BaseComputationsService implements ComputationsService {

    @Autowired
    ComputationsDao computationsDao;

    @Override
    public List<Computations> findAll() {
        return computationsDao.findAll();
    }

    @Override
    public Computations findByName(String name) {
        return computationsDao.findByName(name);
    }

    @Override
    public void save(Computations computations) {
        ComputationsEntity computationsEntity = new ComputationsEntity(computations);
        computationsDao.save(computationsEntity);
    }

    @Override
    public void deleteByName(String name) {
        log.error("\nHMDC deleting jar \n");
        computationsDao.deleteByName(name);
    }
}
