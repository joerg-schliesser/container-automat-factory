/*
 * Copyright 2025 the original author or authors.
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
import de.containerautomat.processing.ContainerAutomatProcessingStep;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeEvent;
import de.containerautomat.processing.runtime.ContainerAutomatRuntimeRequest;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

/**
 * A test suite for testing the methods in {@link PostgreSqlContainerAutomatStorage}
 * that store information about requests and events to a PostgreSQL database.
 * <p/>
 * Note: These tests are rudimentary, as application logic is only simulated in the
 * generated application. In addition, no integration tests for the actual database
 * access are included at this level.
 */
@Testcontainers
@ActiveProfiles("postgresql")
@SpringBootTest(classes = PostgreSqlContainerAutomatStorage.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {PostgreSqlContainerAutomatConfig.class, PostgreSqlContainerAutomatStorageTests.PostgresContainerConfig.class})
class PostgreSqlContainerAutomatStorageTests {

    private static final String TEST_INPUT = "1010";
    private static final String TEST_REQUEST_DESCRIPTION = "Test reqest for ContainerAutomatStorage.";
    private static final String TEST_INSTANCE_ID = UUID.randomUUID().toString();
    private static final int TEST_PROCESSING_POSITION = 1;
    private static final String TEST_STATE_NAME = "S1";
    private static final String TEST_EVENT_DESCRIPTION = "Test event for ContainerAutomatStorage.";


    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16.2-bullseye")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");


    @BeforeAll
    static void beforeAll() {
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    }

    @Profile("postgresql")
    @TestConfiguration
    static class PostgresContainerConfig {

        @Bean
        @Primary
        public javax.sql.DataSource dataSource() {
            return org.springframework.boot.jdbc.DataSourceBuilder.create()
                    .url(postgreSQLContainer.getJdbcUrl())
                    .username(postgreSQLContainer.getUsername())
                    .password(postgreSQLContainer.getPassword())
                    .driverClassName(postgreSQLContainer.getDriverClassName())
                    .build();
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(javax.sql.DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
            emf.setDataSource(dataSource);
            emf.setPackagesToScan("de.containerautomat.processing.postgresql");
            emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

            java.util.Properties props = new java.util.Properties();
            props.setProperty("hibernate.hbm2ddl.auto", "none");
            props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            emf.setJpaProperties(props);

            return emf;
        }

        @Bean
        public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }
    }


    @SpyBean
    PostgreSqlContainerAutomatProcessingInstanceRepository postgresSqlContainerAutomatProcessingInstanceRepository;

    @SpyBean
    PostgreSqlContainerAutomatProcessingStepRepository postgresSqlContainerAutomatProcessingStepRepository;

    @Autowired
    PostgreSqlContainerAutomatStorage postgresSqlContainerAutomatStorage;


    @Test
    void postgresql_containerautomat_storage_available() {
        assertNotNull(postgresSqlContainerAutomatStorage);
    }

    @Test
    void create_processing_instance() {

        var testRequest = new ContainerAutomatRuntimeRequest(TEST_INPUT, TEST_REQUEST_DESCRIPTION);

        Mockito.doAnswer(invocation -> invocation.getArgument(0, PostgreSqlContainerAutomatProcessingInstance.class))
                .when(postgresSqlContainerAutomatProcessingInstanceRepository)
                .save(Mockito.any(PostgreSqlContainerAutomatProcessingInstance.class));

        var testResult = postgresSqlContainerAutomatStorage.createProcessingInstance(testRequest);

        Mockito.verify(postgresSqlContainerAutomatProcessingInstanceRepository, times(1)).save(Mockito.any(PostgreSqlContainerAutomatProcessingInstance.class));

        assertNotNull(testResult);
        assertEquals(TEST_INPUT, testResult.getInput());
        assertNotNull(TEST_REQUEST_DESCRIPTION, testResult.getDescription());
    }

    @Test
    void create_processing_step_for_existing_processing_instance() {

        var testInstance = PostgreSqlContainerAutomatProcessingInstance.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .creationTime(Instant.now())
                .input(TEST_INPUT)
                .description(TEST_REQUEST_DESCRIPTION)
                .build();
        var testTime = Instant.now();
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(ContainerAutomatEvent.EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();

        Mockito.doReturn(Optional.of(testInstance)).when(postgresSqlContainerAutomatProcessingInstanceRepository)
                .findByProcessingInstanceId(TEST_INSTANCE_ID);
        Mockito.doAnswer(invocation -> invocation.getArgument(0, PostgreSqlContainerAutomatProcessingStep.class))
                .when(postgresSqlContainerAutomatProcessingStepRepository)
                .save(Mockito.any(PostgreSqlContainerAutomatProcessingStep.class));

        var testResult = postgresSqlContainerAutomatStorage.createProcessingStep(testTime, testEvent);

        Mockito.verify(postgresSqlContainerAutomatProcessingStepRepository, times(1)).save(Mockito.any(PostgreSqlContainerAutomatProcessingStep.class));

        assertNotNull(testResult);
        assertEquals(TEST_INSTANCE_ID, testResult.getProcessingInstanceId());
        assertEquals(TEST_PROCESSING_POSITION, testResult.getProcessingPosition());
        assertEquals(testEvent.currentInputSymbol().orElseThrow(), testResult.getInputSymbol());
        assertEquals(TEST_STATE_NAME, testResult.getStateName());
        assertEquals(testTime, testResult.getStartTime());
        assertEquals(ContainerAutomatProcessingStep.createStepResultFromEvent(ContainerAutomatEvent.EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS), testResult.getStepResult());
        assertEquals(TEST_EVENT_DESCRIPTION, testResult.getDescription());
    }

    @Test
    void create_processing_step_for_not_existing_processing_instance() {

        var testTime = Instant.now();
        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(ContainerAutomatEvent.EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS)
                .eventTime(Instant.now())
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_EVENT_DESCRIPTION)
                .build();

        Mockito.doReturn(Optional.empty()).when(postgresSqlContainerAutomatProcessingInstanceRepository)
                .findByProcessingInstanceId(Mockito.any(String.class));

        var resultError = assertThrows(IllegalArgumentException.class, () -> postgresSqlContainerAutomatStorage.createProcessingStep(testTime, testEvent));

        assertEquals(PostgreSqlContainerAutomatStorage.ERROR_MESSAGE_UNKNOWN_PROCESSING_INSTANCE_ID.formatted(TEST_INSTANCE_ID), resultError.getMessage());
    }

}
