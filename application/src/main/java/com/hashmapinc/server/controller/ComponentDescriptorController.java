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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hashmapinc.server.common.data.plugin.ComponentDescriptor;
import com.hashmapinc.server.common.data.plugin.ComponentType;
import com.hashmapinc.server.exception.TempusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ComponentDescriptorController extends BaseController {

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN')")
    @RequestMapping(value = "/component/{componentDescriptorClazz:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ComponentDescriptor getComponentDescriptorByClazz(@PathVariable("componentDescriptorClazz") String strComponentDescriptorClazz) throws TempusException {
        checkParameter("strComponentDescriptorClazz", strComponentDescriptorClazz);
        try {
            return checkComponentDescriptorByClazz(strComponentDescriptorClazz);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN')")
    @RequestMapping(value = "/components/{componentType}", method = RequestMethod.GET)
    @ResponseBody
    public List<ComponentDescriptor> getComponentDescriptorsByType(@PathVariable("componentType") String strComponentType) throws TempusException {
        checkParameter("componentType", strComponentType);
        try {
            return checkComponentDescriptorsByType(ComponentType.valueOf(strComponentType));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN')")
    @RequestMapping(value = "/components/actions/{pluginClazz:.+}", method = RequestMethod.GET)
    @ResponseBody
    public List<ComponentDescriptor> getPluginActionsByPluginClazz(@PathVariable("pluginClazz") String pluginClazz) throws TempusException {
        checkParameter("pluginClazz", pluginClazz);
        try {
            return checkPluginActionsByPluginClazz(pluginClazz);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
