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
package de.containerautomat.processing.mongodb;

import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A test suite to test that the {@link MongoDbContainerAutomatConfig}
 * can be loaded.
 */
@SpringBootTest(classes = MongoDbContainerAutomatConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = MongoDbContainerAutomatConfigTests.TestConfig.class)
@ActiveProfiles("mongodb")
class MongoDbContainerAutomatConfigTests {

    @Profile("mongodb")
    @TestConfiguration
    static class TestConfig {

        @Bean
        public MongoClient mongoClient() {
            return Mockito.mock(MongoClient.class);
        }

        @Bean
        public MongoTemplate mongoTemplate() {
            return new MongoTemplate(mongoClient(), "testdb");
        }

    }


    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    MongoDbContainerAutomatConfig mongoDbContainerAutomatConfig;

    @Autowired
    MongoDbContainerAutomatProcessingInstanceRepository mongoDbContainerAutomatProcessingInstanceRepository;

    @Autowired
    MongoDbContainerAutomatProcessingStepRepository mongoDbContainerAutomatProcessingStepRepository;


    @Test
    void application_context_loads() {
        assertNotNull(applicationContext);
    }

    @Test
    void mongodb_containerautomat_config_loads() {
        assertNotNull(mongoDbContainerAutomatConfig);
    }

    @Test
    void mongodb_containerautomat_processing_instance_repository_available() {
        assertNotNull(mongoDbContainerAutomatProcessingInstanceRepository);
    }

    @Test
    void mongodb_containerautomat_processing_step_repository_available() {
        assertNotNull(mongoDbContainerAutomatProcessingStepRepository);
    }

}
