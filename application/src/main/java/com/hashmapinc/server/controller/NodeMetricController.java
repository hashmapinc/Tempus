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

import com.hashmapinc.server.common.data.cluster.NodeMetric;
import com.hashmapinc.server.exception.TempusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class NodeMetricController extends BaseController {

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/nodes", method = RequestMethod.GET)
    @ResponseBody
    public List<NodeMetric> getNodeMetric() throws TempusException {
        try {
            List<NodeMetric> nodeMetrics = checkNotNull(nodeMetricService.findAll());
            log.debug("NodeMetrics : [{}]", nodeMetrics);
            return nodeMetrics;
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
