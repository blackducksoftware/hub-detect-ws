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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.detectws.app.exception.ServiceIsBusyException;

@Component
public class DetectServiceHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DetectServiceAction imageInspectorAction;

    @Autowired
    private ResponseFactory responseFactory;

    public ResponseEntity<String> scanImage(final String scheme, final String host, final int port, final String requestUri, final Map<String, String> requestParams) {
        try {
            final String bdio = imageInspectorAction.scanImage(requestParams);
            return responseFactory.createResponseAccepted(bdio);
        } catch (final ServiceIsBusyException e) {
            logger.error(e.getMessage());
            return responseFactory.createResponse(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        } catch (final Exception e) {
            logger.error(String.format("Exception thrown while getting image packages: %s", e.getMessage()), e);
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<String> ready(final String scheme, final String host, final int port, final String requestUri) {
        logger.info("readiness check");
        try {
            final boolean readyResponse = imageInspectorAction.ready();
            if (readyResponse) {
                return responseFactory.createResponse(HttpStatus.OK, "");
            } else {
                return responseFactory.createResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service is busy");
            }
        } catch (final Exception e) {
            logger.error(String.format("Exception thrown while getting image packages: %s", e.getMessage()), e);
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
