/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.containerautomat.factory.api;

import de.containerautomat.api.ContainerAutomatControllerBase;
import de.containerautomat.factory.builders.DfaApplicationBuilder;
import de.containerautomat.factory.outlets.ZiparchiveOutlet;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * A Spring REST controller for receiving requests to generate a Container-Automat
 * application based on the description of a Deterministic Finite Automaton (DFA).
 */
@RestController
@OpenAPIDefinition(
        info = @Info(
                title = "Container-Automat Factory",
                version = "1.0",
                description = "The REST API of the Container-Automat factory.",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(url = "https://www.container-automat.de", name = "Container-Automat")),
        tags = {
                @Tag(
                        name = "Container-Automat",
                        description = "Container-Automat is a generator of service-oriented applications for demonstration purposes that are based on Deterministic Finite Automata (abbreviated DFA).",
                        externalDocs = @ExternalDocumentation(description = "www.container-automat.de")),
                @Tag(
                        name = "DFA",
                        description = "Deterministic Finite Automaton."),
                @Tag(
                        name = "DEA",
                        description = "Deterministischer Endlicher Automat.")
        },
        externalDocs = @ExternalDocumentation(description = "www.container-automat.de"))
public class ContainerAutomatFactoryController extends ContainerAutomatControllerBase {

    private static final Log log = LogFactory.getLog(ContainerAutomatFactoryController.class);

    static final String VERSION_PREFIX = "/v1";
    static final String PATH_APPS_CREATE = VERSION_PREFIX + "/apps/create";

    @PostMapping(path = PATH_APPS_CREATE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> generateContainerAutomatApp(@RequestBody @Valid DfaApplicationParameters dfaApplicationParameters) {

        var appName = dfaApplicationParameters.getApplicationMetaData().getAppName();
        var dfaDescription = dfaApplicationParameters.getDfa().getDescription();
        log.info("Request to generate Container-Automat application %s from DFA. Description: %s".formatted(appName, dfaDescription));

        var ziparchiveOutlet = new ZiparchiveOutlet();
        var applicationBuilder = new DfaApplicationBuilder(dfaApplicationParameters, ziparchiveOutlet);
        applicationBuilder.createApplicationFiles();
        var result = ziparchiveOutlet.getZipArchive();

        var responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dfaApplicationParameters.getApplicationMetaData().getAppName().toLowerCase() + ".zip\"");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok().headers(responseHeaders).body(result);
    }

}
