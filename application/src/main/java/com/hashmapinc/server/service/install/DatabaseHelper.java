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
package com.hashmapinc.server.service.install;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import com.hashmapinc.server.common.data.ShortCustomerInfo;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.DashboardId;
import com.hashmapinc.server.dao.dashboard.DashboardService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by igor on 2/27/18.
 */
@Slf4j
public class DatabaseHelper {

    private DatabaseHelper() {
    }

    public static final CSVFormat CSV_DUMP_FORMAT = CSVFormat.DEFAULT.withNullString("\\N");

    public static final String DEVICE = "device";
    public static final String TENANT_ID = "tenant_id";
    public static final String CUSTOMER_ID = "customer_id";
    public static final String SEARCH_TEXT = "search_text";
    public static final String ADDITIONAL_INFO = "additional_info";
    public static final String ASSET = "asset";
    public static final String DASHBOARD = "dashboard";
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String ASSIGNED_CUSTOMERS = "assigned_customers";
    public static final String CONFIGURATION = "configuration";

    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static void upgradeTo40AssignDashboards(Path dashboardsDump, DashboardService dashboardService, boolean sql) throws IOException {
        JavaType assignedCustomersType =
                objectMapper.getTypeFactory().constructCollectionType(HashSet.class, ShortCustomerInfo.class);
        try (CSVParser csvParser = new CSVParser(Files.newBufferedReader(dashboardsDump), CSV_DUMP_FORMAT.withFirstRecordAsHeader())) {
            csvParser.forEach(record -> processRecord(dashboardService, sql, assignedCustomersType, record));
        }
    }

    private static void processRecord(DashboardService dashboardService, boolean sql, JavaType assignedCustomersType, CSVRecord record) {
        String customerIdString = record.get(CUSTOMER_ID);
        String assignedCustomersString = record.get(ASSIGNED_CUSTOMERS);
        DashboardId dashboardId = new DashboardId(toUUID(record.get(ID), sql));
        List<CustomerId> customerIds = new ArrayList<>();
        if (!StringUtils.isEmpty(assignedCustomersString)) {
            try {
                Set<ShortCustomerInfo> assignedCustomers = objectMapper.readValue(assignedCustomersString, assignedCustomersType);
                assignedCustomers.forEach(customerInfo -> {
                    CustomerId customerId = customerInfo.getCustomerId();
                    if (!customerId.isNullUid()) {
                        customerIds.add(customerId);
                    }
                });
            } catch (IOException e) {
                log.error("Unable to parse assigned customers field", e);
            }
        }
        if (!StringUtils.isEmpty(customerIdString)) {
            CustomerId customerId = new CustomerId(toUUID(customerIdString, sql));
            if (!customerId.isNullUid()) {
                customerIds.add(customerId);
            }
        }
        for (CustomerId customerId : customerIds) {
            dashboardService.assignDashboardToCustomer(dashboardId, customerId);
        }
    }

    private static UUID toUUID(String src, boolean sql) {
        if (sql) {
            return UUIDConverter.fromString(src);
        } else {
            return UUID.fromString(src);
        }
    }

}
