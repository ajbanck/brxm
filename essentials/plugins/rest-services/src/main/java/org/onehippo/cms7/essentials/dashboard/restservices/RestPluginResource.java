/*
 * Copyright 2014-2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cms7.essentials.dashboard.restservices;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import org.onehippo.cms7.essentials.dashboard.ctx.PluginContext;
import org.onehippo.cms7.essentials.dashboard.ctx.PluginContextFactory;
import org.onehippo.cms7.essentials.dashboard.instruction.FileInstruction;
import org.onehippo.cms7.essentials.dashboard.instruction.PluginInstructionSet;
import org.onehippo.cms7.essentials.dashboard.instruction.executors.PluginInstructionExecutor;
import org.onehippo.cms7.essentials.dashboard.instructions.InstructionExecutor;
import org.onehippo.cms7.essentials.dashboard.instructions.InstructionSet;
import org.onehippo.cms7.essentials.dashboard.model.UserFeedback;
import org.onehippo.cms7.essentials.dashboard.packaging.DefaultInstructionPackage;
import org.onehippo.cms7.essentials.dashboard.packaging.InstructionPackage;
import org.onehippo.cms7.essentials.dashboard.rest.BaseResource;
import org.onehippo.cms7.essentials.dashboard.rest.KeyValueRestful;
import org.onehippo.cms7.essentials.dashboard.rest.PostPayloadRestful;
import org.onehippo.cms7.essentials.dashboard.rest.RestfulList;
import org.onehippo.cms7.essentials.dashboard.service.JcrService;
import org.onehippo.cms7.essentials.dashboard.service.RebuildService;
import org.onehippo.cms7.essentials.dashboard.service.SettingsService;
import org.onehippo.cms7.essentials.dashboard.services.ContentBeansService;
import org.onehippo.cms7.essentials.dashboard.utils.GlobalUtils;
import org.onehippo.cms7.essentials.dashboard.utils.HstUtils;
import org.onehippo.cms7.essentials.dashboard.utils.JavaSourceUtils;
import org.onehippo.cms7.essentials.dashboard.utils.annotations.AnnotationUtils;

@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
@Path("/restservices")
public class RestPluginResource extends BaseResource {

    @Inject private RebuildService rebuildService;
    @Inject private PluginContextFactory contextFactory;
    @Inject private JcrService jcrService;
    @Inject private SettingsService settingsService;
    @Inject private ContentBeansService contentBeansService;

    @GET
    @Path("/beans")
    public RestfulList<KeyValueRestful> getHippoBeans() {
        final RestfulList<KeyValueRestful> list = new RestfulList<>();
        final Map<String, java.nio.file.Path> beans = contentBeansService.findBeans();
        for (java.nio.file.Path beanPath : beans.values()) {
            final String fileName = beanPath.toFile().getName();
            list.add(new KeyValueRestful(fileName, beanPath.toString()));
        }
        return list;
    }


    @GET
    @Path("/mounts")
    public List<MountRestful> getHippoSites() throws RepositoryException {
        final Set<Node> hstMounts = HstUtils.getHstMounts(jcrService);
        final List<MountRestful> list = new ArrayList<>();
        for (Node m : hstMounts) {
            list.add(new MountRestful(m.getIdentifier(), m.getPath(), m.getName()));
        }
        return list;
    }


    @POST
    @Path("/")
    public UserFeedback executeInstructionPackage(final PostPayloadRestful payloadRestful) {
        final UserFeedback feedback = new UserFeedback();
        final PluginContext context = contextFactory.getContext();
        final Map<String, String> values = payloadRestful.getValues();
        final boolean isGenericApiEnabled = Boolean.valueOf(values.get(RestPluginConst.GENERIC_API_ENABLED));
        final boolean isManualApiEnabled = Boolean.valueOf(values.get(RestPluginConst.MANUAL_API_ENABLED));
        final String genericRestName = values.get(RestPluginConst.GENERIC_REST_NAME);
        final String manualRestName = values.get(RestPluginConst.MANUAL_REST_NAME);

        if (isGenericApiEnabled && isManualApiEnabled && genericRestName.equals(manualRestName)) {
            return feedback.addError("Generic and manual REST resources must use different URLs.");
        }

        if (isManualApiEnabled) {
            if (Strings.isNullOrEmpty(manualRestName)) {
                return feedback.addError("Manual REST resource URL must be specified.");
            }

            final InstructionExecutor executor = new PluginInstructionExecutor();
            final String selectedBeans = values.get(RestPluginConst.JAVA_FILES);
            final Set<ValidBean> validBeans = annotateBeans(selectedBeans);

            for (ValidBean validBean : validBeans) {
                final Map<String, Object> properties = new HashMap<>();
                properties.put("beanPackage", validBean.getBeanPackage());
                properties.put("beanName", validBean.getBeanName());
                properties.put("beans", validBean.getFullQualifiedName());
                properties.put("fullQualifiedName", validBean.getFullQualifiedName());
                properties.put("fullQualifiedResourceName", validBean.getFullQualifiedResourceName());
                context.addPlaceholderData(properties);

                final InstructionSet mySet = new PluginInstructionSet();
                mySet.addInstruction(createFileInstruction());
                executor.execute(mySet, context);
            }

            final InstructionPackage instructionPackage = new DefaultInstructionPackage() {
                @Override
                public String getInstructionPath() {
                    return "/META-INF/manual_rest_instructions.xml";
                }
            };
            getInjector().autowireBean(instructionPackage);
            values.put(RestPluginConst.REST_NAME, manualRestName);
            values.put(RestPluginConst.PIPELINE_NAME, RestPluginConst.MANUAL_PIPELINE_NAME);
            instructionPackage.setProperties(new HashMap<>(values));
            instructionPackage.getProperties().put("beans", validBeans);
            instructionPackage.execute(context);

            rebuildService.requestRebuild("restServices");
            feedback.addSuccess("Spring configuration changed, project rebuild needed.");
        }

        if (isGenericApiEnabled) {
            if (Strings.isNullOrEmpty(genericRestName)) {
                return feedback.addError("Generic REST resource URL must be specified.");
            }

            final InstructionPackage instructionPackage = new DefaultInstructionPackage() {
                @Override
                public String getInstructionPath() {
                    return "/META-INF/generic_rest_instructions.xml";
                }
            };
            getInjector().autowireBean(instructionPackage);
            values.put(RestPluginConst.REST_NAME, genericRestName);
            values.put(RestPluginConst.PIPELINE_NAME, RestPluginConst.GENERIC_PIPELINE_NAME);
            instructionPackage.setProperties(new HashMap<>(values));
            instructionPackage.execute(context);
        }

        return feedback.addSuccess("REST configuration setup was successful.");
    }

    private FileInstruction createFileInstruction() {
        final FileInstruction instruction = new FileInstruction();
        getInjector().autowireBean(instruction);
        instruction.setAction("copy");
        instruction.setOverwrite(false);
        instruction.setBinary(false);
        instruction.setSource("BeanNameResource.txt");
        instruction.setTarget("{{restFolder}}/{{beanName}}Resource.java");
        return instruction;
    }

    private Set<ValidBean> annotateBeans(final String input) {
        final Set<ValidBean> validBeans = new HashSet<>();

        if (Strings.isNullOrEmpty(input)) {
            return validBeans;
        }
        final Iterable<String> split = Splitter.on(',').split(input);
        for (String path : split) {
            final File file = new File(path);
            if (file.exists()) {
                final java.nio.file.Path filePath = file.toPath();
                final String className = JavaSourceUtils.getClassName(filePath);
                if (Strings.isNullOrEmpty(className)) {
                    continue;
                }
                String source = GlobalUtils.readTextFile(filePath).toString();
                // access annotation:
                source = AnnotationUtils.addXmlAccessNoneAnnotation(source);
                // root annotation
                source = AnnotationUtils.addXmlRootAnnotation(source, className.toLowerCase());
                // annotate fields:
                source = AnnotationUtils.addXmlElementAnnotation(source);
                //add adapters:
                source = AnnotationUtils.addKnownAdapters(source);
                // rewrite bean:
                GlobalUtils.writeToFile(source, filePath);

                final ValidBean bean = new ValidBean();
                bean.setBeanName(className);
                bean.setBeanPath(path);
                bean.setBeanPackage(JavaSourceUtils.getPackageName(filePath));
                final String qualifiedClassName = JavaSourceUtils.getFullQualifiedClassName(filePath);
                bean.setFullQualifiedName(qualifiedClassName);
                final String resourceName = MessageFormat.format("{0}.{1}Resource", settingsService.getSettings().getSelectedRestPackage(), className);
                bean.setFullQualifiedResourceName(resourceName);
                validBeans.add(bean);
            }
        }
        return validBeans;
    }
}
