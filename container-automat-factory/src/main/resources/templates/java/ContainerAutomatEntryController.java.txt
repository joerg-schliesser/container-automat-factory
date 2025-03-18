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
package de.containerautomat.api;

import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.ContainerAutomatMessaging;
import de.containerautomat.processing.ContainerAutomatProcessingInstance;
import de.containerautomat.processing.ContainerAutomatRequest;
import de.containerautomat.processing.ContainerAutomatStorage;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeCommand;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * A Spring REST controller for receiving requests to be processed by the
 * DFA implemented by the generated application.
 */
@RestController
@ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_ENTRY)
@OpenAPIDefinition(
        info = @Info(
                title = "ContainerAutomat Entry Controller",
                version = "1.0",
                description = "The REST API of a ContainerAutomat application."))
@RequiredArgsConstructor
public class ContainerAutomatEntryController extends ContainerAutomatControllerBase {

    private static final Log log = LogFactory.getLog(ContainerAutomatEntryController.class);

    public static final String PATH_REQUESTS = "/requests";

    static final String LOG_MESSAGE_NEW_REQUEST_PROCESSING_INSTANCE = "Processing new request. ProcessingInstance created:%n%s";

    private final DeterministicFiniteAutomaton automaton;

    private final ContainerAutomatStorage storage;

    private final ContainerAutomatMessaging messaging;


    @PostMapping(path = PATH_REQUESTS, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContainerAutomatProcessingInstance> processRequest(@RequestBody @Valid ContainerAutomatRequest containerAutomatRequest) {

        var processingInstance = storage.createProcessingInstance(containerAutomatRequest);
        logProcessingInstanceCreated(processingInstance);
        var processingCommand = ContainerAutomatRuntimeCommand.fromProcessingInstance(processingInstance);
        messaging.sendContainerAutomatCommand(automaton.getStartState(), processingCommand);
        return ResponseEntity.ok(processingInstance);
    }

    protected void logProcessingInstanceCreated(ContainerAutomatProcessingInstance containerAutomatProcessingInstance) {

        log.info(LOG_MESSAGE_NEW_REQUEST_PROCESSING_INSTANCE.formatted(containerAutomatProcessingInstance.toString()));
    }

}
