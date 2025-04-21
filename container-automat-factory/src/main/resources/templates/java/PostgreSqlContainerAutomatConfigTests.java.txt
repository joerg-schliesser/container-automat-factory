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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test suite to test that the {@link PostgreSqlContainerAutomatConfig}
 * can be loaded and the JPA related beans are available.
 */
@Testcontainers
@ActiveProfiles("postgresql")
@SpringBootTest(classes = PostgreSqlContainerAutomatConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = PostgreSqlContainerAutomatConfigTests.PostgresContainerConfig.class)
class PostgreSqlContainerAutomatConfigTests {

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
        public DataSource dataSource() {
            return org.springframework.boot.jdbc.DataSourceBuilder.create()
                    .url(postgreSQLContainer.getJdbcUrl())
                    .username(postgreSQLContainer.getUsername())
                    .password(postgreSQLContainer.getPassword())
                    .driverClassName(postgreSQLContainer.getDriverClassName())
                    .build();
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
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


    @Autowired
    DataSource dataSource;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    PostgreSqlContainerAutomatConfig postgreSqlContainerAutomatConfig;

    @Autowired
    PostgreSqlContainerAutomatProcessingInstanceRepository postgreSqlContainerAutomatProcessingInstanceRepository;

    @Autowired
    PostgreSqlContainerAutomatProcessingStepRepository postgreSqlContainerAutomatProcessingStepRepository;

    @Test
    void data_source_is_configured() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            assertTrue(connection.isValid(1));
        }
    }

    @Test
    void application_context_loads() {
        assertNotNull(applicationContext);
    }

    @Test
    void entity_manager_factory_is_available() {
        assertThat(entityManagerFactory).isNotNull();
    }

    @Test
    void entity_manager_is_available() {
        assertNotNull(entityManager);
    }

    @Test
    void postgresql_containerautomat_config_loads() {
        assertNotNull(postgreSqlContainerAutomatConfig);
    }

    @Test
    void postgresql_containerautomat_processing_instance_repository_available() {
        assertNotNull(postgreSqlContainerAutomatProcessingInstanceRepository);
    }

    @Test
    void postgresql_containerautomat_processing_step_repository_available() {
        assertNotNull(postgreSqlContainerAutomatProcessingStepRepository);
    }

}
