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
 * The declaration of a data type that represents an instance of a request
 * used to process an input string by the DFA implemented by the generated
 * application.
 * <p/>
 * Instances of requests are persistently stored in a database at the
 * beginning of the processing.
 */
public interface ContainerAutomatProcessingInstance {

    String getProcessingInstanceId();
    
    Instant getCreationTime();
    
    String getInput();
    
    String getDescription();

}
