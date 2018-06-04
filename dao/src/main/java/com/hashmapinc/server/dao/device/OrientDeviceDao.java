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
import org.springframework.beans.factory.annotation.Value;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.OVertex;

import com.hashmapinc.server.common.data.Device;
import com.hashmapinc.server.common.data.EntitySubtype;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.dao.util.OrientDao;

@Component
@Slf4j
@OrientDao
public class OrientDeviceDao implements DeviceDao {
  // define globals
  @Value("${orientdb.db_name}")
  private String orient_db_name;

  @Value("${orientdb.url}")
  private String orient_db_url;

  @Value("${orientdb.username}")
  private String orient_db_username;

  @Value("${orientdb.password}")
  private String orient_db_password;

  // define orient connection (use a factory in production)
  OrientDB orient = new OrientDB("remote:" + orient_db_url, OrientDBConfig.defaultConfig());
  ODatabaseSession db = orient.open(orient_db_name, orient_db_username, orient_db_password);

  /**
   * retrieves the device class from the database.
   * 
   * @return device - OClass for devices
   */
  private OClass getDeviceClass() {
    // look for "Device" class
    OClass device = db.getClass("Device"); 

    // if device isn't found, define it here
    if (device == null) { 
      // create device
      device = db.createVertexClass("Device");  
      // create some device fields
      device.createProperty("name", OType.STRING);
      device.createProperty("tenantId", OType.STRING); // Orient does not support UUID 128-bit numbers
      device.createProperty("deviceId", OType.STRING); // Orient does not support UUID 128-bit numbers
      device.createProperty("searchText", OType.STRING);
      device.createProperty("type", OType.STRING);
    }

    return device;
  }

  /**
   * Save or update device object
   *
   * @param device the device object
   * @return saved device object
   */
  @Override
  public Device save(
    Device device
  ) {
    log.debug("Try to save device [{}]", device);

    // create vertex
    OClass devClass = getDeviceClass();
    OVertex devVertex = db.newVertex("Device");
    devVertex.setProperty("name", device.getName());
    devVertex.setProperty("tenantId", device.getTenantId());
    devVertex.setProperty("deviceId", device.getId());
    devVertex.setProperty("searchText", device.getSearchText());
    devVertex.setProperty("type", device.getType());

    // save vertex
    devVertex.save();

    return device;
  }

  /**
   * Find devices by tenantId and page link.
   *
   * @param tenantId the tenantId
   * @param pageLink the page link
   * @return the list of device objects
   */
  @Override
  public List<Device> findDevicesByTenantId(
    UUID tenantId, 
    TextPageLink pageLink
  ) {
    log.debug(
      "Try to findDevicesByTenantId with tenantId = [{}] and pageLink = [{}]", 
      tenantId, 
      pageLink
    );
    return null;
  }

  /**
   * Find devices by tenantId, type and page link.
   *
   * @param tenantId the tenantId
   * @param type     the type
   * @param pageLink the page link
   * @return the list of device objects
   */
  @Override
  public List<Device> findDevicesByTenantIdAndType(
    UUID tenantId, 
    String type, 
    TextPageLink pageLink
  ) {
    log.debug(
      "Try to findDevicesByTenantIdAndType with tenantId = [{}] and type = [{}] and pageLink = [{}]", 
      tenantId, 
      type, 
      pageLink
    );
    return null;
  }

  /**
   * Find devices by tenantId and devices Ids.
   *
   * @param tenantId  the tenantId
   * @param deviceIds the device Ids
   * @return the list of device objects
   */
  @Override
  public ListenableFuture<List<Device>> findDevicesByTenantIdAndIdsAsync(
    UUID tenantId, 
    List<UUID> deviceIds
  ) {
    log.debug(
      "Try to findDevicesByTenantIdAndIdsAsync with tenantId = [{}] and deviceIds = [{}]", 
      tenantId, 
      deviceIds
    );
    return null;
  }

