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
package de.containerautomat.processing.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.ContainerAutomatCommandProcessor;
import de.containerautomat.processing.ContainerAutomatEventListener;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeProcessor;
import de.containerautomat.processing.runtime.ContainerAutomatWorkSimulator;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A test suite for testing the methods in {@link RabbitMqContainerAutomatConfig}
 * that define Spring beans.
 * <p/>
 * Note: These tests are rudimentary, as application logic is only simulated in the
 * generated application. In addition, no integration tests for the messaging and
 * broker components are included at this level.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rabbitmq")
@ContextConfiguration(classes = {RabbitMqContainerAutomatConfig.class})
@TestPropertySource(properties = {
        ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_ENTRY + "=true",
        ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE + "=true"
})
class RabbitMqContainerAutomatConfigTests {

    @MockBean
    ObjectMapper objectMapper;

    @MockBean
    ContainerAutomatRuntimeProcessor containerAutomatRuntimeProcessor;

    @MockBean
    ContainerAutomatWorkSimulator containerAutomatWorkSimulator;


    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    MessageConverter messageConverter;

    @Autowired
    DirectExchange directExchange;

    @Autowired
    FanoutExchange fanoutExchange;

    @Autowired
    ContainerAutomatCommandProcessor containerAutomatCommandProcessor;

    @Autowired
    ContainerAutomatEventListener containerAutomatEventListener;


    @Test
    void application_context_loads() {
        assertNotNull(applicationContext);
    }

    @Test
    void message_converter_bean_is_available() {
        assertNotNull(messageConverter);
    }

    @Test
    void direct_exchange_bean_is_available() {
        assertNotNull(directExchange);
    }

    @Test
    void fanout_exchange_bean_is_available() {
        assertNotNull(fanoutExchange);
    }

    @Test
    void containerautomat_command_processor_bean_is_available() {
        assertNotNull(containerAutomatCommandProcessor);
    }

    @Test
    void containerautomat_event_listener_bean_is_available() {
        assertNotNull(containerAutomatEventListener);
    }

}
