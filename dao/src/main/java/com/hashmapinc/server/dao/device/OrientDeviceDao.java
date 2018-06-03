/**
 * Copyright © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.dao.device;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.google.common.util.concurrent.ListenableFuture;

import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.EntitySubtype;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.util.OrientDao;

@Component
@Slf4j
@OrientDao
public class OrientDeviceDao implements DeviceDao {

  @Override
  public Device save(Device domain) {
    return domain;
  }

  @Override
  public List<Device> findDevicesByTenantId(UUID tenantId, TextPageLink pageLink) {
    return null;
  }

  @Override
  public List<Device> findDevicesByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
    return null;
  }

  @Override
  public ListenableFuture<List<Device>> findDevicesByTenantIdAndIdsAsync(UUID tenantId, List<UUID> deviceIds) {
    log.debug("Try to find devices by tenantId [{}] and device Ids [{}]", tenantId, deviceIds);
    return null;
  }

  @Override
  public List<Device> findDevicesByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
    log.debug("Try to find devices by tenantId [{}], customerId[{}] and pageLink [{}]", tenantId, customerId, pageLink);
    return null;
  }

  @Override
  public List<Device> findDevicesByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type,
      TextPageLink pageLink) {
    log.debug("Try to find devices by tenantId [{}], customerId [{}], type [{}] and pageLink [{}]", tenantId,
        customerId, type, pageLink);
    return null;
  }

  @Override
  public ListenableFuture<List<Device>> findDevicesByTenantIdCustomerIdAndIdsAsync(UUID tenantId, UUID customerId,
      List<UUID> deviceIds) {
    log.debug("Try to find devices by tenantId [{}], customerId [{}] and device Ids [{}]", tenantId, customerId,
        deviceIds);
    return null;
  }

  @Override
  public Optional<Device> findDeviceByTenantIdAndName(UUID tenantId, String deviceName) {
    return null;
  }

  @Override
  public ListenableFuture<List<EntitySubtype>> findTenantDeviceTypesAsync(UUID tenantId) {
    return null;
  }

  @Override
  public List<Device> find() {
    return null;
  }

  @Override
  public Device findById(UUID id) {
    return null;
  }

  @Override
  public ListenableFuture<Device> findByIdAsync(UUID id) {
    return null;
  }

  @Override
  public boolean removeById(UUID id) {
    return true;
  }
}
