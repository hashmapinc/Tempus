/**
 * Copyright © 2016-2017 Hashmap, Inc
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
package org.thingsboard.server.service.computation.annotation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import com.hashmap.tempus.annotations.ConfigurationMapping;
import com.hashmap.tempus.annotations.Configurations;
import com.hashmap.tempus.annotations.SparkAction;
import com.hashmap.tempus.annotations.SparkRequest;
import com.hashmap.tempus.models.SparkActionConfigurationType;
import com.hashmap.tempus.models.SparkActionRequestType;
import com.hashmap.tempus.models.SparkActionType;
import eu.infomas.annotation.AnnotationDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.thingsboard.server.common.msg.computation.ComputationActionCompiled;
import org.thingsboard.server.common.msg.computation.ComputationRequestCompiled;
import org.thingsboard.server.service.computation.classloader.RuntimeJavaCompiler;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
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
    private final RuntimeJavaCompiler compiler;
    private final Path jar;
    private ObjectMapper mapper = new ObjectMapper();

    public AnnotationsProcessor(Path jar,
                                RuntimeJavaCompiler compiler){
        this.jar = jar;
        this.classLoader = jarClassloader(jar);
        this.compiler = compiler;
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
        SparkActionRequestType model = request(clazz);
        return model;
    }

    private SparkActionType action(Class<?> clazz){
        SparkAction action = clazz.getAnnotation(SparkAction.class);
        SparkActionType model = new SparkActionType();
        model.setPackageName(clazz.getPackage().getName());
        model.setApplicationKey(action.applicationKey());
        model.setClassName(action.actionClass());
        model.setName(action.name());
        model.setDescriptor(action.descriptor());
        return model;
    }

    private SparkActionConfigurationType configurations(Class<?> clazz){
        Configurations configs = clazz.getAnnotation(Configurations.class);
        SparkActionConfigurationType actionConfiguration = null;
        if(configs != null) {
            actionConfiguration = new SparkActionConfigurationType();
            actionConfiguration.setClassName(configs.className());
            for (ConfigurationMapping mapping : configs.mappings()) {
                try {
                    actionConfiguration.addField(mapping.field(), mapping.type().getCanonicalName());
                } catch (MirroredTypeException me) {
                    TypeMirror typeMirror = me.getTypeMirror();
                    log.debug("found config {} for {}", mapping.field(), typeMirror.toString());
                    actionConfiguration.addField(mapping.field(), typeMirror.toString());
                }
            }
        }
        return actionConfiguration;
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

    private ComputationActionCompiled processModel(SparkActionType model){
        try {
            Properties props = new Properties();
            URL url = this.getClass().getClassLoader().getResource("velocity.properties");
            props.load(url.openStream());

            VelocityEngine ve = new VelocityEngine(props);
            ve.init();
            VelocityContext vc = new VelocityContext();
            vc.put("model", model);
            Template ct = ve.getTemplate("templates/config.vm");
            Class<?> aClass = generateSource(ct, vc, model.getPackageName() + "." + model.getConfiguration().getClassName());
            if(aClass != null) {
                Template at = ve.getTemplate("templates/action.vm");
                generateSource(at, vc, model.getPackageName() + "." + model.getClassName());
                JsonNode descriptor = descriptorNode(model.getDescriptor());
                return new ComputationActionCompiled(model.getPackageName() + "." + model.getClassName(), model.getDescriptor(), model.getName(), descriptor);
            }
        } catch (IOException e) {
            log.error("Exception occurred while generating java source", e);
        } catch (ClassNotFoundException e) {
            log.error("Exception while loading class", e);
        }
        return null;
    }

    private ComputationRequestCompiled processModelRequest(SparkActionRequestType model){
        try {
            Properties props = new Properties();
            URL url = this.getClass().getClassLoader().getResource("velocity.properties");
            props.load(url.openStream());
            JsonNode descriptor = descriptorNode(model.getDescriptor());
            return new ComputationRequestCompiled(model.getArgs(), model.getArgType(), model.getName(), descriptor, model.getMainClass());
        } catch (IOException e) {
            log.error("Exception occurred while generating java source", e);
        }
        return null;
    }


    private Class<?> generateSource(Template vt, VelocityContext vc, String sourceName) throws IOException, ClassNotFoundException {
        log.debug("Generating source for {}", sourceName);
        StringWriter writer = new StringWriter();

        vt.merge(vc, writer);
        Class<?> clazz = null;

        try {
            compiler.compile(sourceName, writer.toString());
            clazz = compiler.load(sourceName);
        }catch(Exception e){
            log.error("Error occurred while processing dynamic actions", e);
        }
        writer.close();
        return clazz;
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
