package com.hashmapinc.server.dao.computations;
import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.ComputationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;

import java.util.List;
import java.util.Optional;

public interface ComputationsService {

    Computations findByName(String name);

    Optional<Computations> findByTenantIdAndName(TenantId tenantId, String name);

    Computations findById(ComputationId id);

    Computations save(Computations computations);

    void deleteById(ComputationId computationId);

    TextPageData<Computations> findTenantComputations(TenantId tenantId, TextPageLink pageLink);

    List<Computations> findAllTenantComputationsByTenantId(TenantId tenantId);

}
