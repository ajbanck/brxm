/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms7.essentials.plugins.projects;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import org.onehippo.cms7.essentials.dashboard.ctx.PluginContext;
import org.onehippo.cms7.essentials.dashboard.instructions.Instruction;
import org.onehippo.cms7.essentials.dashboard.instructions.InstructionStatus;
import org.onehippo.cms7.essentials.dashboard.model.MavenDependency;
import org.onehippo.cms7.essentials.dashboard.packaging.MessageGroup;
import org.onehippo.cms7.essentials.dashboard.service.ContextXmlService;
import org.onehippo.cms7.essentials.dashboard.service.LoggingService;
import org.onehippo.cms7.essentials.dashboard.service.MavenAssemblyService;
import org.onehippo.cms7.essentials.dashboard.service.MavenCargoService;
import org.onehippo.cms7.essentials.dashboard.service.MavenDependencyService;
import org.onehippo.cms7.essentials.dashboard.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add WPM JDBC resource to context.xml.
 */
public class ProjectsInstruction implements Instruction {

    private static Logger log = LoggerFactory.getLogger(ProjectsInstruction.class);
    private static final String WPM_RESOURCE_NAME = "jdbc/wpmDS";
    private static final Map<String, String> WPM_RESOURCE_ATTRIBUTES = new LinkedHashMap<>();
    private static final String WPM_WEBAPP_ARTIFACTID = "hippo-addon-wpm-camunda";
    private static final String ENTERPRISE_SERVICES_ARTIFACTID = "hippo-enterprise-services";
    private static final String WPM_WEBAPP_CONTEXT = "/bpm";
    private static final String LOGGER_HST_BRANCH = "com.onehippo.cms7.hst.configuration.branch";
    private static final String LOGGER_PROJECT = "com.onehippo.cms7.services.wpm.project";

    static {
        WPM_RESOURCE_ATTRIBUTES.put("auth", "Container");
        WPM_RESOURCE_ATTRIBUTES.put("type", "javax.sql.DataSource");
        WPM_RESOURCE_ATTRIBUTES.put("maxTotal", "100");
        WPM_RESOURCE_ATTRIBUTES.put("maxIdle", "10");
        WPM_RESOURCE_ATTRIBUTES.put("initialSize", "10");
        WPM_RESOURCE_ATTRIBUTES.put("maxWaitMillis", "10000");
        WPM_RESOURCE_ATTRIBUTES.put("testWhileIdle", "true");
        WPM_RESOURCE_ATTRIBUTES.put("testOnBorrow", "false");
        WPM_RESOURCE_ATTRIBUTES.put("validationQuery", "SELECT 1");
        WPM_RESOURCE_ATTRIBUTES.put("timeBetweenEvictionRunsMillis", "10000");
        WPM_RESOURCE_ATTRIBUTES.put("minEvictableIdleTimeMillis", "60000");
        WPM_RESOURCE_ATTRIBUTES.put("username", "sa");
        WPM_RESOURCE_ATTRIBUTES.put("password", "");
        WPM_RESOURCE_ATTRIBUTES.put("driverClassName", "org.h2.Driver");
        WPM_RESOURCE_ATTRIBUTES.put("url", "jdbc:h2:./wpm/wpm;AUTO_SERVER=TRUE");
    }

    @Inject private ContextXmlService contextXmlService;
    @Inject private LoggingService loggingService;
    @Inject private ProjectService projectService;
    @Inject private MavenCargoService mavenCargoService;
    @Inject private MavenDependencyService mavenDependencyService;
    @Inject private MavenAssemblyService mavenAssemblyService;

    @Override
    public InstructionStatus execute(final PluginContext context) {
        contextXmlService.addResource(WPM_RESOURCE_NAME, WPM_RESOURCE_ATTRIBUTES);

        projectService.getLog4j2Files().forEach(f -> {
            loggingService.addLoggerToLog4jConfiguration(f, LOGGER_HST_BRANCH, "warn");
            loggingService.addLoggerToLog4jConfiguration(f, LOGGER_PROJECT, "warn");
        });

        // Install "enterprise services" JAR
        final MavenDependency enterpriseServices
                = mavenDependencyService.createDependency(ProjectService.GROUP_ID_ENTERPRISE, ENTERPRISE_SERVICES_ARTIFACTID);
        mavenCargoService.addDependencyToCargoSharedClasspath(context, enterpriseServices);
        mavenAssemblyService.addIncludeToFirstDependencySet("shared-lib-component.xml", enterpriseServices);

        // Install BPM WAR
        final MavenDependency bpmWar = mavenDependencyService.createDependency(ProjectService.GROUP_ID_ENTERPRISE,
                WPM_WEBAPP_ARTIFACTID, null, "war", null);
        mavenCargoService.addDeployableToCargoRunner(context, bpmWar, WPM_WEBAPP_CONTEXT);
        mavenAssemblyService.addDependencySet("webapps-component.xml", "webapps",
                "bpm.war", false, "provided", bpmWar);

        return InstructionStatus.SUCCESS;
    }

    @Override
    public void populateChangeMessages(final BiConsumer<MessageGroup, String> changeMessageQueue) {
        changeMessageQueue.accept(MessageGroup.EXECUTE, "Add Resource '" + WPM_RESOURCE_NAME + "' to Tomcat context.xml.");
        changeMessageQueue.accept(MessageGroup.EXECUTE, "Add Logger '" + LOGGER_HST_BRANCH + "' to log4j2 configuration files.");
        changeMessageQueue.accept(MessageGroup.EXECUTE, "Add Logger '" + LOGGER_PROJECT + "' to log4j2 configuration files.");
        changeMessageQueue.accept(MessageGroup.EXECUTE, "Add dependency '" + ProjectService.GROUP_ID_ENTERPRISE
                + ":" + ENTERPRISE_SERVICES_ARTIFACTID + "' to shared classpath of the Maven cargo plugin configuration.");
        changeMessageQueue.accept(MessageGroup.EXECUTE, "Add same dependency to distribution configuration file 'shared-lib-component.xml'.");
        changeMessageQueue.accept(MessageGroup.EXECUTE, "Add deployable '" + WPM_WEBAPP_ARTIFACTID
                + ".war' with context path '" + WPM_WEBAPP_CONTEXT + "' to deployables of Maven cargo plugin configuration.");
        changeMessageQueue.accept(MessageGroup.EXECUTE, "Add same web application to distribution configuration file 'webapps-component.xml'.");
    }
}
