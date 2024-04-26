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
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingContinuation;
import lombok.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * A test suite for the class {@link ContainerAutomatRuntimeProcessor}
 * for processing objects of type {@link ContainerAutomatCommand}.
 */
@JsonTest
@ContextConfiguration(classes = ContainerAutomatCoreConfig.class)
@TestPropertySource(properties = {
        ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE + "=true",
        ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_STATE_NAME + "=" + ContainerAutomatRuntimeProcessorTests.TEST_STATE_NAME,
        ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_DFA_RESOURCE_PATH + "=/test-dfa.json"
})
class ContainerAutomatRuntimeProcessorTests {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @EqualsAndHashCode
    static class ProcessingStepImpl implements ContainerAutomatProcessingStep {

        private String processingStepId;
        private String processingInstanceId;
        private int processingPosition;
        private String inputSymbol;
        private String stateName;
        private Instant startTime;
        private Instant endTime;
        private StepResult stepResult;
        private String description;
    }


    static final String TEST_STATE_NAME = "S1";
    private static final String TEST_ACCEPT_STATE_NAME = "S1";
    private static final String TEST_REJECT_STATE_NAME = "S2";
    private static final String TEST_PROCESSING_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String TEST_ACCEPTED_PROCESSING_INPUT = "1010";
    private static final String TEST_REJECTED_PROCESSING_INPUT = "10100";
    private static final String TEST_INVALID_PROCESSING_INPUT = "1a10";
    private static final int TEST_PROCESSING_POSITION = 1;
    private static final String TEST_INPUT_SYMBOL = Character.toString(TEST_ACCEPTED_PROCESSING_INPUT.charAt(TEST_PROCESSING_POSITION));
    private static final String TEST_EVENT_DESCRIPTION = "Test event for ContainerAutomatRuntimeProcessor.";
    private static final String TEST_DFA_DESCRIPTION = "Test version of DFA for checking input of an even number of zeros.";
    private static final String TEST_PROCESSING_MESSAGE = "Test processing for ContainerAutomatRuntimeProcessor.";
    private static final String TEST_WORK_RESULT_DESCRIPTION = "Test work result for ContainerAutomatRuntimeProcessor.";


    @MockBean
    ContainerAutomatStorage storage;

    @MockBean
    ContainerAutomatMessaging messaging;

    @Autowired
    ContainerAutomatRuntimeProcessor runtimeProcessor;

    @Autowired
    DeterministicFiniteAutomaton dfa;


    @Test
    void runtime_processor_is_available() {

        assertNotNull(runtimeProcessor);
    }

    @Test
    void dfa_is_available_and_test_version_is_used() {

        assertNotNull(dfa);
        assertEquals(TEST_DFA_DESCRIPTION, dfa.getDescription());
    }

