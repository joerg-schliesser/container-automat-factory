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

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test suite for the {@link PostgreSqlContainerAutomatProcessingInstance} entity class
 * for testing the implementation of {@link PostgreSqlContainerAutomatProcessingInstance#equals(Object)}
 * and {@link PostgreSqlContainerAutomatProcessingInstance#hashCode()}.
 */
class PostgreSqlContainerAutomatProcessingInstanceTests {

    private static final String TEST_INSTANCE_INPUT = "1010";
    private static final String TEST_INSTANCE_DESCRIPTION = "Test PostgreSqlContainerAutomatProcessingInstance";
    private static final String TEST_INSTANCE_ID = UUID.randomUUID().toString();
    private static final long TEST_INSTANCE_KEY = 645;

    private static final String OTHER_TEST_INSTANCE_INPUT = "0101";
    private static final String OTHER_TEST_INSTANCE_DESCRIPTION = "Other test PostgreSqlContainerAutomatProcessingInstance";
    private static final String OTHER_TEST_INSTANCE_ID = UUID.randomUUID().toString();
    private static final long OTHER_TEST_INSTANCE_KEY = 845;


    @Test
    void hashcode_of_processing_instance() {

        var testInstance = createTestInstance();

        assertEquals(Objects.hashCode(TEST_INSTANCE_ID), testInstance.hashCode());
    }

    @Test
    void equal_to_same_object() {

        var testInstance = createTestInstance();

        assertTrue(testInstance.equals(testInstance));
    }

    @Test
    void equal_in_case_of_same_id() {

        var testInstance1 = createTestInstance();
        var testInstance2 = createOtherTestInstance();
        testInstance2.setProcessingInstanceId(testInstance1.getProcessingInstanceId());

        assertEquals(testInstance1, testInstance2);
    }

    @Test
    void not_equal_to_null() {

        var testInstance = createTestInstance();

        assertFalse(testInstance.equals(null));
    }

    @Test
    void not_equal_to_other_class() {

        var testInstance = createTestInstance();

        assertFalse(testInstance.equals(new Object()));
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

    private PostgreSqlContainerAutomatProcessingInstance createOtherTestInstance() {

        return PostgreSqlContainerAutomatProcessingInstance.builder()
                .processingInstanceId(OTHER_TEST_INSTANCE_ID)
                .creationTime(Instant.now())
                .input(OTHER_TEST_INSTANCE_INPUT)
                .description(OTHER_TEST_INSTANCE_DESCRIPTION)
                .key(OTHER_TEST_INSTANCE_KEY)
                .build();
    }

}