  /**
   * Find devices by tenantId, customerId and page link.
   *
   * @param tenantId   the tenantId
   * @param customerId the customerId
   * @param pageLink   the page link
   * @return the list of device objects
   */
  @Override
  public List<Device> findDevicesByTenantIdAndCustomerId(
    UUID tenantId, 
    UUID customerId, 
    TextPageLink pageLink
  ) {
    log.debug(
      "Try to findDevicesByTenantIdAndCustomerId with tenantId = [{}] and customerId = [{}] and pageLink = [{}]", 
      tenantId, 
      customerId, 
      pageLink
    );
    return null;
  }

  /**
   * Find devices by tenantId, customerId, type and page link.
   *
   * @param tenantId   the tenantId
   * @param customerId the customerId
   * @param type       the type
   * @param pageLink   the page link
   * @return the list of device objects
   */
  @Override
  public List<Device> findDevicesByTenantIdAndCustomerIdAndType(
    UUID tenantId, 
    UUID customerId, 
    String type,
    TextPageLink pageLink
  ) {
    log.debug(
      "Try to findDevicesByTenantIdAndCustomerIdAndType with tenantId = [{}], customerId = [{}], type = [{}] and pageLink = [{}]", 
      tenantId,
      customerId, 
      type, 
      pageLink
    );
    return null;
  }

  /**
   * Find devices by tenantId, customerId and devices Ids.
   *
   * @param tenantId   the tenantId
   * @param customerId the customerId
   * @param deviceIds  the device Ids
   * @return the list of device objects
   */
  @Override
  public ListenableFuture<List<Device>> findDevicesByTenantIdCustomerIdAndIdsAsync(
    UUID tenantId, 
    UUID customerId,
    List<UUID> deviceIds
  ) {
    log.debug(
      "Try to findDevicesByTenantIdCustomerIdAndIdsAsync with tenantId = [{}] and customerId = [{}] and deviceIds = [{}]", 
      tenantId, 
      customerId,
      deviceIds
    );
    return null;
  }

  /**
   * Find devices by tenantId and device name.
   *
   * @param tenantId   the tenantId
   * @param deviceName the device name
   * @return the optional device object
   */
  @Override
  public Optional<Device> findDeviceByTenantIdAndName(
    UUID tenantId, 
    String deviceName
  ) {
    log.debug(
      "Try to findDeviceByTenantIdAndName with tenantId = [{}] and deviceName = [{}]", 
      tenantId, 
      deviceName
    );
    return null;
  }

  /**
   * Find tenants device types.
   *
   * @param tenantId the tenantId
   * @return the list of tenant device type objects
   */
  @Override
  public ListenableFuture<List<EntitySubtype>> findTenantDeviceTypesAsync(
    UUID tenantId
  ) {
    log.debug(
      "Try to findTenantDeviceTypesAsync with tenantId = [{}]", 
      tenantId
    );
    return null;
  }

  /**
   * Find all devices.
   *
   * @return the list of all devices
   */
  @Override
  public List<Device> find() {
    log.debug("Try to find all devices");
    return null;
  }

  /**
   * Find a single device by its UUID.
   *
   * @param id the UUID of the device to find
   * @return the device object found
   */
  @Override
  public Device findById(
    UUID id
  ) {
    log.debug(
      "Try to findById with id = [{}]",
      id
    );
    
    return null;
  }

  /**
   * Find a single device by its UUID.
   *
   * @param id the UUID of the device to find
   * @return the device object found
   */
  @Override
  public ListenableFuture<Device> findByIdAsync(
    UUID id
  ) {
    log.debug(
      "Try to findByIdAsync with id = [{}]", 
      id
    );
    return null;
  }

  /**
   * Remove a single device by its UUID.
   *
   * @param id the UUID of the device to remove
   * @return the status of the removal (true = success, false = fail)
   */
  @Override
  public boolean removeById(
    UUID id
  ) {
    log.debug(
      "Try to removeById with id = [{}]", 
      id
    );
    return true;
  }
}
