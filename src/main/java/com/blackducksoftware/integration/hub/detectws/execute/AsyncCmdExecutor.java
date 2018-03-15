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

import com.blackducksoftware.integration.hub.detectws.state.ReadyDao;

public class AsyncCmdExecutor implements Callable<String> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ReadyDao readyDao;
    private final File workingDir;
    private final Map<String, String> environmentVariables;
    private final String exePath;
    private final List<String> args;

    public AsyncCmdExecutor(final ReadyDao readyDao, final File workingDir, final Map<String, String> environmentVariables, final String exePath, final List<String> args) {
        this.readyDao = readyDao;
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
        logger.info(String.format("******************* call(); thread name: %s", Thread.currentThread().getName()));
        if (readyDao == null) {
            logger.info(String.format("******************* call(): readyDao is null!!!!!!!!!!!!!!!!!!!"));
        }
        logger.info(String.format("******************* call(): setting ready to false"));
        readyDao.setReady(false); // TODO if I comment both calls to readyDao, it works
        logger.info(String.format("******************* call(): calling SimpleExecutor.execute"));
        final String stdout = SimpleExecutor.execute(workingDir, environmentVariables, exePath, args);
        logger.info(String.format("******************* call(): SimpleExecutor.execute returned"));
        readyDao.setReady(true);
        return stdout;
    }

}