    @Test
    void result_of_successful_symbol_processing() {

        var testCommand = createTestCommand();
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();
        var testStep = createTestStep(testEvent);
        var testWorkResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatWorkResult(TEST_WORK_RESULT_DESCRIPTION, 1000);

        Mockito.doNothing().when(messaging).sendContainerAutomatEvent(isA(ContainerAutomatEvent.class));
        Mockito.doNothing().when(messaging).sendContainerAutomatCommand(isA(String.class), isA(ContainerAutomatCommand.class));
        Mockito.when(storage.createProcessingStep(isA(Instant.class), isA(ContainerAutomatEvent.class))).thenReturn(testStep);
        var testProcessor = Mockito.spy(runtimeProcessor);

        var result = assertDoesNotThrow(() -> testProcessor.processCommand(testCommand, command -> testWorkResult));

        Mockito.verify(testProcessor, Mockito.times(1)).logCommandProcessingStart(testCommand);
        Mockito.verify(testProcessor, Mockito.times(2)).sendCommandProcessingEvent(eq(testCommand), isA(EventType.class), isA(String.class), any(String.class), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));
        Mockito.verify(testProcessor, Mockito.times(0)).sendInputProcessingFinishedEvent(any(ContainerAutomatCommand.class), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));
        Mockito.verify(testProcessor, Mockito.times(1)).sendProcessingContinuesEvent(eq(testCommand), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));
        Mockito.verify(testProcessor, Mockito.times(1)).sendNextCommand(testCommand);
        Mockito.verify(testProcessor, Mockito.times(1)).storeProcessingStep(isA(Instant.class), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));

        assertNotNull(result);
        assertNotNull(result.getProcessedCommand());
        assertSame(testCommand, result.getProcessedCommand());
        assertNotNull(result.getWorkResult());
        assertSame(testWorkResult, result.getWorkResult());
        assertNotNull(result.getLastEvent());
        assertEventHasTestValuesAndTimeWithinRange(testEvent, result.getLastEvent(), true);
        assertTrue(result.isLastEventSent());
        assertNotNull(result.getProcessingStep());
        assertSame(testStep, result.getProcessingStep());
        assertNull(result.getError());
    }

    @Test
    void result_of_invalid_input_processing() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_INVALID_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .build();
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(EventType.STATE_PROCESSING_ERROR)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_INVALID_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();
        var testStep = createTestStep(testEvent);
        var testWorkResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatWorkResult(TEST_WORK_RESULT_DESCRIPTION, 1000);
        var testErrorMessage = DeterministicFiniteAutomaton.ERROR_MESSAGE_NO_TRANSITION_TEMPLATE.formatted(TEST_STATE_NAME, Character.toString(TEST_INVALID_PROCESSING_INPUT.charAt(TEST_PROCESSING_POSITION)));

        Mockito.doNothing().when(messaging).sendContainerAutomatEvent(isA(ContainerAutomatEvent.class));
        Mockito.doThrow(new IllegalArgumentException("Sending next command not expected for invalid input.")).when(messaging).sendContainerAutomatCommand(isA(String.class), isA(ContainerAutomatCommand.class));
        Mockito.when(storage.createProcessingStep(isA(Instant.class), isA(ContainerAutomatEvent.class))).thenReturn(testStep);
        var testProcessor = Mockito.spy(runtimeProcessor);

        var result = assertDoesNotThrow(() -> testProcessor.processCommand(testCommand, command -> testWorkResult));

        Mockito.verify(testProcessor, Mockito.times(1)).logCommandProcessingStart(testCommand);
        Mockito.verify(testProcessor, Mockito.times(2)).sendCommandProcessingEvent(eq(testCommand), isA(EventType.class), isA(String.class), any(String.class), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));
        Mockito.verify(testProcessor, Mockito.times(0)).sendInputProcessingFinishedEvent(any(ContainerAutomatCommand.class), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));
        Mockito.verify(testProcessor, Mockito.times(0)).sendProcessingContinuesEvent(eq(testCommand), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));
        Mockito.verify(testProcessor, Mockito.times(1)).sendNextCommand(testCommand);
        Mockito.verify(testProcessor, Mockito.times(1)).storeProcessingStep(isA(Instant.class), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));

        assertNotNull(result);
        assertNotNull(result.getProcessedCommand());
        assertSame(testCommand, result.getProcessedCommand());
        assertNotNull(result.getWorkResult());
        assertSame(testWorkResult, result.getWorkResult());
        assertNotNull(result.getLastEvent());
        assertEventHasTestValuesAndTimeWithinRange(testEvent, result.getLastEvent(), true);
        assertTrue(result.isLastEventSent());
        assertNotNull(result.getProcessingStep());
        assertSame(testStep, result.getProcessingStep());
        assertNotNull(result.getError());
        assertEquals(testErrorMessage, result.getError().getMessage());
    }

    @Test
    void result_of_successful_processing_finalization() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_ACCEPTED_PROCESSING_INPUT.length())
                .build();
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(EventType.STATE_PROCESSING_FINISHED_INPUT_ACCEPTED)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_ACCEPTED_PROCESSING_INPUT.length())
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();
        var testStep = createTestStep(testEvent);
        var testWorkResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatWorkResult(TEST_WORK_RESULT_DESCRIPTION, 1000);

        Mockito.doNothing().when(messaging).sendContainerAutomatEvent(isA(ContainerAutomatEvent.class));
        Mockito.doThrow(new IllegalArgumentException("Sending next command not expected during finalization of processing.")).when(messaging).sendContainerAutomatCommand(isA(String.class), isA(ContainerAutomatCommand.class));
        Mockito.when(storage.createProcessingStep(isA(Instant.class), isA(ContainerAutomatEvent.class))).thenReturn(testStep);
        var testProcessor = Mockito.spy(runtimeProcessor);

        var result = assertDoesNotThrow(() -> testProcessor.processCommand(testCommand, command -> testWorkResult));

        Mockito.verify(testProcessor, Mockito.times(1)).logCommandProcessingStart(testCommand);
        Mockito.verify(testProcessor, Mockito.times(2)).sendCommandProcessingEvent(eq(testCommand), isA(EventType.class), isA(String.class), any(String.class), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));
        Mockito.verify(testProcessor, Mockito.times(1)).sendInputProcessingFinishedEvent(any(ContainerAutomatCommand.class), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));
        Mockito.verify(testProcessor, Mockito.times(0)).sendProcessingContinuesEvent(eq(testCommand), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));
        Mockito.verify(testProcessor, Mockito.times(0)).sendNextCommand(testCommand);
        Mockito.verify(testProcessor, Mockito.times(1)).storeProcessingStep(isA(Instant.class), isA(ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult.class));

        assertNotNull(result);
        assertNotNull(result.getProcessedCommand());
        assertSame(testCommand, result.getProcessedCommand());
        assertNotNull(result.getWorkResult());
        assertSame(testWorkResult, result.getWorkResult());
        assertNotNull(result.getLastEvent());
        assertEventHasTestValuesAndTimeWithinRange(testEvent, result.getLastEvent(), true);
        assertTrue(result.isLastEventSent());
        assertNotNull(result.getProcessingStep());
        assertSame(testStep, result.getProcessingStep());
        assertNull(result.getError());
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void log_message_of_symbol_processing_start(CapturedOutput output) {

        var testCommand = createTestCommand();
        var testMessage = ContainerAutomatRuntimeProcessor.LOG_MESSAGE_PROCESSING_SYMBOL_AT_POSITION_TEMPLATE.formatted(TEST_INPUT_SYMBOL, TEST_PROCESSING_POSITION, TEST_PROCESSING_INSTANCE_ID, TEST_ACCEPTED_PROCESSING_INPUT);

        runtimeProcessor.logCommandProcessingStart(testCommand);

        assertTrue(output.getOut().contains(testMessage));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void log_message_of_finalizing_processing_start(CapturedOutput output) {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_ACCEPTED_PROCESSING_INPUT.length())
                .build();
        var testMessage = ContainerAutomatRuntimeProcessor.LOG_MESSAGE_FINALIZING_PROCESSING_TEMPLATE.formatted(TEST_PROCESSING_INSTANCE_ID, TEST_ACCEPTED_PROCESSING_INPUT, TEST_ACCEPTED_PROCESSING_INPUT.length());

        runtimeProcessor.logCommandProcessingStart(testCommand);

        assertTrue(output.getOut().contains(testMessage));
    }

    @Test
    void send_event_for_processing_start() {

        var testCommand = createTestCommand();
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(EventType.STATE_PROCESSING_START)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();
        var testResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult(testCommand);

        runtimeProcessor.sendCommandProcessingEvent(testCommand, EventType.STATE_PROCESSING_START, TEST_STATE_NAME, TEST_EVENT_DESCRIPTION, testResult);

        assertEventHasTestValuesAndTimeWithinRange(testEvent, testResult.getLastEvent(), false);
    }

    @Test
    void send_event_for_continuation_with_input_processing() {

        var testCommand = createTestCommand();
        var testNextCommand = testCommand.nextCommand();
        var testTransisiton = dfa.getTransition(TEST_STATE_NAME, TEST_INPUT_SYMBOL);
        var testContinuation = new ContainerAutomatProcessingContinuation(testTransisiton.getSubsequentStateName(), testNextCommand);

        var testEventDescription = ContainerAutomatRuntimeProcessor.PROCESSING_MESSAGE_CONTINUATION_WITH_INPUT_TEMPLATE.formatted(TEST_ACCEPTED_PROCESSING_INPUT.charAt(TEST_PROCESSING_POSITION + 1), testContinuation.nextState(), TEST_PROCESSING_MESSAGE);
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(testEventDescription)
                .build();
        var testWorkResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatWorkResult(TEST_WORK_RESULT_DESCRIPTION, 1000);
        var testResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult(testCommand);
        testResult.setContinuation(testContinuation);
        testResult.setWorkResult(testWorkResult);

        runtimeProcessor.sendProcessingContinuesEvent(testCommand, testResult);

        assertEventHasTestValuesAndTimeWithinRange(testEvent, testResult.getLastEvent(), true);
    }

    @Test
    void send_event_for_continuation_with_processing_finalization() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_ACCEPTED_PROCESSING_INPUT.length() - 1)
                .build();
        var testNextCommand = testCommand.nextCommand();
        var testContinuation = new ContainerAutomatProcessingContinuation(TEST_ACCEPT_STATE_NAME, testNextCommand);
        var testEventDescription = ContainerAutomatRuntimeProcessor.PROCESSING_MESSAGE_CONTINUATION_WITH_FINALIZATION_TEMPLATE.formatted(TEST_ACCEPT_STATE_NAME, TEST_PROCESSING_MESSAGE);
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_ACCEPTED_PROCESSING_INPUT.length() - 1)
                .stateName(TEST_STATE_NAME)
                .description(testEventDescription)
                .build();
        var testWorkResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatWorkResult(TEST_WORK_RESULT_DESCRIPTION, 1000);
        var testResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult(testCommand);
        testResult.setContinuation(testContinuation);
        testResult.setWorkResult(testWorkResult);

        runtimeProcessor.sendProcessingContinuesEvent(testCommand, testResult);

        assertEventHasTestValuesAndTimeWithinRange(testEvent, testResult.getLastEvent(), true);
    }

    @Test
    void send_event_for_finalization_with_accept() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_ACCEPTED_PROCESSING_INPUT.length())
                .build();
        var testEventDescription = ContainerAutomatRuntimeProcessor.PROCESSING_MESSAGE_FINALIZATION_WITH_ACCEPT_TEMPLATE.formatted(TEST_PROCESSING_MESSAGE);
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(EventType.STATE_PROCESSING_FINISHED_INPUT_ACCEPTED)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_ACCEPTED_PROCESSING_INPUT.length())
                .stateName(TEST_ACCEPT_STATE_NAME)
                .description(testEventDescription)
                .build();
        var testWorkResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatWorkResult(TEST_WORK_RESULT_DESCRIPTION, 1000);
        var testResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult(testCommand);
        testResult.setWorkResult(testWorkResult);

        runtimeProcessor.sendInputProcessingFinishedEvent(testCommand, testResult);

        assertEventHasTestValuesAndTimeWithinRange(testEvent, testResult.getLastEvent(), true);
    }

    @Test
    void send_event_for_finalization_with_reject() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_REJECTED_PROCESSING_INPUT)
                .processingPosition(TEST_REJECTED_PROCESSING_INPUT.length())
                .build();
        var testEventDescription = ContainerAutomatRuntimeProcessor.PROCESSING_MESSAGE_FINALIZATION_WITH_REJECT_TEMPLATE.formatted(TEST_PROCESSING_MESSAGE);
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(EventType.STATE_PROCESSING_FINISHED_INPUT_REJECTED)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_REJECTED_PROCESSING_INPUT)
                .processingPosition(TEST_REJECTED_PROCESSING_INPUT.length())
                .stateName(TEST_REJECT_STATE_NAME)
                .description(testEventDescription)
                .build();
        var testProcessor = new ContainerAutomatRuntimeProcessor(dfa, TEST_REJECT_STATE_NAME, messaging, storage);
        var testWorkResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatWorkResult(TEST_WORK_RESULT_DESCRIPTION, 1000);
        var testResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult(testCommand);
        testResult.setWorkResult(testWorkResult);

        testProcessor.sendInputProcessingFinishedEvent(testCommand, testResult);

        assertEventHasTestValuesAndTimeWithinRange(testEvent, testResult.getLastEvent(), true);
    }

    @Test
    void process_continuation_of_command() {

        var testCommand = createTestCommand();
        var testNextCommand = testCommand.nextCommand();
        var testTransisiton = dfa.getTransition(TEST_STATE_NAME, TEST_INPUT_SYMBOL);

        Mockito.doNothing().when(messaging).sendContainerAutomatCommand(isA(String.class), isA(ContainerAutomatCommand.class));

        var continuation = runtimeProcessor.sendNextCommand(testCommand);
        assertNotNull(continuation);
        assertEquals(testTransisiton.getSubsequentStateName(), continuation.nextState());
        assertEquals(testNextCommand.getProcessingInput(), continuation.nextCommand().getProcessingInput());
        assertEquals(testNextCommand.getProcessingPosition(), continuation.nextCommand().getProcessingPosition());
        assertEquals(testNextCommand.getProcessingInstanceId(), continuation.nextCommand().getProcessingInstanceId());
    }

    @Test
    void create_step_for_event_without_error() {

        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();
        var testStep = createTestStep(testEvent);
        var testResult = new ContainerAutomatRuntimeProcessor.ContainerAutomatProcessingResult(createTestCommand());
        testResult.setLastEvent(testEvent);
        testResult.setLastEventSent(true);

        Mockito.when(storage.createProcessingStep(testStep.getStartTime(), testEvent)).thenReturn(testStep);

        runtimeProcessor.storeProcessingStep(testStep.getStartTime(), testResult);
        assertNotNull(testResult.getProcessingStep());
        assertSame(testStep, testResult.getProcessingStep());
        assertNull(testResult.getError());
    }

    private void assertEventHasTestValuesAndTimeWithinRange(ContainerAutomatEvent expected, ContainerAutomatEvent actual, boolean ignoreDescription) {

        assertEquals(expected.getEventType(), actual.getEventType());
        assertEquals(expected.getProcessingInstanceId(), actual.getProcessingInstanceId());
        assertEquals(expected.getProcessingInput(), actual.getProcessingInput());
        assertEquals(expected.getProcessingPosition(), actual.getProcessingPosition());
        assertEquals(expected.getStateName(), actual.getStateName());
        if (!ignoreDescription) {
            assertEquals(expected.getDescription(), actual.getDescription());
        }

        assertTrue(expected.getEventTime().compareTo(actual.getEventTime()) <= 0);
        assertTrue(Instant.now().compareTo(actual.getEventTime()) >= 0);
    }

    private ContainerAutomatCommand createTestCommand() {

        return ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .build();
    }

    private ContainerAutomatProcessingStep createTestStep(ContainerAutomatEvent event) {

        return ProcessingStepImpl.builder()
                .processingStepId(UUID.randomUUID().toString())
                .processingInstanceId(event.getProcessingInstanceId())
                .processingPosition(event.getProcessingPosition())
                .inputSymbol(event.currentInputSymbol().orElse(""))
                .stateName(event.getStateName())
                .startTime(event.getEventTime().minusMillis(1000))
                .endTime(event.getEventTime())
                .stepResult(ContainerAutomatProcessingStep.createStepResultFromEvent(event.getEventType()))
                .description(event.getDescription())
                .build();
    }

}
