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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncCmdExecutor implements Callable<String> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final File workingDir;
    private final Map<String, String> environmentVariables;
    private final String exePath;
    private final List<String> args;

    public AsyncCmdExecutor(final File workingDir, final Map<String, String> environmentVariables, final String exePath, final List<String> args) {
        this.workingDir = workingDir;
        if (environmentVariables == null) {
            this.environmentVariables = new HashMap<>();
        } else {
            this.environmentVariables = environmentVariables;
        }
        this.exePath = exePath;
        this.args = args;
    }

    @Override
    public String call() throws Exception {
        return SimpleExecutor.execute(workingDir, environmentVariables, exePath, args);

        // final Executable executor = new Executable(new File("."), environmentVariables, exePath, args);
        // final ExecutableRunner runner = new ExecutableRunner();
        // final ExecutableOutput out = runner.execute(executor);
        // final List<String> stderrList = out.getErrorOutputAsList();
        // final List<String> stdout = out.getStandardOutputAsList();
        //
        // final String argsString = StringUtils.join(args, ' ');
        // final String stderrString = StringUtils.join(stderrList, '\n');
        // final String stdoutString = StringUtils.join(stdout, '\n');
        // logger.info(String.format("Command: '%s %s'; Output: %s; stderr: %s", exePath, argsString, stdoutString, stderrString));
        // return stdoutString;
    }

}
