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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.detectws.execute.AsyncCmdExecutor;
import com.blackducksoftware.integration.hub.detectws.execute.CmdExecutor;

@Component
public class DetectServiceAction {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProgramVersion programVersion;

    public String scanImage(final String dockerTarfilePath, final String hubProjectName, final String hubProjectVersion, final String codeLocationPrefix, final boolean cleanupWorkingDir)
            throws IntegrationException, IOException, InterruptedException {
        final String msg = String.format("hub-detect-ws v%s: dockerTarfilePath: %s, hubProjectName: %s, hubProjectVersion: %s, codeLocationPrefix: %s, cleanupWorkingDir: %b", programVersion.getProgramVersion(), dockerTarfilePath,
                hubProjectName, hubProjectVersion, codeLocationPrefix, cleanupWorkingDir);
        logger.info(msg);
        downloadDetect();
        launchDetectAsync();
        return "scan/inspect image acceptance mocked";
    }

    private void launchDetectAsync() {
        logger.info("Launching detect");
        final AsyncCmdExecutor executor = new AsyncCmdExecutor("/tmp/detect.sh --help", null, 5, null);
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        // final Future<String> containerCleanerFuture =
        executorService.submit(executor);
    }

    private void downloadDetect() throws IOException, InterruptedException, IntegrationException {
        logger.info("Downloading detect");
        final String detectScriptString = CmdExecutor.execCmd("curl -s https://blackducksoftware.github.io/hub-detect/hub-detect.sh", null, 5, null);
        FileUtils.writeStringToFile(new File("/tmp/detect.sh"), detectScriptString, StandardCharsets.UTF_8);
        logger.info("Making detect executable");
        CmdExecutor.execCmd("chmod +x /tmp/detect.sh", null, 5, null);
    }
}
