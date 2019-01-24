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
package com.hashmapinc.server.service;

import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.tenant.TenantService;

public class CloudStorageServiceUtils {

    private static final String FILE_URL_FORMAT = "/%s/%s";
    private static final String FOLDER_URL_FORMAT = "/%s";

    public static String createBucketName(TenantId tenantId, TenantService tenantService) {
        Tenant tenant = tenantService.findTenantById(tenantId);
        return tenant.getName().replace(" ", "-");
    }

    public static String createObjectUrl(String fileName, String type) {
        if (fileName.contentEquals(""))
            return String.format(FOLDER_URL_FORMAT, type);
        return String.format(FILE_URL_FORMAT, type, fileName.replace(" ", "-"));
    }

}
