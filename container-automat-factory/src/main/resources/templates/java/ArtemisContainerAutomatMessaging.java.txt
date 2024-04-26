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

import de.containerautomat.processing.ContainerAutomatCommand;
import de.containerautomat.processing.ContainerAutomatEvent;
import de.containerautomat.processing.ContainerAutomatMessaging;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;


/**
 * An implementation of the service interface {@link ContainerAutomatMessaging}
 * for use with ActiveMQ Artemis as a message broker.
 * <p/>
 * Some notes on the messaging concepts used in conjunction with ActiveMQ Artemis
 * are located in {@link ArtemisContainerAutomatConfig}.
 */
@Profile("artemis")
@Service
public class ArtemisContainerAutomatMessaging implements ContainerAutomatMessaging {

    private final JmsTemplate commandQueueJmsTemplate;

    private final JmsTemplate eventTopicJmsTemplate;


    public ArtemisContainerAutomatMessaging(@Qualifier("commandQueueJmsTemplate") JmsTemplate commandQueueJmsTemplate, @Qualifier("eventTopicJmsTemplate") JmsTemplate eventTopicJmsTemplate) {

        this.commandQueueJmsTemplate = commandQueueJmsTemplate;
        this.eventTopicJmsTemplate = eventTopicJmsTemplate;
    }

    @Override
    public void sendContainerAutomatCommand(String targetState, ContainerAutomatCommand containerAutomatCommand) {

        commandQueueJmsTemplate.convertAndSend(ArtemisContainerAutomatConfig.COMMANDS_QUEUE_NAME_PREFIX + targetState, containerAutomatCommand);
    }

    @Override
    public void sendContainerAutomatEvent(ContainerAutomatEvent containerAutomatEvent) {

        eventTopicJmsTemplate.convertAndSend(ArtemisContainerAutomatConfig.EVENTS_TOPIC, containerAutomatEvent);
    }

}
