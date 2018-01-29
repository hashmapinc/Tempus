package org.thingsboard.server.dao.computations;

import org.thingsboard.server.common.data.Computations;
import org.thingsboard.server.dao.model.sql.ComputationsEntity;

import java.util.List;

public interface ComputationsDao {
    List<Computations> findAll();

    Computations findByName(String name);

    void save(ComputationsEntity computationsEntity);

    void deleteByName(String name);
}
