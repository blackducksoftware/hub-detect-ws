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

import java.util.Arrays;
import java.util.List;

public class ExecutableOutput {
    private final String standardOutput;
    private final String errorOutput;

    public ExecutableOutput(final String standardOutput, final String errorOutput) {
        this.standardOutput = standardOutput;
        this.errorOutput = errorOutput;
    }

    public List<String> getStandardOutputAsList() {
        return Arrays.asList(standardOutput.split(System.lineSeparator()));
    }

    public List<String> getErrorOutputAsList() {
        return Arrays.asList(errorOutput.split(System.lineSeparator()));
    }
}
