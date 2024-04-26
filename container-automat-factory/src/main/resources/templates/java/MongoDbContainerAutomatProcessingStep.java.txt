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
package de.containerautomat.processing.mongodb;

import de.containerautomat.processing.ContainerAutomatProcessingStep;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * An implementation of the data type {@link ContainerAutomatProcessingStep}
 * for storing objects of this type in a MongoDB database when
 * using Spring Data MongoDB.
 */
@Data
@Builder
@Document(collection = "ContainerAutomatProcessingStep")
public class MongoDbContainerAutomatProcessingStep implements ContainerAutomatProcessingStep {

    @Id
    private String processingStepId;

    @Indexed
    private String processingInstanceId;

    private int processingPosition;

    @NonNull
    private String inputSymbol;

    @NonNull
    private String stateName;

    @NonNull
    private Instant startTime;

    @NonNull
    private Instant endTime;

    @NonNull
    private StepResult stepResult;

    @NonNull
    private String description;

}
