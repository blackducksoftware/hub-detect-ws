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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DetectServiceController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // Endpoints
    private static final String SCAN_INSPECT_IMAGE = "/scaninspectimage";
    private static final String READY_TO_SCANINSPECT = "/ready";

    @Autowired
    private DetectServiceHandler imageInspectorHandler;

    @RequestMapping(path = SCAN_INSPECT_IMAGE, method = RequestMethod.POST)
    public ResponseEntity<String> scanImage(final HttpServletRequest request, @RequestParam final Map<String, String> requestParams) {
        logger.info(String.format("Endpoing %s called:", SCAN_INSPECT_IMAGE));
        requestParams.forEach((key, value) -> logger.info(String.format("\t%s=%s", key, value)));
        return imageInspectorHandler.scanImage(request.getScheme(), request.getServerName(), request.getServerPort(), request.getRequestURI(), requestParams);
    }

    @RequestMapping(path = READY_TO_SCANINSPECT, method = RequestMethod.GET)
    public ResponseEntity<String> ready(final HttpServletRequest request) {
        logger.info(String.format("Endpoing %s called:", READY_TO_SCANINSPECT));
        return imageInspectorHandler.ready(request.getScheme(), request.getServerName(), request.getServerPort(), request.getRequestURI());
    }
}
