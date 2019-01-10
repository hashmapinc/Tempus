package com.hashmapinc.server.service.computation;

import com.hashmapinc.server.common.data.computation.Computations;
import com.hashmapinc.server.common.data.id.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface ComputationsFunctionService {
    Computations add(Computations computation, TenantId tenantId) throws Exception;
    Computations delete(Computations computations) throws Exception;
}
