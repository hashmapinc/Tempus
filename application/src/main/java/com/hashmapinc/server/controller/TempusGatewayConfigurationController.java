/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hashmapinc.server.controller;

import com.hashmapinc.server.common.data.TempusGatewayConfiguration;
import com.hashmapinc.server.common.data.id.TempusGatewayConfigurationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.kubernetes.ReplicaSetStatus;
import com.hashmapinc.server.common.data.exception.TempusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/configuration/")
@Slf4j
public class TempusGatewayConfigurationController extends BaseController {
    public static final String TEMPUS_GATEWAY_CONFIGURATION_ID = "tempusGatewayConfigurationId";

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @GetMapping(value = "tempusGateway/{tempusGatewayConfigurationId}")
    @ResponseBody
    public TempusGatewayConfiguration getTempusGatewayConfigurationById(@PathVariable(TEMPUS_GATEWAY_CONFIGURATION_ID) String strTempusGatewayConfigurationId)
            throws TempusException {
        checkParameter(TEMPUS_GATEWAY_CONFIGURATION_ID, strTempusGatewayConfigurationId);
        try {
            TempusGatewayConfigurationId tempusGatewayConfigurationId = new TempusGatewayConfigurationId(toUUID(strTempusGatewayConfigurationId));
            return checkTempusGatewayConfigurationId(tempusGatewayConfigurationId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/tempusGateway")
    @ResponseBody
    public TempusGatewayConfiguration saveTempusGatewayConfiguration(@RequestBody TempusGatewayConfiguration tempusGatewayConfiguration) throws TempusException {
        try {
            tempusGatewayConfiguration.setTenantId(getCurrentUser().getTenantId());
            TempusGatewayConfiguration savedTempusGatewayConfiguration =
                    checkNotNull(tempusGatewayConfigurationService.saveTempusGatewayConfiguration(tempusGatewayConfiguration));

            log.debug("savedTempusGatewayConfiguration : [{}]", savedTempusGatewayConfiguration);
            return savedTempusGatewayConfiguration;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/tempusGateway")
    @ResponseBody
    public TempusGatewayConfiguration getTempusGatewayConfiguration() throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return tempusGatewayConfigurationService.findTempusGatewayConfigurationByTenantId(tenantId).orElse(new TempusGatewayConfiguration());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/tempusGateway/deploy")
    @ResponseBody
    public Boolean deployTempusGateway() throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            final Optional<TempusGatewayConfiguration> tempusGatewayConfigurationByTenantId =
                    tempusGatewayConfigurationService.findTempusGatewayConfigurationByTenantId(tenantId);

            if(!tempusGatewayConfigurationByTenantId.isPresent()){
                return false;
            }
            tempusGatewayConfigurationByTenantId.ifPresent(tempusGatewayConfiguration ->
                    tempusGatewayKubernetesService.deployTempusGateway(tempusGatewayConfiguration));
            return true;
        } catch (Exception e) {
            log.debug("Exception [{}]", e);
            return false;
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/tempusGateway/status")
    @ResponseBody
    public ReplicaSetStatus getTempusGatewayPodsStatus() throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            final Optional<TempusGatewayConfiguration> tempusGatewayConfiguration =
                    tempusGatewayConfigurationService.findTempusGatewayConfigurationByTenantId(tenantId);

            if(!tempusGatewayConfiguration.isPresent()){
                return null;
            }
            return tempusGatewayKubernetesService.getTempusGatewayReplicaSetStatus(tenantId);

        } catch (Exception e) {
            log.debug("Exception [{}]", e);
            return null;
        }
    }
}
