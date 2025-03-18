/*
 * Copyright 2024-2025 the original author or authors.
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
package de.containerautomat.processing.kafka;

import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.ContainerAutomatCommandProcessor;
import de.containerautomat.processing.ContainerAutomatEventListener;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeProcessor;
import de.containerautomat.processing.runtime.ContainerAutomatWorkSimulator;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.config.TopicBuilder;

/**
 * A Spring configuration that defines Spring beans needed by the generated
 * application when using Kafka as a message broker.
 * <p/>
 * The following Kafka topics are used:
 * <p/>
 * Commands of type {@link de.containerautomat.processing.ContainerAutomatCommand}
 * are processed via the {@link #stateTopic(String)}. For each service that represents
 * a state of the DFA, a corresponding topic is created.
 * <p/>
 * Events of type {@link de.containerautomat.processing.ContainerAutomatEvent}
 * are processed via the {@link #eventsTopic()}.
 */
@Profile("kafka")
@Configuration
@PropertySource(value = {"classpath:/kafka.properties"})
public class KafkaContainerAutomatConfig {

    static final String COMMANDS_TOPIC_NAME_PREFIX = "container-automat-";
    static final String EVENTS_TOPIC_NAME = "container-automat-events";


    @Bean
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE)
    public NewTopic stateTopic(@Value("${" + ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_STATE_NAME + ":}") String stateName) {

        return TopicBuilder.name(COMMANDS_TOPIC_NAME_PREFIX + stateName)
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_ENTRY)
    public NewTopic eventsTopic() {

        return TopicBuilder.name(EVENTS_TOPIC_NAME)
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE)
    public ContainerAutomatCommandProcessor containerAutomatCommandProcessor(ContainerAutomatRuntimeProcessor containerAutomatRuntimeProcessor, ContainerAutomatWorkSimulator containerAutomatWorkSimulator) {

        return new KafkaContainerAutomatCommandProcessor(containerAutomatRuntimeProcessor, containerAutomatWorkSimulator);
    }

    @Bean
    @ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_ENTRY)
    public ContainerAutomatEventListener containerAutomatEventListener() {

        return new KafkaContainerAutomatEventListener();
    }

}
