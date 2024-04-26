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

import de.containerautomat.processing.ContainerAutomatCommand;
import de.containerautomat.processing.ContainerAutomatProcessingInstance;
import lombok.*;

/**
 * An implementation of the data type {@link ContainerAutomatCommand},
 * which also provides factory methods.
 */
@Data
@Builder
@NoArgsConstructor // Used for Jackson deserialization.
@AllArgsConstructor // Required for Builder.
public class ContainerAutomatRuntimeCommand implements ContainerAutomatCommand {

    @NonNull
    private String processingInstanceId;

    @NonNull
    private String processingInput;

    private int processingPosition;


    @Override
    public ContainerAutomatRuntimeCommand nextCommand() {

        if (isProcessingEndCommand()) {
            throw new IndexOutOfBoundsException(processingPosition);
        }

        return ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(processingInstanceId)
                .processingInput(processingInput)
                .processingPosition(processingPosition + 1)
                .build();
    }

    public static ContainerAutomatRuntimeCommand fromProcessingInstance(ContainerAutomatProcessingInstance processingInstance) {

        return ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(processingInstance.getProcessingInstanceId())
                .processingInput(processingInstance.getInput())
                .processingPosition(0)
                .build();
    }

}
