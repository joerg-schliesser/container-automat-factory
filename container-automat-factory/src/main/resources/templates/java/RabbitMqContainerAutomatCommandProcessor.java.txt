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

import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.ContainerAutomatCommand;
import de.containerautomat.processing.ContainerAutomatCommandProcessor;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeProcessor;
import de.containerautomat.processing.runtime.ContainerAutomatWorkSimulator;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * An implementation of the service interface {@link ContainerAutomatCommandProcessor}
 * for use with RabbitMQ as a message broker.
 * <p/>
 * For the actual processing of the commands, the class
 * {@link ContainerAutomatRuntimeProcessor} is delegated to.
 * <p/>
 * For the simulation of an application-specific processing logic that takes some
 * time and whose duration depends to some extent on chance, an object of type
 * {@link ContainerAutomatWorkSimulator} is used.
 * <p/>
 * Some notes on the messaging concepts used in conjunction with RabbitMQ are
 * located in {@link RabbitMqContainerAutomatConfig}.
 */
@RabbitListener(queues = RabbitMqContainerAutomatConfig.COMMANDS_QUEUE_NAME_PREFIX + "${" + ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_STATE_NAME + ":}")
@RequiredArgsConstructor
public class RabbitMqContainerAutomatCommandProcessor implements ContainerAutomatCommandProcessor {

    private final ContainerAutomatRuntimeProcessor containerAutomatRuntimeProcessor;

    private final ContainerAutomatWorkSimulator containerAutomatWorkSimulator;


    @Override
    @RabbitHandler
    public void processCommand(ContainerAutomatCommand containerAutomatCommand) {

        containerAutomatRuntimeProcessor.processCommand(containerAutomatCommand, containerAutomatWorkSimulator::simulateWork);
    }

}
