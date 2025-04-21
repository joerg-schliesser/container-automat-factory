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
package de.containerautomat.processing.redis;

import de.containerautomat.processing.ContainerAutomatEvent;
import de.containerautomat.processing.ContainerAutomatProcessingStep;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeEvent;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

/**
 * A test suite for testing the methods in {@link RedisContainerAutomatStorage}
 * that store information about requests and events to a Redis database.
 * <p/>
 * Note: These tests are rudimentary, as application logic is only simulated in the
 * generated application. In addition, no integration tests for the actual database
 * access are included at this level.
 */
@SpringBootTest(classes = RedisContainerAutomatStorage.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {RedisContainerAutomatConfig.class, RedisContainerAutomatStorageTests.TestConfig.class})
@ActiveProfiles("redis")
class RedisContainerAutomatStorageTests {

    private static final String TEST_INPUT = "1010";
    private static final String TEST_REQUEST_DESCRIPTION = "Test reqest for ContainerAutomatStorage.";
    private static final String TEST_INSTANCE_ID = UUID.randomUUID().toString();
    private static final int TEST_PROCESSING_POSITION = 1;
    private static final String TEST_STATE_NAME = "S1";
    private static final String TEST_EVENT_DESCRIPTION = "Test event for ContainerAutomatStorage.";


    @Profile("redis")
    @TestConfiguration
    static class TestConfig {

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return new LettuceConnectionFactory();
        }

        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory);
            template.setKeySerializer(new GenericToStringSerializer<>(Object.class));
            template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
            return template;
        }

    }


    @SpyBean
    RedisContainerAutomatProcessingInstanceRepository redisContainerAutomatProcessingInstanceRepository;

    @SpyBean
    RedisContainerAutomatProcessingStepRepository redisContainerAutomatProcessingStepRepository;

    @Autowired
    RedisContainerAutomatStorage redisContainerAutomatStorage;


    @Test
    void redis_containerautomat_storage_available() {
        assertNotNull(redisContainerAutomatStorage);
    }

    @Test
    void create_processing_instance() {

        var testRequest = new ContainerAutomatRuntimeRequest(TEST_INPUT, TEST_REQUEST_DESCRIPTION);

        Mockito.doAnswer(invocation -> invocation.getArgument(0, RedisContainerAutomatProcessingInstance.class))
                .when(redisContainerAutomatProcessingInstanceRepository)
                .save(Mockito.any(RedisContainerAutomatProcessingInstance.class));

        var testResult = redisContainerAutomatStorage.createProcessingInstance(testRequest);

        Mockito.verify(redisContainerAutomatProcessingInstanceRepository, times(1)).save(Mockito.any(RedisContainerAutomatProcessingInstance.class));

        assertNotNull(testResult);
        assertEquals(TEST_INPUT, testResult.getInput());
        assertNotNull(TEST_REQUEST_DESCRIPTION, testResult.getDescription());
    }

    @Test
    void create_processing_step_for_existing_processing_instance() {

        var testInstance = RedisContainerAutomatProcessingInstance.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .creationTime(Instant.now())
                .input(TEST_INPUT)
                .description(TEST_REQUEST_DESCRIPTION)
                .build();
        var testTime = Instant.now();
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(ContainerAutomatEvent.EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();

        Mockito.doReturn(Optional.of(testInstance)).when(redisContainerAutomatProcessingInstanceRepository)
                .findById(TEST_INSTANCE_ID);
        Mockito.doAnswer(invocation -> invocation.getArgument(0, RedisContainerAutomatProcessingStep.class))
                .when(redisContainerAutomatProcessingStepRepository)
                .save(Mockito.any(RedisContainerAutomatProcessingStep.class));

        var testResult = redisContainerAutomatStorage.createProcessingStep(testTime, testEvent);

        Mockito.verify(redisContainerAutomatProcessingStepRepository, times(1)).save(Mockito.any(RedisContainerAutomatProcessingStep.class));

        assertNotNull(testResult);
        assertEquals(TEST_INSTANCE_ID, testResult.getProcessingInstanceId());
        assertEquals(TEST_PROCESSING_POSITION, testResult.getProcessingPosition());
        assertEquals(testEvent.currentInputSymbol().orElseThrow(), testResult.getInputSymbol());
        assertEquals(TEST_STATE_NAME, testResult.getStateName());
        assertEquals(testTime, testResult.getStartTime());
        assertEquals(ContainerAutomatProcessingStep.createStepResultFromEvent(ContainerAutomatEvent.EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS), testResult.getStepResult());
        assertEquals(TEST_EVENT_DESCRIPTION, testResult.getDescription());
    }

    @Test
    void create_processing_step_for_not_existing_processing_instance() {

        var testTime = Instant.now();
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(ContainerAutomatEvent.EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();

        Mockito.doReturn(Optional.empty()).when(redisContainerAutomatProcessingInstanceRepository)
                .findById(Mockito.any(String.class));

        var resultError = assertThrows(IllegalArgumentException.class, () -> redisContainerAutomatStorage.createProcessingStep(testTime, testEvent));

        assertEquals(RedisContainerAutomatStorage.ERROR_MESSAGE_UNKNOWN_PROCESSING_INSTANCE_ID.formatted(TEST_INSTANCE_ID), resultError.getMessage());
    }

}
