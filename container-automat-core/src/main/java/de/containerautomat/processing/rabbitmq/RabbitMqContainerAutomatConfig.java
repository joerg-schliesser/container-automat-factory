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
package de.containerautomat.processing.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.ContainerAutomatCommandProcessor;
import de.containerautomat.processing.ContainerAutomatEventListener;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeProcessor;
import de.containerautomat.processing.runtime.ContainerAutomatWorkSimulator;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * A Spring configuration that defines Spring beans needed by the generated
 * application when using RabbitMQ as a message broker.
 * <p/>
 *  The following messaging concepts are used in the context of RabbitMQ:
 *  <p/>
 *  Commands of type {@link de.containerautomat.processing.ContainerAutomatCommand}
 *  are processed via a {@link DirectExchange}. For each service that represents a state
 *  of the DFA, a queue is created.
 *  <p/>
 *  Events of type {@link de.containerautomat.processing.ContainerAutomatEvent}
 *  are processed via a {@link FanoutExchange}, for which a single queue is created
 *  that can be used by multiple receivers.
 *  <p/>
 *  The queues are created in the {@link RabbitMqContainerAutomatMessaging} service.
 *  <p/>
 *  AMQP is used as the protocol.
 */
@Profile("rabbitmq")
@Configuration
@PropertySource(value = {"classpath:/rabbitmq.properties"})
public class RabbitMqContainerAutomatConfig {

    public static final String FANOUT_EXCHANGE_NAME_CONTAINERAUTOMAT_EVENTS = "container-automat-events";
    public static final String DIRECT_ECHANGE_NAME_CONTAINERAUTOMAT_COMMANDS = "container-automat-commands";
    public static final String COMMANDS_QUEUE_NAME_PREFIX = "container-automat-";
    public static final String EVENTS_QUEUE_NAME = "container-automat-events";


    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public DirectExchange directExchange() {

        return new DirectExchange(DIRECT_ECHANGE_NAME_CONTAINERAUTOMAT_COMMANDS, true, false);
    }

    @Bean
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE)
    public FanoutExchange fanoutExchange() {

        return new FanoutExchange(FANOUT_EXCHANGE_NAME_CONTAINERAUTOMAT_EVENTS, false, false);
    }

    @Bean
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE)
    public ContainerAutomatCommandProcessor containerAutomatCommandProcessor(ContainerAutomatRuntimeProcessor containerAutomatRuntimeProcessor, ContainerAutomatWorkSimulator containerAutomatWorkSimulator) {

        return new RabbitMqContainerAutomatCommandProcessor(containerAutomatRuntimeProcessor, containerAutomatWorkSimulator);
    }

    @Bean
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_ENTRY)
    public ContainerAutomatEventListener containerAutomatEventListener() {

        return new RabbitMqContainerAutomatEventListener();
    }

}
