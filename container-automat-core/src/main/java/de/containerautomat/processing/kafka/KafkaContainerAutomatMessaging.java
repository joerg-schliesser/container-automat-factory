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
package de.containerautomat.processing.kafka;

import de.containerautomat.processing.ContainerAutomatCommand;
import de.containerautomat.processing.ContainerAutomatEvent;
import de.containerautomat.processing.ContainerAutomatMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * An implementation of the service interface {@link ContainerAutomatMessaging}
 * for use with Kafka as a message broker.
 * <p/>
 * Some notes on the topics used in conjunction with Kafka are located in
 * {@link KafkaContainerAutomatConfig}.
 */
@Profile("kafka")
@Service
@RequiredArgsConstructor
public class KafkaContainerAutomatMessaging implements ContainerAutomatMessaging {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    public void sendContainerAutomatCommand(String targetState, ContainerAutomatCommand containerAutomatCommand) {

        kafkaTemplate.send(KafkaContainerAutomatConfig.COMMANDS_TOPIC_NAME_PREFIX + targetState, containerAutomatCommand.getProcessingInstanceId(), containerAutomatCommand);
    }

    @Override
    public void sendContainerAutomatEvent(ContainerAutomatEvent containerAutomatEvent) {

        kafkaTemplate.send(KafkaContainerAutomatConfig.EVENTS_TOPIC_NAME, containerAutomatEvent.getProcessingInstanceId(), containerAutomatEvent);
    }

}
