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
package com.blackducksoftware.integration.hub.detectws.execute;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.detectws.execute.fromdetect.Executable;
import com.blackducksoftware.integration.hub.detectws.execute.fromdetect.ExecutableOutput;
import com.blackducksoftware.integration.hub.detectws.execute.fromdetect.ExecutableRunner;
import com.blackducksoftware.integration.hub.detectws.execute.fromdetect.ExecutableRunnerException;

public class SimpleExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SimpleExecutor.class);

    public static String execute(final File workingDir, final Map<String, String> environmentVariables, final String exePath, final List<String> args) throws ExecutableRunnerException {
        final Executable executor = new Executable(workingDir, environmentVariables, exePath, args);
        final ExecutableRunner runner = new ExecutableRunner();
        final ExecutableOutput out = runner.execute(executor);
        final List<String> stderrList = out.getErrorOutputAsList();
        final List<String> stdout = out.getStandardOutputAsList();

        final String argsString = StringUtils.join(args, ' ');
        final String stderrString = StringUtils.join(stderrList, '\n');
        final String stdoutString = StringUtils.join(stdout, '\n');
        logger.info(String.format("Command: '%s %s'; Output: %s; stderr: %s", exePath, argsString, stdoutString, stderrString));
        return stdoutString;
    }
}
