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

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncCmdExecutor implements Callable<String> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String cmd;
    private final String stdinString;
    private final long timeoutSeconds;
    private final Map<String, String> env;

    public AsyncCmdExecutor(final String cmd, final String stdinString, final long timeoutSeconds, final Map<String, String> env) {
        this.cmd = cmd;
        this.stdinString = stdinString;
        this.timeoutSeconds = timeoutSeconds;
        this.env = env;
    }

    @Override
    public String call() throws Exception {
        final String cmdOutput = CmdExecutor.execCmd(cmd, stdinString, timeoutSeconds, env);
        logger.info(String.format("Command: '%s'; Output: %s", cmd, cmdOutput));
        return cmdOutput;
    }

}
