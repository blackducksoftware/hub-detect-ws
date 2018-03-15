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
package com.blackducksoftware.integration.hub.detectws.state;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.detectws.controller.DetectServiceAction;

@Component
public class ReadyDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String READY_FILE_PATH = String.format("%s/ready.txt", DetectServiceAction.PGM_DIR);
    private static final File readyFile = new File(READY_FILE_PATH);

    public boolean isReady() {
        synchronized (this) {
            if (!readyFile.canRead()) {
                return false;
            }
            try {
                final String readyString = FileUtils.readFileToString(readyFile, StandardCharsets.UTF_8);
                if ("true".equals(readyString)) {
                    return true;
                } else {
                    return false;
                }
            } catch (final IOException e) {
                logger.warn(String.format("Error reading ready state file: %s", READY_FILE_PATH));
                return false;
            }
        }
    }

    public void setReady(final Boolean ready) throws IOException {
        logger.info(String.format("Waiting for lock to set Application Ready state to %b", ready));
        synchronized (this) {
            logger.info(String.format("Setting Application Ready state to %s", ready.toString()));
            FileUtils.write(readyFile, ready.toString(), StandardCharsets.UTF_8);
        }
    }

}
