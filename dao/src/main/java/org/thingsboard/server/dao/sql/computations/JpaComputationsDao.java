package org.thingsboard.server.dao.sql.computations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.Computations;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.computations.ComputationsDao;
import org.thingsboard.server.dao.model.sql.ComputationsEntity;
import org.thingsboard.server.dao.sql.JpaAbstractDaoListeningExecutorService;

import java.util.List;

@Service
public class JpaComputationsDao extends JpaAbstractDaoListeningExecutorService implements ComputationsDao {

    @Autowired
    ComputationsRepository computationsRepository;

    @Override
    public List<Computations> findAll() {
        Iterable <ComputationsEntity> computationsEntities = computationsRepository.findAll();
        List<ComputationsEntity> computationsEntityList = DaoUtil.toList(computationsEntities);
        List<Computations> computationsList = DaoUtil.convertDataList(computationsEntityList);
        return computationsList;
    }

    @Override
    public Computations findByName(String name) {
        ComputationsEntity computationsEntity = computationsRepository.findByName(name);
        return DaoUtil.getData(computationsEntity);
    }

    @Override
    public void save(ComputationsEntity computationsEntity) {
        computationsRepository.save(computationsEntity);
    }

    @Override
    public void deleteByName(String name) {
        computationsRepository.deleteByName(name);
    }

}
