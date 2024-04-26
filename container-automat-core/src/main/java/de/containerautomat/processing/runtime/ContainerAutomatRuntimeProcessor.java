/*
 * Copyright 2024 the original author or authors.
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
package de.containerautomat.processing.runtime;

import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.*;
import de.containerautomat.processing.ContainerAutomatEvent.EventType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.function.Function;

/**
 * A service class for processing objects of type {@link ContainerAutomatCommand}.
 * The class is used in those services of the generated application that
 * represent states of the underlying DFA.
 * <p/>
 * Implementations of {@link ContainerAutomatCommandProcessor#processCommand(ContainerAutomatCommand)}
 * delegate to the {@link #processCommand(ContainerAutomatCommand, Function)}
 * method of this class.
 * <p/>
 * A reference to {@link ContainerAutomatWorkSimulator#simulateWork(ContainerAutomatCommand)}
 * is passed as a function to simulate application-specific processing
 * logic that takes some time and whose duration depends to some extent
 * on chance.
 */
@Service
@ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE)
public class ContainerAutomatRuntimeProcessor {

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class ContainerAutomatProcessingResult {

        @NonNull
        private final ContainerAutomatCommand processedCommand;

        private ContainerAutomatWorkResult workResult;

        private ContainerAutomatProcessingContinuation continuation;

        private ContainerAutomatEvent lastEvent;

        private boolean lastEventSent;

        private ContainerAutomatProcessingStep processingStep;

        private Exception error;

    }

    public record ContainerAutomatWorkResult(String description, long durationMillis) {
    }

    public record ContainerAutomatProcessingContinuation(String nextState, ContainerAutomatCommand nextCommand) {
    }


    private static final Log log = LogFactory.getLog(ContainerAutomatRuntimeProcessor.class);

    static final String LOG_MESSAGE_FINALIZING_PROCESSING_TEMPLATE = "Finalizing the processing of instance %s input %s at position %d.";
    static final String LOG_MESSAGE_PROCESSING_SYMBOL_AT_POSITION_TEMPLATE = "Processing input symbol %s at position %d of instance %s input %s.";
    static final String PROCESSING_MESSAGE_CONTINUATION_WITH_FINALIZATION_TEMPLATE = "Processing continues with finalization at final state %s. Processing message: %s";
    static final String PROCESSING_MESSAGE_CONTINUATION_WITH_INPUT_TEMPLATE = "Processing continues with input symbol %s at state %s. Processing message: %s";
    static final String PROCESSING_MESSAGE_FINALIZATION_WITH_ACCEPT_TEMPLATE = "Instance input accepted. Processing message: %s";
    static final String PROCESSING_MESSAGE_FINALIZATION_WITH_REJECT_TEMPLATE = "Instance input rejected. Processing message: %s";
    static final String PROCESSING_MESSAGE_AMBIGUOUS_SITUATION_ERROR = "Ambiguous processing situation. Error event explicitly created for processing step because of missing event information.";


    private final DeterministicFiniteAutomaton automaton;

    private final String stateName;

    private final ContainerAutomatMessaging messaging;

    private final ContainerAutomatStorage storage;


    public ContainerAutomatRuntimeProcessor(DeterministicFiniteAutomaton automaton, @Value("${" + ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_STATE_NAME + ":}") String stateName, ContainerAutomatMessaging messaging, ContainerAutomatStorage storage) {

        if (automaton.getState(stateName) == null) {
            throw new IllegalArgumentException("State %s is not part of the automaton.".formatted(stateName));
        }

        this.automaton = automaton;
        this.stateName = stateName;
        this.messaging = messaging;
        this.storage = storage;
    }

