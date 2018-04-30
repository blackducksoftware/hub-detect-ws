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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseFactory {

    public ResponseEntity<String> createResponseAccepted(final String bdio) {
        return new ResponseEntity<>(bdio, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<String> createResponse(final HttpStatus status, final String warning) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Warning", warning);
        return new ResponseEntity<>(null, headers, status);
    }

    public ResponseEntity<String> createRedirect(final String newUrl, final String warning) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", newUrl);
        headers.add("Warning", warning);
        return new ResponseEntity<>(null, headers, HttpStatus.FOUND);
    }
}
