# Container-Automat Factory

## A generator for examples of service-oriented applications

Container-Automat is a generator of service-oriented applications for
demonstration purposes that are based on Deterministic Finite Automata
(abbreviated DFA).

The DFA is used to simulate the application logic of a service-oriented
application. In addition to the application-specific components, the
application also contains a message broker, a database, and optional
components for logging the processing of requests. So relatively complex
conglomerates of services are possible.

The idea behind the Container-Automat project is to be able to easily
generate sample applications

- to explore the interaction of services in Docker containers.
- in order to gain experience with a message broker or a database.
- that can serve as a starting point for showcases or prototypes.

## Quickstart

**Step 1: Build a Docker image for the factory application.**

    container-automat> builddocker-container-automat-factory.cmd

**Step 2: Run a Docker container with the factory application.**

    container-automat> rundocker-container-automat-factory.cmd

**Step 3: Open the factory UI in a browser with the following URL.**

    http://localhost:9999/v1/dfa-editor.html

Note: There are corresponding .sh files for Linux for the Windows .cmd
files mentioned.

## Features of generated applications

- The application as a whole implements the DFA.
- For each state of the DFA there is a service, which is implemented in Java with Spring Boot.
- Requests are received in the form of input strings for the DFA via a REST interface.
- For state transitions, commands are sent to the service of the target state using a message broker.
- Processing can be logged using events also sent via the message broker.
- Optionally, these events can be processed with Elasticsearch, Logstash, and Kibana.
- Information about requests and processing steps is stored in a database.
- Both the message broker and the database can be selected from several systems.
  - Supported message brokers: ActiveMQ Artemis, Kafka, and RabbitMQ.
  - Supported databases: MongoDB, PostgreSQL, and Redis. 
- The message broker, the database, and the optional ELK services are started in Docker containers.
- The Java programs for the states of the DFA can be started both locally and in Docker containers.
- The entire application can be executed as a cluster with Docker Compose or on Kubernetes.

## Requirements

- Docker
- Java JDK 21
- Spring Boot 3.3.3
- Maven 3.9.6

## Building and launching the Factory with Docker

The Dockerfile _container-automat-factory.dockerfile_ in the root directory of
the factory project can be used to create a Docker image that contains a Java
runtime and the factory application.

To start the Docker build process, the project directory contains both a CMD
file for the Windows command prompt and an SH file for the Linux shell.

    # Build the Docker image of the factory under Windows.
    container-automat> builddocker-container-automat-factory.cmd

    # Build the Docker image of the factory under Linux.
    container-automat$ ./builddocker-container-automat-factory.sh

This is a multi-stage Docker build process in which a Maven base image is used
in the first stage to build the application directly from the Java source, so
that neither Maven nor a JDK needs to be available on the local system.

Once the build process is complete, a Docker container with the factory app
can be started by calling the following CMD file under Windows or SH file under
Linux. (These files are also located in the root directory of the project.)

    # Start a Docker container with the factory under Windows.
    container-automat> rundocker-container-automat-factory.cmd

    # Start a Docker container with the factory under Linux.
    container-automat$ ./rundocker-container-automat-factory.sh

The HTML UI of the factory application can then be called up locally
under port 9999 in a browser with the following URL:

    http://localhost:9999/v1/dfa-editor.html

The following commands can be used to stop and restart the Docker container
with the factory app:

    docker stop container-automat-factory
    docker start container-automat-factory

For details on how Docker containers work in general, please refer to the
Docker documentation or relevant literature.

## Documentation

The factory source code contains only a few comments. Essentially, it
should speak for itself. More information about the use of the
_Container-Automat Factory_, about the examples of deterministic finite
automata it contains, and about the Container-Automat project in general
can be found on the project website at:

[www.container-automat.de](https://www.container-automat.de)

## License

The _Container-Automat Factory_ is open source software licensed under
the Apache License, Version 2.0. You may obtain a copy of the license at:

https://www.apache.org/licenses/LICENSE-2.0

