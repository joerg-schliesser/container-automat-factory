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
package de.containerautomat.processing.postgresql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.containerautomat.processing.ContainerAutomatProcessingInstance;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;

/**
 * An implementation of the data type {@link ContainerAutomatProcessingInstance}
 * for storing objects of this type in a PostgreSQL database when using Spring
 * Data JPA.
 */
@Entity
@Table(name = "container-automat_processing_instance", indexes = {
        @Index(name = "idx_instance_creation_id", columnList = PostgreSqlContainerAutomatProcessingInstance.COLUMN_CREATION_TIME + ", "
                + PostgreSqlContainerAutomatProcessingInstance.COLUMN_PROCESSING_INSTANCE_ID)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PostgreSqlContainerAutomatProcessingInstance implements ContainerAutomatProcessingInstance {

    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_PROCESSING_INSTANCE_ID = "processing_instance_id";
    public static final String COLUMN_CREATION_TIME = "creation_time";
    public static final String COLUMN_INPUT = "input";
    public static final String COLUMN_DESCRIPTION = "description";

    public static final int MAX_LENGTH_PROCESSING_INSTANCE_ID = 40;
    public static final int MAX_LENGTH_INPUT = 500;
    public static final int MAX_LENGTH_DESCRIPTION = 200;

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = COLUMN_KEY)
    private Long key;

    @Column(name = COLUMN_PROCESSING_INSTANCE_ID, length = MAX_LENGTH_PROCESSING_INSTANCE_ID, unique = true, nullable = false)
    private String processingInstanceId;

    @Column(name = COLUMN_CREATION_TIME, nullable = false)
    private Instant creationTime;

    @Column(name = COLUMN_INPUT, length = MAX_LENGTH_INPUT, nullable = false)
    private String input;

    @Column(name = COLUMN_DESCRIPTION, length = MAX_LENGTH_DESCRIPTION, nullable = false)
    private String description;


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostgreSqlContainerAutomatProcessingInstance other = (PostgreSqlContainerAutomatProcessingInstance) o;
        return Objects.equals(getProcessingInstanceId(), other.getProcessingInstanceId());
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(getProcessingInstanceId());
    }

}
