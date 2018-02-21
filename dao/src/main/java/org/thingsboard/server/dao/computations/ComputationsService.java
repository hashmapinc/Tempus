package org.thingsboard.server.dao.computations;
import org.thingsboard.server.common.data.computation.Computations;
import org.thingsboard.server.common.data.id.ComputationId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;

import java.util.List;
import java.util.Optional;

public interface ComputationsService {

    Computations findByName(String name);

    Optional<Computations> findByTenantIdAndName(TenantId tenantId, String name);

    Computations findById(ComputationId id);

    Computations save(Computations computations);

    void deleteById(ComputationId computationId);

    void deleteByJarName(String name);

    TextPageData<Computations> findTenantComputations(TenantId tenantId, TextPageLink pageLink);

    List<Computations> findAllTenantComputationsByTenantId(TenantId tenantId);

}
