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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProgramVersion {
    private final Logger logger = LoggerFactory.getLogger(ProgramVersion.class);
    private String programVersion;

    public String getProgramVersion() throws IOException {
        if (programVersion == null) {
            final Properties props = new Properties();
            final InputStream stream = this.getClass().getClassLoader().getResourceAsStream("version.properties");
            props.load(stream);
            programVersion = props.getProperty("program.version");
            logger.debug(String.format("programVersion: %s", programVersion));
        }
        return programVersion;
    }
}
