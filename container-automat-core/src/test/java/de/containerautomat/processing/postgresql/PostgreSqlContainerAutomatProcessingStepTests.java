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
package de.containerautomat.processing.postgresql;

import de.containerautomat.processing.ContainerAutomatProcessingStep;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test suite for the {@link PostgreSqlContainerAutomatProcessingStep} entity class
 * for testing the implementation of {@link PostgreSqlContainerAutomatProcessingStep#equals(Object)}
 * and {@link PostgreSqlContainerAutomatProcessingStep#hashCode()}.
 */
class PostgreSqlContainerAutomatProcessingStepTests {

    private static final int TEST_STEP_PROCESSING_POSITION = 1;
    private static final String TEST_STEP_INPUT_SYMBOL = "0";
    private static final String TEST_STEP_DESCRIPTION = "Test PostgreSqlContainerAutomatProcessingStep";
    private static final String TEST_STEP_ID = UUID.randomUUID().toString();
    private static final long TEST_STEP_KEY = 425;
    private static final String TEST_STEP_STATE_NAME = "S1";
    private static final ContainerAutomatProcessingStep.StepResult TEST_STEP_RESULT = ContainerAutomatProcessingStep.StepResult.CONTINUE_PROCESSING;

    private static final int OTHER_TEST_STEP_PROCESSING_POSITION = 0;
    private static final String OTHER_TEST_STEP_INPUT_SYMBOL = "1";
    private static final String OTHER_TEST_STEP_DESCRIPTION = "Other test PostgreSqlContainerAutomatProcessingStep";
    private static final String OTHER_TEST_STEP_ID = UUID.randomUUID().toString();
    private static final long OTHER_TEST_STEP_KEY = 312;
    private static final String OTHER_TEST_STEP_STATE_NAME = "S2";
    private static final ContainerAutomatProcessingStep.StepResult OTHER_TEST_STEP_RESULT = ContainerAutomatProcessingStep.StepResult.CONTINUE_PROCESSING;

    private static final String TEST_INSTANCE_INPUT = "1010";
    private static final String TEST_INSTANCE_DESCRIPTION = "Test PostgreSqlContainerAutomatProcessingInstance";
    private static final String TEST_INSTANCE_ID = UUID.randomUUID().toString();
    private static final long TEST_INSTANCE_KEY = 645;


    @Test
    void hashcode_of_processing_step() {

        var testStep = createTestStep();

        assertEquals(Objects.hashCode(TEST_STEP_ID), testStep.hashCode());
    }

    @Test
    void equal_to_same_object() {

        var testStep = createTestStep();

        assertTrue(testStep.equals(testStep));
    }

    @Test
    void equal_in_case_of_same_id() {

        var testStep = createTestStep();
        var testStep2 = createOtherTestStep();
        testStep2.setProcessingStepId(testStep.getProcessingStepId());

        assertEquals(testStep, testStep2);
    }

    @Test
    void not_equal_to_null() {

        var testStep = createTestStep();

        assertFalse(testStep.equals(null));
    }

    @Test
    void not_equal_to_other_class() {

        var testStep = createTestStep();

        assertFalse(testStep.equals(new Object()));
    }

    private PostgreSqlContainerAutomatProcessingStep createTestStep() {

        return PostgreSqlContainerAutomatProcessingStep.builder()
                .processingStepId(TEST_STEP_ID)
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(1))
                .stepResult(TEST_STEP_RESULT)
                .processingPosition(TEST_STEP_PROCESSING_POSITION)
                .stateName(TEST_STEP_STATE_NAME)
                .description(TEST_STEP_DESCRIPTION)
                .key(TEST_STEP_KEY)
                .inputSymbol(TEST_STEP_INPUT_SYMBOL)
                .postgreSqlContainerAutomatProcessingInstance(createTestInstance())
                .build();
    }

    private PostgreSqlContainerAutomatProcessingStep createOtherTestStep() {

        return PostgreSqlContainerAutomatProcessingStep.builder()
                .processingStepId(OTHER_TEST_STEP_ID)
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(1))
                .stepResult(OTHER_TEST_STEP_RESULT)
                .processingPosition(OTHER_TEST_STEP_PROCESSING_POSITION)
                .stateName(OTHER_TEST_STEP_STATE_NAME)
                .description(OTHER_TEST_STEP_DESCRIPTION)
                .key(OTHER_TEST_STEP_KEY)
                .inputSymbol(OTHER_TEST_STEP_INPUT_SYMBOL)
                .postgreSqlContainerAutomatProcessingInstance(createTestInstance())
                .build();
    }

    private PostgreSqlContainerAutomatProcessingInstance createTestInstance() {

        return PostgreSqlContainerAutomatProcessingInstance.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .creationTime(Instant.now())
                .input(TEST_INSTANCE_INPUT)
                .description(TEST_INSTANCE_DESCRIPTION)
                .key(TEST_INSTANCE_KEY)
                .build();
    }

}
