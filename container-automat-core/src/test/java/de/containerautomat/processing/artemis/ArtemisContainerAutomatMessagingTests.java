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
package de.containerautomat.processing.artemis;

import de.containerautomat.processing.ContainerAutomatEvent;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeCommand;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A test suite for testing the methods in {@link ArtemisContainerAutomatMessaging}
 * that send commands and events.
 * <p/>
 * Note: These tests are rudimentary, as application logic is only simulated in the
 * generated application. In addition, no integration tests for the messaging and
 * broker components are included at this level.
 */
@SpringBootTest(classes = ArtemisContainerAutomatMessaging.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("artemis")
class ArtemisContainerAutomatMessagingTests {

    private static final String TEST_PROCESSING_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String TEST_ACCEPTED_PROCESSING_INPUT = "1010";
    private static final int TEST_PROCESSING_POSITION = 1;
    private static final String TEST_STATE_NAME = "S1";
    private static final String TEST_EVENT_DESCRIPTION = "Test event for ContainerAutomatRuntimeProcessor.";


    @MockBean
    @Qualifier("commandQueueJmsTemplate")
    JmsTemplate commandQueueJmsTemplate;

    @MockBean
    @Qualifier("eventTopicJmsTemplate")
    JmsTemplate eventTopicJmsTemplate;


    @Autowired
    ArtemisContainerAutomatMessaging artemisContainerAutomatMessaging;


    @Test
    void artemis_containerautomat_messaging_service_is_available() {

        assertNotNull(artemisContainerAutomatMessaging);
    }

    @Test
    void send_containerautomat_command_calls_command_queue_jms_template() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .build();

        artemisContainerAutomatMessaging.sendContainerAutomatCommand(TEST_STATE_NAME, testCommand);

        Mockito.verify(commandQueueJmsTemplate, Mockito.times(1)).convertAndSend(ArtemisContainerAutomatConfig.COMMANDS_QUEUE_NAME_PREFIX + TEST_STATE_NAME, testCommand);
    }

    @Test
    void send_containerautomat_event_calls_event_topic_jms_template() {

        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(ContainerAutomatEvent.EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();

        artemisContainerAutomatMessaging.sendContainerAutomatEvent(testEvent);

        Mockito.verify(eventTopicJmsTemplate, Mockito.times(1)).convertAndSend(ArtemisContainerAutomatConfig.EVENTS_TOPIC, testEvent);
    }

}
