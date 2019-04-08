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

import com.hashmapinc.server.common.data.audit.ActionType;
import com.hashmapinc.server.common.data.id.PluginId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.plugin.ComponentLifecycleEvent;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hashmapinc.server.common.data.EntityType;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.plugin.PluginMetaData;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.dao.model.ModelConstants;
import com.hashmapinc.server.common.data.exception.TempusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PluginController extends BaseController {

    public static final String PLUGIN_ID = "pluginId";

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping(value = "/plugin/{pluginId}")
    @ResponseBody
    public PluginMetaData getPluginById(@PathVariable(PLUGIN_ID) String strPluginId) throws TempusException {
        checkParameter(PLUGIN_ID, strPluginId);
        try {
            PluginId pluginId = new PluginId(toUUID(strPluginId));
            return checkPlugin(pluginDataEncoderService.encoder(pluginService.findPluginById(pluginId)));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping(value = "/plugin/token/{pluginToken}")
    @ResponseBody
    public PluginMetaData getPluginByToken(@PathVariable("pluginToken") String pluginToken) throws TempusException {
        checkParameter("pluginToken", pluginToken);
        try {
            return checkPlugin(pluginDataEncoderService.encoder(pluginService.findPluginByApiToken(pluginToken)));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @PostMapping(value = "/plugin")
    @ResponseBody
    public PluginMetaData savePlugin(@RequestBody PluginMetaData source) throws TempusException {
        try {
            boolean created = source.getId() == null;
            source.setTenantId(getCurrentUser().getTenantId());
            PluginMetaData plugin = checkNotNull(pluginService.savePlugin(source));
            actorService.onPluginStateChange(plugin.getTenantId(), plugin.getId(),
                    created ? ComponentLifecycleEvent.CREATED : ComponentLifecycleEvent.UPDATED);

            logEntityAction(plugin.getId(), plugin,
                    null,
                    created ? ActionType.ADDED : ActionType.UPDATED, null);

            return pluginDataEncoderService.encoder(plugin);
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.PLUGIN), source,
                    null, source.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @PostMapping(value = "/plugin/{pluginId}/activate")
    @ResponseStatus(value = HttpStatus.OK)
    public void activatePluginById(@PathVariable(PLUGIN_ID) String strPluginId) throws TempusException {
        checkParameter(PLUGIN_ID, strPluginId);
        try {
            PluginId pluginId = new PluginId(toUUID(strPluginId));
            PluginMetaData plugin = checkPlugin(pluginService.findPluginById(pluginId));
            pluginService.activatePluginById(pluginId);
            actorService.onPluginStateChange(plugin.getTenantId(), plugin.getId(), ComponentLifecycleEvent.ACTIVATED);

            logEntityAction(plugin.getId(), plugin,
                    null,
                    ActionType.ACTIVATED, null, strPluginId);

        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.PLUGIN),
                    null,
                    null,
                    ActionType.ACTIVATED, e, strPluginId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @PostMapping(value = "/plugin/{pluginId}/suspend")
    @ResponseStatus(value = HttpStatus.OK)
    public void suspendPluginById(@PathVariable(PLUGIN_ID) String strPluginId) throws TempusException {
        checkParameter(PLUGIN_ID, strPluginId);
        try {
            PluginId pluginId = new PluginId(toUUID(strPluginId));
            PluginMetaData plugin = checkPlugin(pluginService.findPluginById(pluginId));
            pluginService.suspendPluginById(pluginId);
            actorService.onPluginStateChange(plugin.getTenantId(), plugin.getId(), ComponentLifecycleEvent.SUSPENDED);

            logEntityAction(plugin.getId(), plugin,
                    null,
                    ActionType.SUSPENDED, null, strPluginId);

        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.PLUGIN),
                    null,
                    null,
                    ActionType.SUSPENDED, e, strPluginId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @GetMapping(value = "/plugin/system", params = {"limit"})
    @ResponseBody
    public TextPageData<PluginMetaData> getSystemPlugins(
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
        try {
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            TextPageData<PluginMetaData> pluginMetaDataTextPageData =  checkNotNull(pluginService.findSystemPlugins(pageLink));
            List<PluginMetaData> pluginMetaData = pluginMetaDataTextPageData.getData().stream().map(pluginDataEncoderService::encoder).collect(Collectors.toList());
            return new TextPageData<>(pluginMetaData,pageLink);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @GetMapping(value = "/plugin/tenant/{tenantId}", params = {"limit"})
    @ResponseBody
    public TextPageData<PluginMetaData> getTenantPlugins(
            @PathVariable("tenantId") String strTenantId,
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
        checkParameter("tenantId", strTenantId);
        try {
            TenantId tenantId = new TenantId(toUUID(strTenantId));
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            TextPageData<PluginMetaData> pluginMetaDataTextPageData = checkNotNull(pluginService.findTenantPlugins(tenantId, pageLink));
            List<PluginMetaData> pluginMetaData = pluginMetaDataTextPageData.getData().stream().map(pluginDataEncoderService::encoder).collect(Collectors.toList());
            return new TextPageData<>(pluginMetaData,pageLink);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping(value = "/plugins")
    @ResponseBody
    public List<PluginMetaData> getPlugins() throws TempusException {
        try {
            if (getCurrentUser().getAuthority() == Authority.SYS_ADMIN) {
                return checkNotNull(pluginService.findSystemPlugins().stream().map(pluginDataEncoderService::encoder).collect(Collectors.toList()));
            } else {
                TenantId tenantId = getCurrentUser().getTenantId();
                List<PluginMetaData> plugins = checkNotNull(pluginService.findAllTenantPluginsByTenantId(tenantId).stream().map(pluginDataEncoderService::encoder).collect(Collectors.toList()));
                plugins.stream()
                        .filter(plugin -> plugin.getTenantId().getId().equals(ModelConstants.NULL_UUID))
                        .forEach(plugin -> plugin.setConfiguration(null));
                return plugins;
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/plugin", params = {"limit"})
    @ResponseBody
    public TextPageData<PluginMetaData> getTenantPlugins(
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws TempusException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            TextPageData<PluginMetaData> pluginMetaDataTextPageData =  checkNotNull(pluginService.findTenantPlugins(tenantId, pageLink));
            List<PluginMetaData> pluginMetaData = pluginMetaDataTextPageData.getData().stream().map(pluginDataEncoderService::encoder).collect(Collectors.toList());
            return new TextPageData<>(pluginMetaData,pageLink);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @DeleteMapping(value = "/plugin/{pluginId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deletePlugin(@PathVariable(PLUGIN_ID) String strPluginId) throws TempusException {
        checkParameter(PLUGIN_ID, strPluginId);
        try {
            PluginId pluginId = new PluginId(toUUID(strPluginId));
            PluginMetaData plugin = checkPlugin(pluginService.findPluginById(pluginId));
            pluginService.deletePluginById(pluginId);
            actorService.onPluginStateChange(plugin.getTenantId(), plugin.getId(), ComponentLifecycleEvent.DELETED);

            logEntityAction(pluginId, plugin,
                    null,
                    ActionType.DELETED, null, strPluginId);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.PLUGIN),
                    null,
                    null,
                    ActionType.DELETED, e, strPluginId);
            throw handleException(e);
        }
    }


}
