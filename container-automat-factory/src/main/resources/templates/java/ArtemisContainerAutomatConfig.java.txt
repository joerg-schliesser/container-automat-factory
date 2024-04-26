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
package de.containerautomat.processing.artemis;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.ContainerAutomatCommandProcessor;
import de.containerautomat.processing.ContainerAutomatEventListener;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeProcessor;
import de.containerautomat.processing.runtime.ContainerAutomatWorkSimulator;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * A Spring configuration that defines Spring beans needed by the generated
 * application when using ActiveMQ Artemis as a message broker.
 * <p/>
 * The following messaging concepts are used in the context of ActiveMQ Artemis:
 * <p/>
 * Commands of type {@link de.containerautomat.processing.ContainerAutomatCommand}
 * type are processed using point-to-point queues, with one-time processing and
 * processing guarantee.
 * <p/>
 * Events of type {@link de.containerautomat.processing.ContainerAutomatEvent}
 * are processed using publish-subscribe topics, with the possibility of multiple
 * processing and without a processing guarantee.
 * <p/>
 * JMS is used as the protocol.
 */
@Profile("artemis")
@Configuration
@EnableJms
@PropertySource(value = {"classpath:/artemis.properties"})
public class ArtemisContainerAutomatConfig {

    public static final String COMMANDS_QUEUE_NAME_PREFIX = "container-automat-";
    public static final String EVENTS_TOPIC = "container-automat-events";


    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    @Bean(name = "commandQueueJmsTemplate")
    public JmsTemplate commandQueueJmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {

        var result = new JmsTemplate(connectionFactory);
        result.setMessageConverter(messageConverter);
        result.setPubSubDomain(false);
        result.setDeliveryMode(DeliveryMode.PERSISTENT);
        return result;
    }

    @Bean(name = "eventTopicJmsTemplate")
    public JmsTemplate eventTopicJmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {

        var result = new JmsTemplate(connectionFactory);
        result.setMessageConverter(messageConverter);
        result.setPubSubDomain(true);
        result.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return result;
    }

    @Bean(name = "queueJmsListenerContainerFactory")
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE)
    public JmsListenerContainerFactory<?> queueJmsListenerContainerFactory(ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) {

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setPubSubDomain(false);
        return factory;
    }

    @Bean(name = "topicJmsListenerContainerFactory")
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_ENTRY)
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) {

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }

    @Bean
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE)
    public ContainerAutomatCommandProcessor containerAutomatCommandProcessor(ContainerAutomatRuntimeProcessor containerAutomatRuntimeProcessor, ContainerAutomatWorkSimulator containerAutomatWorkSimulator) {

        return new ArtemisContainerAutomatCommandProcessor(containerAutomatRuntimeProcessor, containerAutomatWorkSimulator);
    }

    @Bean
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_ENTRY)
    public ContainerAutomatEventListener containerAutomatEventListener() {

        return new ArtemisContainerAutomatEventListener();
    }

}
