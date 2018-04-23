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
    private ProgramVersion programVersion;

    @Autowired
    ReadyDao readyDao;

    @Value("${hub.url}")
    private String hubUrl;

    @Value("${hub.username}")
    private String hubUsername;

    @Value("${hub.password}")
    private String hubPassword;

    @Value("${logging.level}")
    private String loggingLevel;

    @Value("${image.inspector.url}")
    private String imageInspectorUrl;

    @Value("${detect.service.shared.dir.path:/opt/blackduck/shared}")
    private String detectServiceSharedDirPath;

    @Value("${image.inspector.service.shared.dir.path:/opt/blackduck/shared}")
    private String imageInspectorServiceSharedDirPath;

    @Value("${detect.java.opts:}")
    private String detectJavaOpts;

    @Value("${detect.hub.signature.scanner.memory:1024}")
    private String signatureScannerMemoryString;

    @Value("${detect.cleanup.bom.tool.files}")
    private String detectCleanupBomToolFilesString;

    @Value("${hub.trust}")
    private String hubTrustString;

    public String scanImage(final String dockerTarfilePath, final String hubProjectName, final String hubProjectVersion, final String codeLocationPrefix, final boolean cleanupWorkingDir)
            throws IntegrationException, IOException, InterruptedException, ExecutableRunnerException {
        // TODO Do something with codelocationprefix and cleanup args
        final String msg = String.format("hub-detect-ws v%s: dockerTarfilePath: %s, hubProjectName: %s, hubProjectVersion: %s, codeLocationPrefix: %s, cleanupWorkingDir: %b", programVersion.getProgramVersion(), dockerTarfilePath,
                hubProjectName, hubProjectVersion, codeLocationPrefix, cleanupWorkingDir);
        logger.info(msg);
        final File pgmDir = new File(PGM_DIR);
        downloadDetect(pgmDir);
        launchDetectAsync(pgmDir, dockerTarfilePath, hubProjectName, hubProjectVersion);
        return "scan/inspect image request accepted";
    }

    public boolean ready() {
        return readyDao.isReady();
    }

    private void launchDetectAsync(final File pgmDir, final String dockerTarfilePath, final String hubProjectName, final String hubProjectVersion) throws IOException {
        // TODO add support for project name/version with spaces
        readyDao.setReady(false);

        logger.debug(String.format("hubUrl: %s", hubUrl));
        logger.debug(String.format("hubUsername: %s", hubUsername));
        logger.debug(String.format("hubPassword: %s", hubPassword));
        logger.debug(String.format("hubTrustString: %s", hubTrustString));
        logger.debug(String.format("loggingLevel: %s", loggingLevel));
        logger.debug(String.format("imageInspectorUrl: %s", imageInspectorUrl));
        logger.debug(String.format("detectServiceSharedDirPath: %s", detectServiceSharedDirPath));
        logger.debug(String.format("imageInspectorServiceSharedDirPath: %s", imageInspectorServiceSharedDirPath));
        logger.debug(String.format("detectJavaOpts: %s", detectJavaOpts));
        logger.debug(String.format("signatureScannerMemoryString: %s", signatureScannerMemoryString));
        logger.debug(String.format("detectCleanupBomToolFilesString: %s", detectCleanupBomToolFilesString));

        // TODO output dir should get cleaned up later somehow
        final String outputFilePath = String.format("%s/run_%d_%d", OUTPUT_DIR_PATH, new Date().getTime(), (int) (Math.random() * 10000));
        final List<String> detectCmdArgs = new ArrayList<>();
        detectCmdArgs.add(String.format("--blackduck.hub.url=%s", hubUrl));
        detectCmdArgs.add(String.format("--blackduck.hub.username=%s", hubUsername));
        detectCmdArgs.add(String.format("--blackduck.hub.password=%s", hubPassword));
        detectCmdArgs.add(String.format("--blackduck.hub.trust.cert=%b", hubTrustString.equalsIgnoreCase("true") ? true : false));
        detectCmdArgs.add(String.format("--logging.level.com.blackducksoftware.integration=%s", loggingLevel));
        detectCmdArgs.add(String.format("--detect.docker.tar=%s", dockerTarfilePath));

        // TODO TEMP; when "exclude all" is available: use that
        detectCmdArgs.add(String.format("--detect.excluded.bom.tool.types=%s", "GRADLE"));

        detectCmdArgs.add(String.format("--detect.docker.passthrough.imageinspector.url=%s", imageInspectorUrl));
        detectCmdArgs.add(String.format("--detect.docker.passthrough.shared.dir.path.imageinspector=%s", imageInspectorServiceSharedDirPath));
        detectCmdArgs.add(String.format("--detect.docker.passthrough.shared.dir.path.local=%s", detectServiceSharedDirPath));
        detectCmdArgs.add(String.format("--detect.cleanup.bom.tool.files=%b", detectCleanupBomToolFilesString.equalsIgnoreCase("true") ? true : false));

        detectCmdArgs.add(String.format("--detect.output.path=%s", outputFilePath));

        detectCmdArgs.add(String.format("--detect.docker.passthrough.on.host=%b", false));
        if (StringUtils.isNotBlank(signatureScannerMemoryString)) {
            final int signatureScannerMemory = Integer.parseInt(signatureScannerMemoryString);
            detectCmdArgs.add(String.format("--detect.hub.signature.scanner.memory=%d", signatureScannerMemory));
        }

        detectCmdArgs.add(String.format("--detect.source.path=%s", SRC_DIR_PATH));
        if (StringUtils.isNotBlank(hubProjectName)) {
            detectCmdArgs.add(String.format("--detect.project.name=%s", hubProjectName));
        }
        if (StringUtils.isNotBlank(hubProjectName)) {
            detectCmdArgs.add(String.format("--detect.project.version.name=%s", hubProjectVersion));
        }
        final Map<String, String> detectCmdEnv = new HashMap<>();
        if (StringUtils.isNotBlank(detectJavaOpts)) {
            detectCmdEnv.put("DETECT_JAVA_OPTS", detectJavaOpts);
        }

        logger.info("Launching detect");
        logger.debug(String.format("detectCmdArgs: %s", detectCmdArgs.toString()));
        logger.debug(String.format("detectCmdEnv: %s", detectCmdEnv.toString()));
        final AsyncCmdExecutor executor = new AsyncCmdExecutor(readyDao, pgmDir,
                detectCmdEnv,
                DETECT_EXE_PATH, detectCmdArgs);
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(executor);
    }

    private void downloadDetect(final File pgmDir) throws IOException, InterruptedException, IntegrationException, ExecutableRunnerException {
        logger.info("Downloading detect");
        final Map<String, String> environmentVariables = new HashMap<>();
        final List<String> args = Arrays.asList(FETCH_DETECT_OPTION_SAVE, DETECT_EXE_URL);
        final String detectScriptString = SimpleExecutor.execute(pgmDir, environmentVariables, FETCH_DETECT_CMD, args);
        final File detectScriptFile = new File(DETECT_EXE_PATH);
        FileUtils.write(detectScriptFile, detectScriptString, StandardCharsets.UTF_8);
        detectScriptFile.setExecutable(true);
    }

}
