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
package de.containerautomat.processing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeEvent;

import java.time.Instant;
import java.util.Optional;

/**
 * The declaration of a data type that represents an event in the context
 * of processing an input string by the DFA implemented by the generated
 * application.
 * <p/>
 * Events are published using a message broker and can be received by
 * multiple recipients to log the processing of requests.
 */
@JsonDeserialize(as = ContainerAutomatRuntimeEvent.class)
public interface ContainerAutomatEvent {

    enum EventType {
        STATE_PROCESSING_START, STATE_PROCESSING_FINISHED_CONTINUE_PROCESS, STATE_PROCESSING_FINISHED_INPUT_ACCEPTED, STATE_PROCESSING_FINISHED_INPUT_REJECTED, STATE_PROCESSING_ERROR
    }


    EventType getEventType();

    Instant getEventTime();

    String getProcessingInstanceId();

    String getProcessingInput();

    int getProcessingPosition();

    String getStateName();

    String getDescription();


    default Optional<String> currentInputSymbol() {

        if (getProcessingInput() == null || getProcessingPosition() < 0 || getProcessingPosition() >= getProcessingInput().length()) {
            return Optional.empty();
        }
        return Optional.of(Character.toString(getProcessingInput().charAt(getProcessingPosition())));
    }

}
