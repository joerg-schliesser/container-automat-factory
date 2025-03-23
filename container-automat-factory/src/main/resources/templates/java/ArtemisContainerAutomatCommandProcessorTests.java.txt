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

import de.containerautomat.processing.ContainerAutomatCommandProcessor;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeCommand;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeProcessor;
import de.containerautomat.processing.runtime.ContainerAutomatWorkSimulator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * A test suite for testing the method in {@link ArtemisContainerAutomatCommandProcessor}
 * that processes commands.
 * <p/>
 * Note: These tests are rudimentary, as application logic is only simulated in the
 * generated application. In addition, no integration tests for the messaging and
 * broker components are included at this level.
 */
@SpringBootTest(classes = ArtemisContainerAutomatCommandProcessor.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("artemis")
class ArtemisContainerAutomatCommandProcessorTests {

    private static final String TEST_PROCESSING_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String TEST_ACCEPTED_PROCESSING_INPUT = "1010";
    private static final int TEST_PROCESSING_POSITION = 1;


    @MockBean
    ContainerAutomatRuntimeProcessor containerAutomatRuntimeProcessor;

    @MockBean
    ContainerAutomatWorkSimulator containerAutomatWorkSimulator;


    @Autowired
    ContainerAutomatCommandProcessor containerAutomatCommandProcessor;


    @Test
    void artemis_containerautomat_command_processor_bean_is_available() {

        assertNotNull(containerAutomatCommandProcessor);
        assertInstanceOf(ArtemisContainerAutomatCommandProcessor.class, containerAutomatCommandProcessor);
    }

    @Test
    void process_command_calls_containerautomat_command_processor() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_ACCEPTED_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .build();

        containerAutomatCommandProcessor.processCommand(testCommand);

        Mockito.verify(containerAutomatRuntimeProcessor, Mockito.times(1)).processCommand(eq(testCommand), any(Function.class));
    }

}