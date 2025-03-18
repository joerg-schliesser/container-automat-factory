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
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeCommand;

import java.util.Optional;

/**
 * The declaration of a data type that represents a command in the context
 * of processing an input string by the DFA implemented by the generated
 * application.
 * <p/>
 * Commands are sent to the service instances of the application that
 * represent the target states of state transitions of the DFA
 * using a message broker.
 * <p/>
 * In the context of processing the command, the next input symbol is
 * evaluated and either according to the associated state transition
 * a new command is sent to the next target state or the processing is
 * terminated with the determination of an error.
 * <p/>
 * If there is no further input symbol, it is checked in the context
 * of processing the command whether the current state is a final
 * state of the DFA or not. Accordingly, the acceptance or rejection
 * of the input is determined as the final result of the request.
 */
@JsonDeserialize(as = ContainerAutomatRuntimeCommand.class)
public interface ContainerAutomatCommand {

    String getProcessingInstanceId();

    String getProcessingInput();

    int getProcessingPosition();

    ContainerAutomatCommand nextCommand();


    default boolean hasInputSymbol() {

        return getProcessingInput() != null && getProcessingPosition() >= 0 && getProcessingPosition() < getProcessingInput().length();
    }

    default Optional<String> currentInputSymbol() {

        return hasInputSymbol() ? Optional.of(Character.toString(getProcessingInput().charAt(getProcessingPosition()))) : Optional.empty();
    }

    default boolean isProcessingEndCommand() {

        return !hasInputSymbol();
    }

}
