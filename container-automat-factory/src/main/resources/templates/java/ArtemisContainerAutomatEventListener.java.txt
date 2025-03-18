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
package de.containerautomat.processing.artemis;

import de.containerautomat.processing.ContainerAutomatEvent;
import de.containerautomat.processing.ContainerAutomatEventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;

/**
 * An implementation of the service interface {@link ContainerAutomatEventListener}
 * for use with ActiveMQ Artemis as a message broker.
 * <p/>
 * Some notes on the messaging concepts used in conjunction with ActiveMQ Artemis
 * are located in {@link ArtemisContainerAutomatConfig}.
 */
public class ArtemisContainerAutomatEventListener implements ContainerAutomatEventListener {

    private static final Log log = LogFactory.getLog(ArtemisContainerAutomatEventListener.class);

    static final String HANDLE_EVENT_LOG_MESSAGE_TEMPLATE = "Received ContainerAutomatEvent:%n%s";


    @Override
    @JmsListener(destination = ArtemisContainerAutomatConfig.EVENTS_TOPIC, containerFactory = "topicJmsListenerContainerFactory")
    public void handleEvent(ContainerAutomatEvent containerAutomatEvent) {

        log.info(HANDLE_EVENT_LOG_MESSAGE_TEMPLATE.formatted(containerAutomatEvent.toString()));
    }

}