    public ContainerAutomatProcessingResult processCommand(ContainerAutomatCommand containerAutomatCommand, Function<ContainerAutomatCommand, ContainerAutomatWorkResult> worker) {

        var processingStart = Instant.now();
        var result = new ContainerAutomatProcessingResult(containerAutomatCommand);

        try {
            logCommandProcessingStart(containerAutomatCommand);
            sendCommandProcessingEvent(containerAutomatCommand, EventType.STATE_PROCESSING_START, stateName, "", result);
            result.setWorkResult(worker.apply(containerAutomatCommand));
            if (containerAutomatCommand.isProcessingEndCommand()) {
                sendInputProcessingFinishedEvent(containerAutomatCommand, result);
            } else {
                result.setContinuation(sendNextCommand(containerAutomatCommand));
                sendProcessingContinuesEvent(containerAutomatCommand, result);
            }
        } catch (Exception e) {
            result.setError(e);
            sendCommandProcessingEvent(containerAutomatCommand, EventType.STATE_PROCESSING_ERROR, stateName, StringUtils.hasText(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName(), result);
        } finally {
            storeProcessingStep(processingStart, result);
        }
        return result;
    }

    protected void logCommandProcessingStart(ContainerAutomatCommand containerAutomatCommand) {

        if (containerAutomatCommand.isProcessingEndCommand()) {
            log.info(LOG_MESSAGE_FINALIZING_PROCESSING_TEMPLATE.formatted(containerAutomatCommand.getProcessingInstanceId(), containerAutomatCommand.getProcessingInput(), containerAutomatCommand.getProcessingPosition()));
        } else {
            log.info(LOG_MESSAGE_PROCESSING_SYMBOL_AT_POSITION_TEMPLATE.formatted(containerAutomatCommand.currentInputSymbol().orElseThrow(), containerAutomatCommand.getProcessingPosition(), containerAutomatCommand.getProcessingInstanceId(), containerAutomatCommand.getProcessingInput()));
        }
    }

    protected void sendCommandProcessingEvent(ContainerAutomatCommand containerAutomatCommand, ContainerAutomatEvent.EventType eventType, String stateName, String eventDescription, ContainerAutomatProcessingResult result) {

        var event = ContainerAutomatRuntimeEvent.builder()
                .processingInstanceId(containerAutomatCommand.getProcessingInstanceId())
                .eventTime(Instant.now())
                .eventType(eventType)
                .stateName(stateName)
                .processingInput(containerAutomatCommand.getProcessingInput())
                .processingPosition(containerAutomatCommand.getProcessingPosition())
                .description(eventDescription)
                .build();

        result.setLastEvent(event);
        result.setLastEventSent(false);
        messaging.sendContainerAutomatEvent(event);
        result.setLastEventSent(true);
    }

    protected void sendInputProcessingFinishedEvent(ContainerAutomatCommand containerAutomatCommand, ContainerAutomatProcessingResult result) {

        if (automaton.getAcceptStates().contains(stateName)) {
            sendCommandProcessingEvent(containerAutomatCommand, EventType.STATE_PROCESSING_FINISHED_INPUT_ACCEPTED, stateName, PROCESSING_MESSAGE_FINALIZATION_WITH_ACCEPT_TEMPLATE.formatted(result.getWorkResult().description()), result);
            return;
        }
        sendCommandProcessingEvent(containerAutomatCommand, EventType.STATE_PROCESSING_FINISHED_INPUT_REJECTED, stateName, PROCESSING_MESSAGE_FINALIZATION_WITH_REJECT_TEMPLATE.formatted(result.getWorkResult().description()), result);
    }

    protected void sendProcessingContinuesEvent(ContainerAutomatCommand containerAutomatCommand, ContainerAutomatProcessingResult result) {

        var continuation = result.getContinuation();
        if (continuation.nextCommand().isProcessingEndCommand()) {
            sendCommandProcessingEvent(containerAutomatCommand, EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS, stateName, PROCESSING_MESSAGE_CONTINUATION_WITH_FINALIZATION_TEMPLATE.formatted(continuation.nextState(), result.getWorkResult().description()), result);
            return;
        }
        var nextInputSymbol = continuation.nextCommand().currentInputSymbol().orElseThrow();
        sendCommandProcessingEvent(containerAutomatCommand, EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS, stateName, PROCESSING_MESSAGE_CONTINUATION_WITH_INPUT_TEMPLATE.formatted(nextInputSymbol, continuation.nextState(), result.getWorkResult().description()), result);
    }

    protected ContainerAutomatProcessingContinuation sendNextCommand(ContainerAutomatCommand containerAutomatCommand) {

        var currentInputSymbol = containerAutomatCommand.currentInputSymbol().orElseThrow();
        var transition = automaton.getTransition(stateName, currentInputSymbol);
        var subsequentStateName = transition.getSubsequentStateName();
        var nextCommand = containerAutomatCommand.nextCommand();

        messaging.sendContainerAutomatCommand(subsequentStateName, nextCommand);
        return new ContainerAutomatProcessingContinuation(subsequentStateName, nextCommand);
    }

    protected void storeProcessingStep(Instant processingStart, ContainerAutomatProcessingResult result) {

        try {
            if (result.getLastEvent() == null) {
                sendCommandProcessingEvent(result.getProcessedCommand(), EventType.STATE_PROCESSING_ERROR, stateName, PROCESSING_MESSAGE_AMBIGUOUS_SITUATION_ERROR, result);
            }
            var processingStep = storage.createProcessingStep(processingStart, result.getLastEvent());
            result.setProcessingStep(processingStep);
        } catch (Exception e) {
            if (result.getError() == null) {
                result.setError(e);
                log.error("Unable to create processing step. Returning result with step creation error: %s. Error during step creation:".formatted(e.getMessage()), e);
            } else {
                log.error("Unable to create processing step. Returning result with original error: %s. Error during step creation:".formatted(result.getError().getMessage()), e);
            }
        }
    }

}
