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
package de.containerautomat.processing.postgresql;

import de.containerautomat.processing.ContainerAutomatEvent;
import de.containerautomat.processing.ContainerAutomatProcessingInstance;
import de.containerautomat.processing.ContainerAutomatProcessingStep;
import de.containerautomat.processing.ContainerAutomatRequest;
import de.containerautomat.processing.ContainerAutomatStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * An implementation of the service interface {@link ContainerAutomatStorage}
 * for using a relational database with Spring Data JPA for the generated
 * application.
 */
@Profile("postgresql")
@Service
@RequiredArgsConstructor
public class PostgreSqlContainerAutomatStorage implements ContainerAutomatStorage {

    private final PostgreSqlContainerAutomatProcessingInstanceRepository processingInstanceRepository;

    private final PostgreSqlContainerAutomatProcessingStepRepository processingStepRepository;


    @Override
    public ContainerAutomatProcessingInstance createProcessingInstance(ContainerAutomatRequest containerAutomatRequest) {

        var processingInstance = PostgreSqlContainerAutomatProcessingInstance.builder()
                .processingInstanceId(UUID.randomUUID().toString())
                .creationTime(Instant.now())
                .input(containerAutomatRequest.getInput())
                .description(containerAutomatRequest.getDescription())
                .build();

        processingInstance = processingInstanceRepository.save(processingInstance);
        return processingInstance;
    }

    @Override
    public ContainerAutomatProcessingStep createProcessingStep(Instant startTime, ContainerAutomatEvent containerAutomatEvent) {

        var processingInstance = processingInstanceRepository.findByProcessingInstanceId(containerAutomatEvent.getProcessingInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("No ProcessingInstance with id %s.".formatted(containerAutomatEvent.getProcessingInstanceId())));

        var processingStep = PostgreSqlContainerAutomatProcessingStep.builder()
                .processingStepId(UUID.randomUUID().toString())
                .postgreSqlContainerAutomatProcessingInstance(processingInstance)
                .processingPosition(containerAutomatEvent.getProcessingPosition())
                .inputSymbol(containerAutomatEvent.currentInputSymbol().orElse(""))
                .stateName(containerAutomatEvent.getStateName())
                .startTime(startTime)
                .endTime(containerAutomatEvent.getEventTime())
                .stepResult(ContainerAutomatProcessingStep.createStepResultFromEvent(containerAutomatEvent.getEventType()))
                .description(containerAutomatEvent.getDescription())
                .build();

        processingStep = processingStepRepository.save(processingStep);
        return processingStep;
    }
}
