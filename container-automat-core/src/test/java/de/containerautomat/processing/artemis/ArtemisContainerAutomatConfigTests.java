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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.ContainerAutomatCommandProcessor;
import de.containerautomat.processing.ContainerAutomatEventListener;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeProcessor;
import de.containerautomat.processing.runtime.ContainerAutomatWorkSimulator;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test suite for testing the methods in {@link ArtemisContainerAutomatConfig}
 * that define Spring beans.
 * <p/>
 * Note: These tests are rudimentary, as application logic is only simulated in the
 * generated application. In addition, no integration tests for the messaging and
 * broker components are included at this level.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("artemis")
@ContextConfiguration(classes = ArtemisContainerAutomatConfig.class)
@TestPropertySource(properties = {
        ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_ENTRY + "=true",
        ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE + "=true"
})
class ArtemisContainerAutomatConfigTests {

    @MockBean
    ObjectMapper objectMapper;

    @MockBean
    ConnectionFactory connectionFactory;

    @MockBean
    DefaultJmsListenerContainerFactoryConfigurer configurer;

    @MockBean
    JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

    @MockBean
    ContainerAutomatRuntimeProcessor containerAutomatRuntimeProcessor;

    @MockBean
    ContainerAutomatWorkSimulator containerAutomatWorkSimulator;


    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    MessageConverter messageConverter;

    @Autowired
    @Qualifier("commandQueueJmsTemplate")
    JmsTemplate commandQueueJmsTemplate;

    @Autowired
    @Qualifier("eventTopicJmsTemplate")
    JmsTemplate eventTopicJmsTemplate;

    @Autowired
    @Qualifier("queueJmsListenerContainerFactory")
    JmsListenerContainerFactory<?> queueJmsListenerContainerFactory;

    @Autowired
    @Qualifier("topicJmsListenerContainerFactory")
    JmsListenerContainerFactory<?> topicJmsListenerContainerFactory;

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
    void command_queue_jms_template_bean_is_available() {
        assertNotNull(commandQueueJmsTemplate);
        assertFalse(commandQueueJmsTemplate.isPubSubDomain());
        assertEquals(DeliveryMode.PERSISTENT, commandQueueJmsTemplate.getDeliveryMode());
    }

    @Test
    void event_topic_jms_template_bean_is_available() {
        assertNotNull(eventTopicJmsTemplate);
        assertTrue(eventTopicJmsTemplate.isPubSubDomain());
        assertEquals(DeliveryMode.NON_PERSISTENT, eventTopicJmsTemplate.getDeliveryMode());
    }

    @Test
    void queue_jms_listener_container_factory_bean_is_available() {
        assertNotNull(queueJmsListenerContainerFactory);
    }

    @Test
    void topic_jms_listener_container_factory_bean_is_available() {
        assertNotNull(topicJmsListenerContainerFactory);
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