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

import java.time.Instant;

/**
 * The declaration of a data type that represents information about a step
 * of processing an input string by the DFA implemented by the generated
 * application.
 * <p/>
 * Information about the steps is persistently stored in a database during
 * the processing.
 */
public interface ContainerAutomatProcessingStep {

    enum StepResult {
        CONTINUE_PROCESSING, PROCESSING_FINISHED_INPUT_ACCEPTED, PROCESSING_FINISHED_INPUT_REJECTED, PROCESSING_ERROR
    }


    String getProcessingStepId();

    String getProcessingInstanceId();

    int getProcessingPosition();

    String getInputSymbol();

    String getStateName();

    Instant getStartTime();

    Instant getEndTime();

    StepResult getStepResult();

    String getDescription();


    static StepResult createStepResultFromEvent(ContainerAutomatEvent.EventType eventType) {

        return switch (eventType) {
            case STATE_PROCESSING_FINISHED_CONTINUE_PROCESS -> ContainerAutomatProcessingStep.StepResult.CONTINUE_PROCESSING;
            case STATE_PROCESSING_FINISHED_INPUT_ACCEPTED -> ContainerAutomatProcessingStep.StepResult.PROCESSING_FINISHED_INPUT_ACCEPTED;
            case STATE_PROCESSING_FINISHED_INPUT_REJECTED -> ContainerAutomatProcessingStep.StepResult.PROCESSING_FINISHED_INPUT_REJECTED;
            case STATE_PROCESSING_START, STATE_PROCESSING_ERROR -> ContainerAutomatProcessingStep.StepResult.PROCESSING_ERROR;
        };
    }

}
