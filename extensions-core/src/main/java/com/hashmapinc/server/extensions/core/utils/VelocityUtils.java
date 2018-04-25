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
package com.hashmapinc.server.extensions.core.utils;

import com.hashmapinc.server.common.data.kv.AttributeKvEntry;
import com.hashmapinc.server.common.data.kv.BasicDsKvEntry;
import com.hashmapinc.server.common.data.kv.StringDataEntry;
import com.hashmapinc.server.common.msg.core.DepthTelemetryUploadRequest;
import com.hashmapinc.server.common.msg.core.TelemetryUploadRequest;
import com.hashmapinc.server.common.msg.session.FromDeviceMsg;
import com.hashmapinc.server.extensions.api.device.DeviceAttributes;
import com.hashmapinc.server.extensions.api.rules.RuleProcessingMetaData;
import com.hashmapinc.server.extensions.core.filter.NashornJsEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.view.ViewContextTool;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hashmapinc.server.common.data.kv.BasicTsKvEntry;
import com.hashmapinc.server.extensions.api.device.DeviceMetaData;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Andrew Shvayka
 */
@Slf4j
public class VelocityUtils {

    private static Logger logger = LoggerFactory.getLogger(VelocityUtils.class);

    public static Template create(String source, String templateName) throws ParseException {
        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        StringReader reader = new StringReader(source);
        SimpleNode node = runtimeServices.parse(reader, templateName);
        Template template = new Template();
        template.setRuntimeServices(runtimeServices);
        template.setData(node);
        template.initDocument();
        return template;
    }

    public static String merge(Template template, VelocityContext context) {
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

    public static VelocityContext createContext(RuleProcessingMetaData metadata) {
        VelocityContext context = new VelocityContext();
        metadata.getValues().forEach((k, v) -> context.put(k, v));
        return context;
    }

    public static VelocityContext createContext(DeviceMetaData deviceMetaData, FromDeviceMsg payload) {
        VelocityContext context = new VelocityContext();
        context.put("date", new DateTool());
        DeviceAttributes deviceAttributes = deviceMetaData.getDeviceAttributes();

        pushAttributes(context, deviceAttributes.getClientSideAttributes(), NashornJsEvaluator.CLIENT_SIDE);
        pushAttributes(context, deviceAttributes.getServerSideAttributes(), NashornJsEvaluator.SERVER_SIDE);
        pushAttributes(context, deviceAttributes.getServerSidePublicAttributes(), NashornJsEvaluator.SHARED);

		switch (payload.getMsgType()) {
        case POST_TELEMETRY_REQUEST_DEPTH:
	            pushDsEntries(context, (DepthTelemetryUploadRequest) payload);
	            break;

        case POST_TELEMETRY_REQUEST:
                pushTsEntries(context, (TelemetryUploadRequest) payload);
                break;


            default:
                break;
        }

        context.put("deviceId", deviceMetaData.getDeviceId().getId().toString());
        context.put("deviceName", deviceMetaData.getDeviceName());
        context.put("deviceType", deviceMetaData.getDeviceType());

        return context;
    }

    private static ViewContextTool getContext(VelocityContext context) {
    	java.util.Map<String, Object> map=new java.util.HashMap<String, Object>();
    	String[] keys = (String[])context.internalGetKeys();
    	if (keys!=null) {
    		for (int i=0; i<keys.length; i++) {
    			map.put(keys[i], context.internalGet(keys[i]));
    		}
    	}
        org.apache.velocity.tools.generic.ValueParser vp=new org.apache.velocity.tools.generic.ValueParser(map);
        ViewContextTool vct = new ViewContextTool();
        vct.configure(vp);
        return vct;
    }

    private static ViewContextTool getContext(TelemetryUploadRequest payload) {
    	java.util.Map<String, Object> map=new java.util.HashMap<String, Object>();
        payload.getData().forEach((k, vList) -> {
            vList.forEach(v -> {
                //map.put(v.getKey(), new BasicTsKvEntry(k, v));
                map.put(v.getKey(), v);
            });
        });
        org.apache.velocity.tools.generic.ValueParser vp=new org.apache.velocity.tools.generic.ValueParser(map);
        ViewContextTool vct = new ViewContextTool();
        vct.configure(vp);
        return vct;
    }

    private static void pushDsEntries(VelocityContext context, DepthTelemetryUploadRequest payload) {
    	Set<BasicDsKvEntry> allKeys = new HashSet<>();
        payload.getData().forEach((k, vList) -> {
            allKeys.addAll(vList.stream().map(v -> {
                context.put(v.getKey(), new BasicDsKvEntry(k, v));
                return new BasicDsKvEntry(k, v);
            }).collect(Collectors.toSet()));
        });
        context.put("tags", allKeys);
    }
    
    private static void pushTsEntries(VelocityContext context, TelemetryUploadRequest payload) {
        Set<BasicTsKvEntry> allKeys = new HashSet<>();
        payload.getData().forEach((k, vList) -> {
            allKeys.addAll(vList.stream().map(v -> {
                context.put(v.getKey(), new BasicTsKvEntry(k, v));
                return new BasicTsKvEntry(k, v);
            }).collect(Collectors.toSet()));
        });
        context.put("tags", allKeys);
    }



    private static void pushAttributes(VelocityContext context, Collection<AttributeKvEntry> deviceAttributes, String prefix) {
        Map<String, String> values = new HashMap<>();
        Set<StringDataEntry> clientAttrib = new HashSet<>();
        if(prefix.contentEquals(NashornJsEvaluator.CLIENT_SIDE)){
            deviceAttributes.forEach(v -> {values.put(v.getKey(), v.getValueAsString());
                clientAttrib.add(new StringDataEntry(v.getKey(), v.getValueAsString()));});
            context.put(prefix, values);
            context.put("cs",values);
            context.put("attrtags", clientAttrib);
        }
        else {
            deviceAttributes.forEach(v -> values.put(v.getKey(), v.getValueAsString()));
            context.put(prefix, values);
        }
    }

}
