package org.thingsboard.server.dao.computations;
import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;

import java.util.List;
import java.util.Optional;

public interface ComputationsService {

    List<Computations> findAll();

    Computations findByName(String name);

    void save(Computations computations);

    void deleteByJarName(String name);

    TextPageData<Computations> findTenantComputations(TenantId tenantId, TextPageLink pageLink);

}
