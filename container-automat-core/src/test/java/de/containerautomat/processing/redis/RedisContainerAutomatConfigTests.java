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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A test suite to test that the {@link RedisContainerAutomatConfig}
 * can be loaded.
 */
@SpringBootTest(classes = RedisContainerAutomatConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = RedisContainerAutomatConfigTests.TestConfig.class)
@ActiveProfiles("redis")
class RedisContainerAutomatConfigTests {

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


    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    RedisContainerAutomatConfig redisContainerAutomatConfig;

    @Autowired
    RedisContainerAutomatProcessingInstanceRepository redisContainerAutomatProcessingInstanceRepository;

    @Autowired
    RedisContainerAutomatProcessingStepRepository redisContainerAutomatProcessingStepRepository;


    @Test
    void application_context_loads() {
        assertNotNull(applicationContext);
    }

    @Test
    void redis_containerautomat_config_loads() {
        assertNotNull(redisContainerAutomatConfig);
    }

    @Test
    void redis_containerautomat_processing_instance_repository_available() {
        assertNotNull(redisContainerAutomatProcessingInstanceRepository);
    }

    @Test
    void redis_containerautomat_processing_step_repository_available() {
        assertNotNull(redisContainerAutomatProcessingStepRepository);
    }

}
