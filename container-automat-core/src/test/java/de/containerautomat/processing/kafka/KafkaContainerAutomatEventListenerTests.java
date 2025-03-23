/*
 * Copyright 2025 the original author or authors.
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
package de.containerautomat.processing.kafka;

import de.containerautomat.processing.ContainerAutomatEvent;
import de.containerautomat.processing.ContainerAutomatEventListener;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test suite for testing the method in {@link KafkaContainerAutomatEventListener}
 * that handles events.
 * <p/>
 * Note: These tests are rudimentary, as application logic is only simulated in the
 * generated application. In addition, no integration tests for the messaging and
 * broker components are included at this level.
 */
@SpringBootTest(classes = KafkaContainerAutomatEventListener.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("kafka")
class KafkaContainerAutomatEventListenerTests {

    private static final String TEST_PROCESSING_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String TEST_ACCEPTED_PROCESSING_INPUT = "1010";
    private static final int TEST_PROCESSING_POSITION = 1;
    private static final String TEST_STATE_NAME = "S1";
    private static final String TEST_EVENT_DESCRIPTION = "Test event for ContainerAutomatRuntimeProcessor.";


    @Autowired
    ContainerAutomatEventListener containerAutomatEventListener;


    @Test
    void kafka_containerautomat_event_listener_bean_is_available() {

        assertNotNull(containerAutomatEventListener);
        assertInstanceOf(KafkaContainerAutomatEventListener.class, containerAutomatEventListener);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void handle_event_logs_event(CapturedOutput output) {

        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(ContainerAutomatEvent.EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();
        var testMessage = KafkaContainerAutomatEventListener.HANDLE_EVENT_LOG_MESSAGE_TEMPLATE.formatted(testEvent.toString());

        containerAutomatEventListener.handleEvent(testEvent);

        assertTrue(output.getOut().contains(testMessage));
    }

}
