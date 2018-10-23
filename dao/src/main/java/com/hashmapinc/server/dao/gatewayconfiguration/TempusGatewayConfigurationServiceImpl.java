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
package com.hashmapinc.server.dao.gatewayconfiguration;

import com.google.common.util.concurrent.ListenableFuture;
import com.hashmapinc.server.common.data.TempusGatewayConfiguration;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.TempusGatewayConfigurationId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import com.hashmapinc.server.dao.exception.DataValidationException;
import com.hashmapinc.server.dao.exception.IncorrectParameterException;
import com.hashmapinc.server.dao.service.DataValidator;
import com.hashmapinc.server.dao.service.Validator;
import com.hashmapinc.server.dao.tenant.TenantDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.hashmapinc.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class TempusGatewayConfigurationServiceImpl extends AbstractEntityService implements TempusGatewayConfigurationService {
    private static final String PUBLIC_CUSTOMER_TITLE = "Public";
    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_TEMPUS_GATEWAY_CONFIGURATION_ID = "Incorrect tempus gateway configuration id ";

    @Autowired
    TempusGatewayConfigurationDao tempusGatewayConfigurationDao;

    @Autowired
    TenantDao tenantDao;

    @Override
    public TempusGatewayConfiguration findTempusGatewayConfigurationById(TempusGatewayConfigurationId tempusGatewayConfigurationId) {
        log.trace("Executing findCustomerById [{}]", tempusGatewayConfigurationId);
        Validator.validateId(tempusGatewayConfigurationId, INCORRECT_TEMPUS_GATEWAY_CONFIGURATION_ID + tempusGatewayConfigurationId);
        return tempusGatewayConfigurationDao.findById(tempusGatewayConfigurationId.getId());
    }

    @Override
    public TempusGatewayConfiguration saveTempusGatewayConfiguration(TempusGatewayConfiguration tempusGatewayConfiguration) {
        log.trace("Executing saveCustomer [{}]", tempusGatewayConfiguration);
        tempusGatewayConfigurationValidator.validate(tempusGatewayConfiguration);
        return tempusGatewayConfigurationDao.save(tempusGatewayConfiguration);
    }

    @Override
    public void deleteTempusGatewayConfiguration(TempusGatewayConfigurationId tempusGatewayConfigurationId) {
        log.trace("Executing deleteTempusGatewayConfiguration [{}]", tempusGatewayConfigurationId);
        Validator.validateId(tempusGatewayConfigurationId, INCORRECT_TEMPUS_GATEWAY_CONFIGURATION_ID + tempusGatewayConfigurationId);
        TempusGatewayConfiguration tempusGatewayConfiguration = findTempusGatewayConfigurationById(tempusGatewayConfigurationId);
        if (tempusGatewayConfiguration == null) {
            throw new IncorrectParameterException("Unable to delete non-existent tempusGatewayConfiguration.");
        }
        deleteEntityRelations(tempusGatewayConfigurationId);
        tempusGatewayConfigurationDao.removeById(tempusGatewayConfigurationId.getId());

    }

    @Override
    public Optional<TempusGatewayConfiguration> findTempusGatewayConfigurationByTenantId(TenantId tenantId) {
        log.trace("Executing findTempusGatewayConfigurationByTenantId [{}]", tenantId);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        return tempusGatewayConfigurationDao.findTempusGatewayConfigurationByTenantId(tenantId.getId());
    }

    private DataValidator<TempusGatewayConfiguration> tempusGatewayConfigurationValidator =
            new DataValidator<TempusGatewayConfiguration>() {

                @Override
                protected void validateCreate(TempusGatewayConfiguration tempusGatewayConfiguration) {
                    tempusGatewayConfigurationDao.findTempusGatewayConfigurationByTenantId(tempusGatewayConfiguration.getTenantId().getId()).ifPresent(
                            c -> {
                                throw new DataValidationException("TempusGatewayConfiguration for this tenant already exists!");
                            }
                    );
                }

                @Override
                protected void validateUpdate(TempusGatewayConfiguration tempusGatewayConfiguration) {
                    if(!tempusGatewayConfigurationDao.findTempusGatewayConfigurationByTenantId(tempusGatewayConfiguration.getTenantId().getId()).isPresent()){
                        throw new DataValidationException("TempusGatewayConfiguration for this tenant does not exists!");
                    }
                }

                @Override
                protected void validateDataImpl(TempusGatewayConfiguration tempusGatewayConfiguration) {
                    if (tempusGatewayConfiguration.getTenantId() == null) {
                        throw new DataValidationException("TempusGatewayConfiguration should be assigned to tenant!");
                    } else {
                        Tenant tenant = tenantDao.findById(tempusGatewayConfiguration.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("TempusGatewayConfiguration is referencing to non-existent tenant!");
                        }
                    }
                }
            };


}
