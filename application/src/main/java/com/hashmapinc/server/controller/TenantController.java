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

import com.hashmapinc.server.common.data.page.TextPageLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.dao.tenant.TenantService;
import com.hashmapinc.server.common.data.exception.TempusException;

@RestController
@RequestMapping("/api")
public class TenantController extends BaseController {
    
    @Autowired
    private TenantService tenantService;

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping(value = "/tenant/{tenantId}")
    @ResponseBody
    public Tenant getTenantById(@PathVariable("tenantId") String strTenantId) throws TempusException {
        checkParameter("tenantId", strTenantId);
        try {
            TenantId tenantId = new TenantId(toUUID(strTenantId));
            checkTenantId(tenantId);
            return checkNotNull(tenantService.findTenantById(tenantId));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @PostMapping(value = "/tenant")
    @ResponseBody 
    public Tenant saveTenant(@RequestBody Tenant tenant) throws TempusException {
        try {
            return checkNotNull(tenantService.saveTenant(tenant));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @DeleteMapping(value = "/tenant/{tenantId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTenant(@PathVariable("tenantId") String strTenantId) throws TempusException {
        checkParameter("tenantId", strTenantId);
        try {
            TenantId tenantId = new TenantId(toUUID(strTenantId));
            tenantService.deleteTenant(tenantId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @GetMapping(value = "/tenants", params = { "limit" })
    @ResponseBody
    public TextPageData<Tenant> getTenants(@RequestParam int limit,
                                           @RequestParam(required = false) String textSearch,
                                           @RequestParam(required = false) String idOffset,
                                           @RequestParam(required = false) String textOffset) throws TempusException {
        try {
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return checkNotNull(tenantService.findTenants(pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping(value = "/unit-system/tenant/{tenantId}")
    @ResponseBody
    public String getUserUnitSystem(@PathVariable("tenantId") String strUserId) throws TempusException {
        checkParameter("tenantId", strUserId);
        try {
            TenantId tenantId = new TenantId(toUUID(strUserId));
            checkTenantId(tenantId);
            return tenantService.findUnitSystemByTenantId(tenantId);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/unit-system/tenant/{tenantId}")
    @ResponseBody
    public void saveUserUnitSystem(@PathVariable("tenantId") String strUserId, @RequestBody String unitSystem) throws TempusException {
        checkParameter("tenantId", strUserId);
        try {
            TenantId tenantId = new TenantId(toUUID(strUserId));
            checkTenantId(tenantId);
            tenantService.saveUnitSystem(unitSystem, tenantId);

        } catch (Exception e) {
            throw handleException(e);
        }
    }
    
}
