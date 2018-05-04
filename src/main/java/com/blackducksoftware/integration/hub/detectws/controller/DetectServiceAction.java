/**
 * hub-detect-ws
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.detectws.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.detectws.execute.AsyncCmdExecutor;
import com.blackducksoftware.integration.hub.detectws.execute.SimpleExecutor;
import com.blackducksoftware.integration.hub.detectws.execute.fromdetect.ExecutableRunnerException;
import com.blackducksoftware.integration.hub.detectws.state.ReadyDao;

@Component
public class DetectServiceAction {
    private static final String FETCH_DETECT_OPTION_SAVE = "-s";
    private static final String FETCH_DETECT_CMD = "curl";
    private static final String DETECT_EXE_URL = "https://blackducksoftware.github.io/hub-detect/hub-detect.sh";
    public static final String PGM_DIR = "/opt/blackduck/hub-detect-ws";
    private static final String SRC_DIR_PATH = String.format("%s/source", PGM_DIR);
    private static final String OUTPUT_DIR_PATH = String.format("%s/output", PGM_DIR);
    private static final String DETECT_EXE_PATH = String.format("%s/detect.sh", PGM_DIR);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Environment propertySources;

    @Autowired
    ReadyDao readyDao;

    @Value("${detectws.logging.level}")
    private String loggingLevel;

    @Value("${detectws.image.inspector.url}")
    private String imageInspectorUrl;

    @Value("${detectws.detect.service.shared.dir.path:/opt/blackduck/shared}")
    private String detectServiceSharedDirPath;

    @Value("${detectws.image.inspector.service.shared.dir.path:/opt/blackduck/shared}")
    private String imageInspectorServiceSharedDirPath;

    @Value("${detectws.detect.java.opts:}")
    private String detectJavaOpts;

    @Value("${detectws.service.request.timeout.seconds}")
    private String serviceRequestTimeoutSecondsString;

    public String scanImage(final Map<String, String> requestParams)
            throws IntegrationException, IOException, InterruptedException, ExecutableRunnerException {
        readyDao.clearReadyFlag();
        final File pgmDir = new File(PGM_DIR);
        downloadDetect(pgmDir);
        launchDetectAsync(pgmDir, requestParams);
        return "scan/inspect image request accepted";
    }

    public boolean ready() {
        return readyDao.isReady();
    }

    private void launchDetectAsync(final File pgmDir, final Map<String, String> requestParams) throws IOException {

        // TODO add support for hub project name/version, username, and password with spaces
        logger.debug(String.format("loggingLevel: %s", loggingLevel));
        logger.debug(String.format("imageInspectorUrl: %s", imageInspectorUrl));
        logger.debug(String.format("detectServiceSharedDirPath: %s", detectServiceSharedDirPath));
        logger.debug(String.format("imageInspectorServiceSharedDirPath: %s", imageInspectorServiceSharedDirPath));
        logger.debug(String.format("serviceRequestTimeoutSecondsString: %s", serviceRequestTimeoutSecondsString));

        // Build map of properties to pass to detect
        // Pre-populate it with user-provided values from configMap
        // Then add/overwrite some that hub-detect-ws needs to be set a certain way
        // Then add/overwrite values in from requestParams, so in case of conflict: requestParams wins
        final Map<String, String> detectParams = new HashMap<>();
        addApplicationDotPropertiesFileValues(detectParams);
        detectParams.put("logging.level.com.blackducksoftware.integration", loggingLevel);
        detectParams.put("detect.excluded.bom.tool.types", "GRADLE");
        detectParams.put("detect.docker.passthrough.imageinspector.url", imageInspectorUrl);
        detectParams.put("detect.docker.passthrough.shared.dir.path.imageinspector", imageInspectorServiceSharedDirPath);
        detectParams.put("detect.docker.passthrough.shared.dir.path.local", detectServiceSharedDirPath);
        final long serviceRequestTimeoutSeconds = Integer.parseInt(serviceRequestTimeoutSecondsString);
        final Long serviceRequestTimeoutMilliseconds = serviceRequestTimeoutSeconds * 1000L;
        detectParams.put("detect.docker.passthrough.command.timeout", serviceRequestTimeoutMilliseconds.toString());
        // TODO output dir should get cleaned up later somehow
        final String outputFilePath = String.format("%s/run_%d_%d", OUTPUT_DIR_PATH, new Date().getTime(), (int) (Math.random() * 10000));
        detectParams.put("detect.output.path", outputFilePath);
        detectParams.put("detect.docker.passthrough.on.host", "false");
        detectParams.put("detect.source.path", SRC_DIR_PATH);
        detectParams.putAll(requestParams);

        final Map<String, String> detectCmdEnv = new HashMap<>();
        if (StringUtils.isNotBlank(detectJavaOpts)) {
            detectCmdEnv.put("DETECT_JAVA_OPTS", detectJavaOpts);
        }

        logger.info("Launching detect");
        logger.debug(String.format("detectCmdArgs: %s", detectParams.toString()));
        logger.debug(String.format("detectCmdEnv: %s", detectCmdEnv.toString()));
        final List<String> detectCmdArgs = propertiesToArgs(detectParams);
        final AsyncCmdExecutor executor = new AsyncCmdExecutor(readyDao, pgmDir,
                detectCmdEnv,
                DETECT_EXE_PATH, detectCmdArgs);
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(executor);
    }

    private void addApplicationDotPropertiesFileValues(final Map<String, String> detectParams) {
        logger.debug("Reading user-specified properties from configMap:");
        for (final Iterator<PropertySource<?>> propertySourceIter = ((AbstractEnvironment) propertySources).getPropertySources().iterator(); propertySourceIter.hasNext();) {
            final PropertySource<?> propertySource = propertySourceIter.next();
            if (propertySource instanceof MapPropertySource) {
                logger.debug(String.format("\tFound property source: %s", propertySource.getName()));
                // TODO is this test reliable?
                if (propertySource.getName().startsWith("applicationConfig") && propertySource.getName().contains("[file:")) {
                    logger.debug("\t\tVetting these properties for detectParams");
                    final Map<String, Object> configMap = ((MapPropertySource) propertySource).getSource();
                    for (final String key : configMap.keySet()) {
                        logger.debug(String.format("\t\t\tChecking key prefix of property of %s", key));
                        if (key.startsWith("detectws.")) {
                            logger.debug(String.format("\t\t\t %s is a detectws property; omitting it from detect properties", key));
                            continue;
                        }
                        logger.debug(String.format("\t\t\tChecking type of value of property %s", key));
                        final Object value = configMap.get(key);
                        if (value instanceof String) {
                            logger.debug("\t\t\tIt's a String); adding it to detect properties");
                            detectParams.put(key, (String) value);
                        }
                    }
                }
            }
        }
    }

    private List<String> propertiesToArgs(final Map<String, String> properties) {
        final List<String> args = new ArrayList<>();
        properties.forEach((key, value) -> args.add(String.format("--%s=%s", key, value)));
        return args;
    }

    private void downloadDetect(final File pgmDir) throws IOException, InterruptedException, IntegrationException, ExecutableRunnerException {
        logger.info("Downloading detect");
        // TODO don't download it every time?
        final Map<String, String> environmentVariables = new HashMap<>();
        final List<String> args = Arrays.asList(FETCH_DETECT_OPTION_SAVE, DETECT_EXE_URL);
        final String detectScriptString = SimpleExecutor.execute(pgmDir, environmentVariables, FETCH_DETECT_CMD, args);
        final File detectScriptFile = new File(DETECT_EXE_PATH);
        FileUtils.write(detectScriptFile, detectScriptString, StandardCharsets.UTF_8);
        detectScriptFile.setExecutable(true);
    }

}
