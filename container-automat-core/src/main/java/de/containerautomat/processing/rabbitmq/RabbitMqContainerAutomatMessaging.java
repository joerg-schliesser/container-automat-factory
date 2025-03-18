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
package de.containerautomat.processing.rabbitmq;

import de.containerautomat.automaton.AutomatonState;
import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import de.containerautomat.processing.ContainerAutomatCommand;
import de.containerautomat.processing.ContainerAutomatEvent;
import de.containerautomat.processing.ContainerAutomatMessaging;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * An implementation of the service interface {@link ContainerAutomatMessaging}
 * for use with RabbitMQ as a message broker.
 * <p/>
 * Some notes on the messaging concepts used in conjunction with RabbitMQ are
 * located in {@link RabbitMqContainerAutomatConfig}.
 */
@Profile("rabbitmq")
@Service
@RequiredArgsConstructor
public class RabbitMqContainerAutomatMessaging implements ContainerAutomatMessaging {

    private final DeterministicFiniteAutomaton automaton;

    private final AmqpAdmin amqpAdmin;

    private final AmqpTemplate amqpTemplate;


    @PostConstruct
    void init() {

        prepareQueue(RabbitMqContainerAutomatConfig.EVENTS_QUEUE_NAME, RabbitMqContainerAutomatConfig.FANOUT_EXCHANGE_NAME_CONTAINERAUTOMAT_EVENTS);
        for (AutomatonState state : automaton.getStates()) {
            prepareQueue(RabbitMqContainerAutomatConfig.COMMANDS_QUEUE_NAME_PREFIX + state.getName(), RabbitMqContainerAutomatConfig.DIRECT_ECHANGE_NAME_CONTAINERAUTOMAT_COMMANDS);
        }
    }

    private void prepareQueue(String queueName, String exchangeName) {

        amqpAdmin.declareQueue(new Queue(queueName));
        amqpAdmin.declareBinding(new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, RabbitMqContainerAutomatConfig.FANOUT_EXCHANGE_NAME_CONTAINERAUTOMAT_EVENTS.equals(exchangeName) ? "" : queueName, null));
    }

    @Override
    public void sendContainerAutomatCommand(String targetState, ContainerAutomatCommand containerAutomatCommand) {

        amqpTemplate.convertAndSend(RabbitMqContainerAutomatConfig.DIRECT_ECHANGE_NAME_CONTAINERAUTOMAT_COMMANDS, RabbitMqContainerAutomatConfig.COMMANDS_QUEUE_NAME_PREFIX + targetState, containerAutomatCommand);
    }

    @Override
    public void sendContainerAutomatEvent(ContainerAutomatEvent containerAutomatEvent) {

        amqpTemplate.convertAndSend(RabbitMqContainerAutomatConfig.FANOUT_EXCHANGE_NAME_CONTAINERAUTOMAT_EVENTS, "", containerAutomatEvent);
    }

}
