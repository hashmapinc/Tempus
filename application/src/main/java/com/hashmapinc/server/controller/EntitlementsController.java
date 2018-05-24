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
package com.hashmapinc.server.controller;

import com.hashmapinc.server.common.data.Entitlements;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.exception.TempusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@Slf4j
public class EntitlementsController extends BaseController {

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/entitlements", method = RequestMethod.POST)
    @ResponseBody
    public Entitlements saveEntitlements(@RequestBody Entitlements entitlements) throws TempusException {
        Entitlements userEntitlements = checkNotNull(entitlementsService.save(entitlements));
        return userEntitlements;
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/user/{userId}/entitlements", method = RequestMethod.GET)
    @ResponseBody
    public Entitlements getUserEntitlements(@PathVariable("userId") String strUserId) throws TempusException {
        checkParameter("userId", strUserId);
        UserId userId = new UserId(toUUID(strUserId));
        Optional<Entitlements> entitlements = entitlementsService.findEntitlementsForUserId(userId);
        return entitlements.orElse(null);
    }

}
