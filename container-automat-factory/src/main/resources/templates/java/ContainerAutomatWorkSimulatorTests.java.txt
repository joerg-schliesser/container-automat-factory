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

import de.containerautomat.config.ContainerAutomatCoreConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test suite for the class {@link ContainerAutomatWorkSimulator}
 * for simulating application-specific processing logic that takes some
 * time and whose duration depends on chance within certain limits.
 */
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE + "=true",
        ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_PROCESSING_MIN_DURATION_MILLIS + "=10",
        ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_PROCESSING_MAX_DURATION_MILLIS + "=100"
})
@Import(ContainerAutomatWorkSimulator.class)
class ContainerAutomatWorkSimulatorTests {

    private static final String TEST_COMMAND_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String TEST_COMMAND_INPUT = "0101";
    private static final int TEST_COMMAND_INTERMEDIATE_PROCESSING_POSITION = 1;
    private static final int TEST_COMMAND_END_OF_PROCESSING_INPUT_POSITION = TEST_COMMAND_INPUT.length();


    @Value("${" + ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_PROCESSING_MIN_DURATION_MILLIS + "}")
    long testMinDurationMillis;

    @Value("${" + ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_PROCESSING_MAX_DURATION_MILLIS + "}")
    long testMaxDurationMillis;

    @Autowired
    private ContainerAutomatWorkSimulator workSimulator;


    @Test
    void work_simulator_bean_available() {

        assertNotNull(workSimulator);
    }

    @Test
    void min_duration_millis_from_properties() {

        assertEquals(testMinDurationMillis, workSimulator.getMinDurationMillis());
    }

    @Test
    void max_duration_millis_from_properties() {

        assertEquals(testMaxDurationMillis, workSimulator.getMaxDurationMillis());
    }

    @Test
    void zero_duration_millis() {

        var testWorkSimulator = new ContainerAutomatWorkSimulator(0, 0);
        assertEquals(0, testWorkSimulator.getRandomDuration());
    }

    @Test
    void invalid_duration_limits() {

        var negativeMinDurationMillis = -1;
        var maxDurationMillis = 100;
        assertThrows(IllegalArgumentException.class, () -> new ContainerAutomatWorkSimulator(negativeMinDurationMillis, maxDurationMillis));

        var minDurationMillis = 100;
        var maxDurationMillisLessThanMindurationMillis = minDurationMillis - 1;
        assertThrows(IllegalArgumentException.class, () -> new ContainerAutomatWorkSimulator(minDurationMillis, maxDurationMillisLessThanMindurationMillis));
    }

    @Test
    void create_random_durations() {

        for (int i = 0; i < testMaxDurationMillis - testMinDurationMillis; i++) {
            var randomDuration = workSimulator.getRandomDuration();
            assertTrue(randomDuration >= testMinDurationMillis && randomDuration <= testMaxDurationMillis);
        }
    }

    @Test
    void simulate_work_for_command_with_input_symbol() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_COMMAND_INSTANCE_ID)
                .processingInput(TEST_COMMAND_INPUT)
                .processingPosition(TEST_COMMAND_INTERMEDIATE_PROCESSING_POSITION)
                .build();

        var workResult = workSimulator.simulateWork(testCommand);
        var expectedMessage = ContainerAutomatWorkSimulator.WORKMESSAGE_WITH_INPUT_SYMBOL_TEMPLATE.formatted(testCommand.currentInputSymbol().orElseThrow(), workResult.durationMillis());
        assertEquals(expectedMessage, workResult.description());
    }

    @Test
    void simulate_work_for_command_without_input_symbol() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_COMMAND_INSTANCE_ID)
                .processingInput(TEST_COMMAND_INPUT)
                .processingPosition(TEST_COMMAND_END_OF_PROCESSING_INPUT_POSITION)
                .build();

        var workResult = workSimulator.simulateWork(testCommand);
        var expectedMessage = ContainerAutomatWorkSimulator.WORKMESSAGE_WITHOUT_INPUT_SYMBOL_TEMPLATE.formatted(workResult.durationMillis());
        assertEquals(expectedMessage, workResult.description());
    }

}
