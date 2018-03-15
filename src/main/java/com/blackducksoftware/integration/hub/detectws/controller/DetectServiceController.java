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

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DetectServiceController {
    // Endpoint
    private static final String SCAN_INSPECT_IMAGE = "/scaninspectimage";
    private static final String READY_TO_SCANINSPECT = "/ready";
    // Mandatory query param
    static final String TARFILE_PATH_QUERY_PARAM = "tarfile";
    // Optional query params
    static final String HUB_PROJECT_NAME_QUERY_PARAM = "hubprojectname";
    static final String HUB_PROJECT_VERSION_QUERY_PARAM = "hubprojectversion";
    static final String CODELOCATION_PREFIX_QUERY_PARAM = "codelocationprefix";
    static final String CLEANUP_WORKING_DIR_QUERY_PARAM = "cleanup";

    @Autowired
    private DetectServiceHandler imageInspectorHandler;

    @RequestMapping(path = SCAN_INSPECT_IMAGE, method = RequestMethod.POST)
    public ResponseEntity<String> scanImage(final HttpServletRequest request, @RequestParam(value = TARFILE_PATH_QUERY_PARAM) final String tarFilePath,
            @RequestParam(value = HUB_PROJECT_NAME_QUERY_PARAM, defaultValue = "") final String hubProjectName, @RequestParam(value = HUB_PROJECT_VERSION_QUERY_PARAM, defaultValue = "") final String hubProjectVersion,
            @RequestParam(value = CODELOCATION_PREFIX_QUERY_PARAM, defaultValue = "") final String codeLocationPrefix,
            @RequestParam(value = CLEANUP_WORKING_DIR_QUERY_PARAM, required = false, defaultValue = "true") final boolean cleanupWorkingDir) {
        return imageInspectorHandler.scanImage(request.getScheme(), request.getServerName(), request.getServerPort(), request.getRequestURI(), tarFilePath, hubProjectName, hubProjectVersion, codeLocationPrefix, cleanupWorkingDir);
    }

    @RequestMapping(path = READY_TO_SCANINSPECT, method = RequestMethod.GET)
    public ResponseEntity<String> ready(final HttpServletRequest request) {
        return imageInspectorHandler.ready(request.getScheme(), request.getServerName(), request.getServerPort(), request.getRequestURI());
    }
}
