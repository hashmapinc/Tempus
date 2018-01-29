package org.thingsboard.server.dao.computations;
import org.thingsboard.server.common.data.Computations;

import java.util.List;

public interface ComputationsService {

    List<Computations> findAll();

    Computations findByName(String name);

    void save(Computations computations);

    void deleteByName(String name);
}
