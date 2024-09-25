
## Table of contents

- [Quickstart](#quickstart)
- [Overall requirements](#overall-requirements)
- [Building the generated application](#building-the-generated-application)
  - [Building Docker images for the entry and state containers](#building-docker-images-for-the-entry-and-state-containers)
  - [Building JAR files for the entry and state programs](#building-jar-files-for-the-entry-and-state-programs)
- [Running the generated application](#running-the-generated-application)
  - [Running the application with Docker Compose](#running-the-application-with-docker-compose)
  - [Running the application in the local environment](#running-the-application-in-the-local-environment)
  - [Deploying and running the application on Kubernetes](#deploying-and-running-the-application-on-kubernetes)
- [Accessing and using the application](#accessing-and-using-the-application)
  - [Sending requests via the REST interface](#sending-requests-via-the-rest-interface)
  - [Tracking the processing of requests](#tracking-the-processing-of-requests)
  - [Evaluating the results of the processing](#evaluating-the-results-of-the-processing)
- [Removing the application](#removing-the-application)
  - [Removing the application from Docker Compose](#removing-the-application-from-docker-compose)
  - [Removing the application in the local environment](#removing-the-application-in-the-local-environment)
  - [Removing the application from Kubernetes](#removing-the-application-from-kubernetes)

## Quickstart

**Step 1: Build and start the generated application with Docker Compose.**

    # Open a console in the project directory and go to the dockerbuild subdirectory.
    container-automat> cd dockerbuild

    # Create the Docker images for entry and state containers.
    dockerbuild> dockerbuild-container-automat.cmd

    # Change to the dockercompose subdirectory.
    dockerbuild> cd ..\dockercompose

    # Start the application with Docker Compose.
    dockercompose> compose-container-automat.cmd up -d

    # Check the status of the containers until all are running 'healthy'.
    # Note: This can take more than two minutes, depending on the combination of services.
    dockercompose> compose-container-automat.cmd ps

**Step 2: Open the Swagger UI of the application with the following URL
in the browser and use it to send requests to the DFA.**

    http://localhost:9997/swagger-ui/index.html

**Step 3: Try further Docker Compose commands.**

    # For instance, show the container logs.
    dockercompose> compose-container-automat.cmd logs

    # List more examples by omitting the parameters for command and option.
    dockercompose> compose-container-automat.cmd

    # Stop and remove the application in Docker Compose.
    dockercompose> compose-container-automat.cmd down

Note: There are corresponding .sh files for Linux for the Windows .cmd
files mentioned.

## Overall requirements

- Docker
- Java JDK 21
- Spring Boot 3.3.3
- Maven 3.9.6

Note: Depending on the procedure for running the application, only a subset
of the requirements must be fulfilled locally. Details can be found in the
following explanations.

## Building the generated application

The DFA, on which the generated application as a whole is based, is
represented at runtime by two different Java programs.

- The entry program provides a REST interface for receiving processing
requests for the DFA.
- The state program processes the individual symbols of the input strings
step by step. For that, an instance of the state program is started for
each state of the DFA.

Depending on whether the generated application is to be executed locally or
as a cluster with Docker Compose or on Kubernetes, the Java programs must be
available in the form of an executable JAR file or in a Docker container.

### Building Docker images for the entry and state containers

The root directory of the generated application contains the _dockerbuild_
subdirectory with files for creating Docker images. This also includes
script files for starting the Docker build process in the Windows command
prompt or the Linux shell.

    # Build the Docker images for entry and state containers under Windows.
    dockerbuild> dockerbuild-container-automat.cmd

    # Build the Docker images for entry and state containers under Linux.
    dockerbuild$ ./dockerbuild-container-automat.sh

Both images are created using a multi-stage build, in which a Maven base
image is used in the first stage to build the application directly from
the source, so that neither Maven nor a JDK needs to be available on the
local system.

The generated images contain a JRE (Java Runtime Environment) and the
respective Java program.

### Building JAR files for the entry and state programs

The root directory of the generated application is also the project
directory of a multi-module Maven project for the two Java programs
that represent the states of the DFA.

To be able to build the Java programs locally, Maven and a JDK (Java
Development Kit) must be available in the versions mentioned above or
a compatible version. Then you can call maven with the following
command:

    mvn clean package

This creates the executable JAR files in the following target
subdirectories:

    The entry program with the REST interface for receiving requests:
    container-automat-entry/target/container-automat-entry.jar

    The state program for processing input symbols using messaging:
    container-automat-state/target/container-automat-state.jar

Notes:

- If the scripts described below are used to run the application in the
local environment, Maven is called automatically if the JAR files are not
available.
- When running with Docker Compose or on Kubernetes, the JAR files are
not required in this form. As described above, the Java programs are also
created implicitly with the container images.

## Running the generated application

The generated application as a whole implements a DFA. The following
services are started for this purpose:

- An instance of the entry program receives input strings as requests for
processing by the DFA via a REST interface.
- For each state of the DFA, an instance of the state program handles
individual symbols of the input string, simulating application-specific
processing that takes some time and whose duration depends to a certain
extent on chance.
- A message broker is used to assign input symbols to the state programs
in the form of command messages for processing.
- In addition, event messages are published for any recipient for logging
purposes via the message broker.
- A database is used to permanently store requests for processing by the
DFA and information about the individual processing steps.
- Optional additional services process the events sent by the message
broker for logging. (Note: The inclusion of these optional services must
already have been selected when the application was created).

### Running the application with Docker Compose

The root directory of the generated application contains the _dockercompose_
subdirectory with files for running the generated application with Docker
Compose. This also includes script files for calling Docker Compose in the
Windows command prompt or the Linux shell.

    # Call Docker Compose for the generated application under Windows.
    dockercompose> compose-container-automat.cmd

    # Call Docker Compose for the generated application under Linux.
    dockercompose$ ./compose-container-automat.sh

If the script file is called without further parameters, a list of examples
with specific parameters for starting and stopping the application and for
some other Compose actions is displayed.

    dockercompose> compose-container-automat.cmd

    Missing command and option for Docker Compose.
    Format: compose-container-automat.cmd [command] [option].
    For instance:
    compose-container-automat.cmd up -d
    compose-container-automat.cmd logs
    compose-container-automat.cmd logs -f
    compose-container-automat.cmd ps
    compose-container-automat.cmd stop
    compose-container-automat.cmd ps -a
    compose-container-automat.cmd start
    compose-container-automat.cmd down

The commands and options are passed from the script to Docker Compose.
Here are some concrete examples:

    # Create the application in Compose and run it detached.
    dockercompose> compose-container-automat.cmd up -d

    # Check the status of the containers.
    dockercompose> compose-container-automat.cmd ps

    # Stop the application in Compose.
    dockercompose> compose-container-automat.cmd stop

    # Restart the application in Compose.
    dockercompose> compose-container-automat.cmd start

Note: Before the application can actually be used, the status of all
containers must be 'healthy'. This can take more than two minutes,
depending on the combination of services.

For details on how Docker Compose works in general and on the commands and
options, please refer to the Docker Compose documentation or relevant
literature.

### Running the application in the local environment

The root directory of the generated application contains the _localrun_
subdirectory with files for running the generated application in the local
environment.

The message broker, the database, and other optional services must first
be started in Docker containers before starting the Java programs that
represent the states of the DFA. For this purpose, the directory
contains the following script files for the Windows command prompt and
the Linux shell.

    # Create and/or start the service containers with Docker under Windows.
    localrun> dockercreate-container-automat.cmd

    # Create and/or start the service containers with Docker under Linux.
    localrun$ ./dockercreate-container-automat.sh

The scripts create a network and the service containers in Docker if they
do not yet exist. The containers are then started.

Note: If the optional services were included when the application was
created, the start process may take some time. Before some services are
started, the system also checks whether other necessary services are
already available. These dependency checks are recorded by corresponding
log outputs in the console.

The Java programs that represent the states of the DFA can then be started.
Therefor the directory contains additional script files for the Windows
command prompt and the Linux shell.

    # Start the Java programs for the generated application under Windows.
    localrun> runlocal-container-automat.cmd

    # Start the Java programs for the generated application under Linux.
    localrun$ ./runlocal-container-automat.sh

The Java programs are implemented with Spring Boot. The start process is
logged by corresponding log outputs in the console.

### Deploying and running the application on Kubernetes

The root directory of the generated application contains the _kubernetes_
subdirectory with files for deploying the generated application to Kubernetes.
This includes the following script files to call _kubectl kustomize_ and
_kubectl apply_ in the Windows command prompt or Linux shell to create and
apply the Kubernetes configuration of the application.

    # Call kubectl under Windows to create and apply the Kubernetes configuration of the application.
    kubernetes> k8s-create-container-automat.cmd

    # Call kubectl under Linux to create and apply the Kubernetes configuration of the application.
    kubernetes$ ./k8s-create-container-automat.sh

Notes:

- The application's pods contain initContainers to start the service
containers based on the functional dependencies and avoid restarts when
initializing the cluster. It may take a few minutes for the application
as a whole to be ready.
- The steps required to call the application's REST interface from outside
the Kubernetes cluster depend on the environment in which Kubernetes itself
is used.
- To run the generated application in a local test or development
environment for Kubernetes, you can use _minikube_. Information on
_minikube_ can be found in the corresponding product documentation.
Available at https://minikube.sigs.k8s.io/docs/ and on GitHub at
_kubernetes/minikube_. (Status: Septembre 2024)

## Accessing and using the application

### Sending requests via the REST interface

Requests to the DFA, which is implemented by the generated application,
are received via a REST interface under port 9997. Calls are possible
via an integrated Swagger UI, which can be opened in the browser with
the following URL:

    http://localhost:9997/swagger-ui/index.html

Using the Swagger UI should be largely self-explanatory. For details on
how it works in general, please refer to the documentation or relevant
information on the internet.

### Tracking the processing of requests

The progress of the request processing can be tracked via the log
outputs of the Java programs.

The log outputs of the state programs record the input symbols processed
in the corresponding state of the DFA and their consequences. For example,
the successful processing of a symbol and the continuation with the
subsequent state for the next symbol, or an abort due to a non-accepted
input symbol.

At the same time, the entry program receives the events that are generally
sent via the message broker for logging and outputs them in the console.

If the Java programs are executed in the local environment, the messages
appear in the console that is connected to the program, be it the command
prompt under Windows, the shell under Linux or the console of a development
environment, if the programs are started there.

When running with Docker Compose or Kubernetes, the messages are contained
in the log of the container in which the program is running.

### Evaluating the results of the processing

Data records are stored permanently in the database selected when the
application was generated, both for the requests and for the individual
processing steps, so that subsequent evaluations are possible using
these data records.

The names of the tables or documents in the database contain the application
name that was specified during generation. For details, please refer to the
Java classes in the subpackage _mongodb_, _postgresql_, or _redis_,
depending on which database was selected.

## Removing the application

### Removing the application from Docker Compose

To remove the application from Docker Compose, you can call the same script
file that was used for starting it in the _dockercompose_ subdirectory.

    # Remove the application and the volumes from Compose under Windows.
    dockercompose> compose-container-automat.cmd down -v

    # Remove the application and the volumes from Compose under Linux.
    dockercompose$ ./compose-container-automat.sh down -v

Please note that the -v (--volumes) option has to be passed to also remove
the volumes. To retain the volumes, omit the -v (--volumes) option.

### Removing the application in the local environment

To remove the application in the local environment, first close the Java
programs manually. You can then stop and remove the Docker containers.
Another script file is available in the _localrun_ subdirectory for this
purpose.

    # Remove the Docker containers and optionally the volumes under Windows.
    localrun> dockerdelete-container-automat.cmd

    # Remove the Docker containers and optionally the volumes under Linux.
    localrun$ ./dockerdelete-container-automat.sh

Note: Removing the volumes is optional. This is queried during the processing
of the script.

### Removing the application from Kubernetes

The _kubernetes_ subdirectory contains the following script files to
call _kubectl delete_ in the Windows command prompt or the Linux shell in
order to delete the application from Kubernetes.

    # Call kubectl under Windows to delete the application from Kubernetes.
    kubernetes> k8s-delete-container-automat.cmd

    # Call kubectl under Linux to delete the application from Kubernetes.
    kubernetes$ ./k8s-delete-container-automat.sh
