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
package com.blackducksoftware.integration.hub.detectws.execute.fromdetect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;

public class ExecutableStreamThread extends Thread {
    private final BufferedReader bufferedReader;
    private final StringBuilder stringBuilder;
    private final Logger logger;

    private String executableOutput;

    public ExecutableStreamThread(final InputStream executableStream, final Logger logger) {
        super("Executable Stream Thread");
        this.logger = logger;
        final InputStreamReader reader = new InputStreamReader(executableStream, StandardCharsets.UTF_8);
        this.bufferedReader = new BufferedReader(reader);
        this.stringBuilder = new StringBuilder();
    }

    @Override
    public void run() {
        try {
            String line;
            final String separator = System.lineSeparator();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + separator);
                logger.info(line);
            }
        } catch (final IOException e) {
            // Ignore
            logger.trace(e.toString());
        }
        this.executableOutput = stringBuilder.toString();
    }

    public String getExecutableOutput() {
    		return executableOutput;
    }

}
