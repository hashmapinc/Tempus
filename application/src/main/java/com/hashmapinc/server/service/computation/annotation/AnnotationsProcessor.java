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
package com.hashmapinc.server.service.computation.annotation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import com.hashmap.tempus.annotations.SparkRequest;
import com.hashmap.tempus.models.SparkActionRequestType;
import com.hashmapinc.server.common.msg.computation.ComputationRequestCompiled;
import eu.infomas.annotation.AnnotationDetector;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class AnnotationsProcessor {
    private final ClassLoader classLoader;
    private final Path jar;
    private ObjectMapper mapper = new ObjectMapper();

    public AnnotationsProcessor(Path jar){
        this.jar = jar;
        this.classLoader = jarClassloader(jar);
    }

    public List<ComputationRequestCompiled> processAnnotations() throws IOException {
        final List<ComputationRequestCompiled> requests = new ArrayList<>();
        AnnotationDetector detector = new AnnotationDetector(newReporter(requests));
        detector.detect(jar.toFile());
        return requests;
    }

    private AnnotationDetector.TypeReporter newReporter(List<ComputationRequestCompiled> requests){
        return new AnnotationDetector.TypeReporter() {
            @Override
            public void reportTypeAnnotation(Class<? extends Annotation> aClass, String s) {
                if(aClass.isAssignableFrom(SparkRequest.class)){
                    try {
                        ComputationRequestCompiled request = processModelRequest(buildModelFromAnnotations(s));
                        if(request != null) {
                            requests.add(request);
                        }
                        log.debug("Java Source creation and loading completed for {} ", s);
                    } catch (ClassNotFoundException e) {
                        log.error("Class not found", e);
                    }
                }
            }
            @SuppressWarnings("unchecked")
            @Override
            public Class<? extends Annotation>[] annotations() {
                return new Class[]{SparkRequest.class};
            }
        };
    }

    private SparkActionRequestType buildModelFromAnnotations(String clazzString) throws ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(clazzString);
        return request(clazz);
    }

    private SparkActionRequestType request(Class<?> clazz){
        SparkRequest request = clazz.getAnnotation(SparkRequest.class);
        SparkActionRequestType actionRequest = null;
        if(request != null){
            actionRequest = new SparkActionRequestType();
            actionRequest.setMainClass(request.main());
            actionRequest.setJar(request.jar());
            actionRequest.setArgs(request.args());
            actionRequest.setDescriptor(request.descriptor());
            actionRequest.setName(request.name());
            actionRequest.setArgType(request.argType());
        }
        return actionRequest;
    }

    private ComputationRequestCompiled processModelRequest(SparkActionRequestType model){
        try {
            Properties props = new Properties();
            URL url = this.getClass().getClassLoader().getResource("velocity.properties");
            props.load(url.openStream());
            if(model != null) {
                JsonNode descriptor = descriptorNode(model.getDescriptor());
                return new ComputationRequestCompiled(model.getArgs(), model.getArgType(), model.getName(), descriptor, model.getMainClass());
            } else {
                return null;
            }

        } catch (IOException e) {
            log.error("Exception occurred while generating java source", e);
        }
        return null;
    }


    private JsonNode descriptorNode(String descriptor) throws IOException {
        InputStream descriptorResource = classLoader.getResourceAsStream(descriptor);
        if(descriptorResource == null){
           return mapper.readTree(
                    Resources.toString(Resources.getResource(descriptor), Charsets.UTF_8));
        }else{
            return mapper.readTree(CharStreams.toString(new InputStreamReader(descriptorResource, "UTF-8")));
        }
    }

    private ClassLoader jarClassloader(Path jarPath) {
        try {
            return new URLClassLoader(new URL[]{jarPath.toUri().toURL()}, this.getClass().getClassLoader());
        } catch (MalformedURLException e) {
            return Thread.currentThread().getContextClassLoader();
        }
    }
}
