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
package de.containerautomat.processing.runtime;

import de.containerautomat.processing.ContainerAutomatEvent;
import lombok.*;

import java.time.Instant;

/**
 * An implementation of the data type {@link ContainerAutomatEvent}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerAutomatRuntimeEvent implements ContainerAutomatEvent {

    @NonNull
    private EventType eventType;

    @NonNull
    private Instant eventTime;

    @NonNull
    private String processingInstanceId;

    @NonNull
    private String processingInput;

    private int processingPosition;

    @NonNull
    private String stateName;

    private String description;

}
