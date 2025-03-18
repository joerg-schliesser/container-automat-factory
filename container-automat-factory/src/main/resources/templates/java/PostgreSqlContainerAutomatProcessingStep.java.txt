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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import de.containerautomat.processing.ContainerAutomatProcessingStep;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;

/**
 * An implementation of the data type {@link ContainerAutomatProcessingStep}
 * for storing objects of this type in a PostgreSQL database when using
 * Spring Data JPA.
 */
@Entity
@Table(name = "container-automat_processing_step", indexes = {
        @Index(name = "idx_instance_steps_position", columnList = PostgreSqlContainerAutomatProcessingStep.COLUMN_PROCESSING_INSTANCE_KEY + ", "
                + PostgreSqlContainerAutomatProcessingStep.COLUMN_PROCESSING_STEP_ID + ", "
                + PostgreSqlContainerAutomatProcessingStep.COLUMN_PROCESSING_POSITION)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PostgreSqlContainerAutomatProcessingStep implements ContainerAutomatProcessingStep {

    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_PROCESSING_STEP_ID = "processing_step_id";
    public static final String COLUMN_PROCESSING_POSITION = "processing_position";
    public static final String COLUMN_INPUT_SYMBOL = "input_symbol";
    public static final String COLUMN_STATE_NAME = "state_name";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_STEP_RESULT = "step_result";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_PROCESSING_INSTANCE_KEY = "processing_instance_key";

    public static final int MAX_LENGTH_PROCESSING_STEP_ID = 40;
    public static final int MAX_LENGTH_DESCRIPTION = 200;
    public static final int MAX_LENGTH_STEP_RESULT = 50;

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = COLUMN_KEY)
    private Long key;

    @Column(name = COLUMN_PROCESSING_STEP_ID, length = MAX_LENGTH_PROCESSING_STEP_ID, unique = true, nullable = false)
    private String processingStepId;

    @Column(name = COLUMN_PROCESSING_POSITION, nullable = false)
    private int processingPosition;

    @Column(name = COLUMN_INPUT_SYMBOL, length = DeterministicFiniteAutomaton.MAX_LENGTH_INPUT_SYMBOL, nullable = false)
    private String inputSymbol;

    @Column(name = COLUMN_STATE_NAME, length = DeterministicFiniteAutomaton.MAX_LENGTH_STATE_NAME, nullable = false)
    private String stateName;

    @Column(name = COLUMN_START_TIME, nullable = false)
    private Instant startTime;

    @Column(name = COLUMN_END_TIME, nullable = false)
    private Instant endTime;

    @Column(name = COLUMN_STEP_RESULT, length = MAX_LENGTH_STEP_RESULT, nullable = false)
    @Enumerated(EnumType.STRING)
    private StepResult stepResult;

    @Column(name = COLUMN_DESCRIPTION, length = MAX_LENGTH_DESCRIPTION, nullable = false)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = COLUMN_PROCESSING_INSTANCE_KEY)
    @ToString.Exclude
    private PostgreSqlContainerAutomatProcessingInstance postgreSqlContainerAutomatProcessingInstance;


    @Override
    public String getProcessingInstanceId() {
        return postgreSqlContainerAutomatProcessingInstance != null ? postgreSqlContainerAutomatProcessingInstance.getProcessingInstanceId() : "";
    }

    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostgreSqlContainerAutomatProcessingStep other = (PostgreSqlContainerAutomatProcessingStep) o;
        return Objects.equals(getProcessingStepId(), other.getProcessingStepId());
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(getProcessingStepId());
    }

}
