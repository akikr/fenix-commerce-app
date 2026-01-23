# FenixCommerce: Multi-Tenant Commerce Ingestion Service

The objective of this project is to design and implement a Java-based backend system that
supports multiple **organizations**/**tenants**, where each organization can operate multiple
eCommerce **websites**/**stores**. These websites push **order**, **fulfilment**, and **tracking** 
information into a centralized platform. The system is expected to be scalable, extensible, and production-ready,
following modern Java and microservices best practices.

## Project Requirements

To build and run this project, you will need the following:

* Java 17 or later
* Maven 3.9.x or later
* Docker and Docker Compose (for running containerized Kafka broker)

## Dependencies

This project uses the following dependencies:

* **Spring Boot:** The core framework for building the application.
* **Spring Web:** The core framework for RESTful microservices.
* **Spring Data JPA:** Provides the necessary components for storing the data in a RDBMS; **MySQL**.
* **Spring Boot Testcontainers:** For integration and unit testing with MySQL container using docker.
* **Spring Boot Actuator:** Adds production-ready features like health checks and metrics.

For a complete list of dependencies, please see the `pom.xml` file.

## Getting Started

To get started with this project, you can clone the repository to your local machine. Once you have cloned the repository, you can import it into your favorite IDE.

### Environment Setup

* The project uses SDKMAN for managing Java and Maven versions.
* Initialize your development environment using **SDKMAN** CLI and sdkman env file [`sdkmanrc`](.sdkmanrc)

```shell
sdk env install
sdk env
```

#### Note: To install SDKMAN refer: [sdkman.io](https://sdkman.io/install)

---

### Build project

- Using spring-boot maven plugin

```shell
sdk env 
./mvnw clean package
```

- Using docker compose

```shell
docker compose -f compose-dev.yml build
```

### Run project

- Using spring-boot maven plugin

```shell
sdk env
./mvnw spring-boot:run
```

It uses [`compose.yml`](compose.yml) to start a MySQL container along with the application with help of spring-boot docker-compose support.


- Using docker compose

```shell
docker compose -f compose-dev.yml up
```

It will build the docker image of the application using [`Dockerfile`](Dockerfile) and run it using the 
[`compose-dev.yml`](compose-dev.yml) file to start a MySQL database in docker compose setup.

---

### See OpenAPI Swagger Documentation: (localhost)

- [swagger-ui](http://localhost:8080/app/swagger-ui.html)
- [swagger-api-docs](http://localhost:8080/app/v3/api-docs)

---

### Project Structure

- For detailed domain-relation and ER diagrams, please refer to the [Docs](./docs) directory

The project follows a standard Maven project structure:

```
.
├── README.md                 # README file
│── docs/                     # Documentation directory
│
├── src/                      # Source code
│   ├── main/
│   │   ├── java/
│   │   │   └── io/akikr/app/
│   │   │       ├── shared/         # Common configurations, utilities, and shared resources
│   │   │       ├── tenant/         # Domain-related packages for organizations/tenants
│   │   │       │   ├── controller/ # REST controllers
│   │   │       │   ├── service/    # Business logic
│   │   │       │   ├── repository/ # Data access layer
│   │   │       │   ├── entity/     # JPA entities
│   │   │       │   └── model/      # DTOs and other models
│   │   │       │
│   │   │       ├── store/        # Domain-related packages for websites/stores
│   │   │       │   └── ...
│   │   │       ├── order/        # Domain-related packages for orders
│   │   │       │   └── ...
│   │   │       ├── fulfilment/   # Domain-related packages for fulfilments
│   │   │       │   └── ...
│   │   │       └── tracking/     # Domain-related packages for tracking
│   │   │           └── ...
│   │   │
│   │   └── resources/        # Application resources
│   │
│   └── test/
│       ├── java/             # Test source files
│       └── resources/        # Test resources
│
├── .git/                     # Git directory
├── .gitignore                # Git ignore file
├── .gitattributes            # Git attributes file
├── pom.xml                   # Maven project configuration
├── .mvn/                     # Maven wrapper configuration
├── mvnw                      # Maven wrapper script (for Unix-like systems)
├── mvnw.cmd                  # Maven wrapper script (for Windows)
├── .sdkmanrc                 # SDKMAN configuration file
├── Dockerfile                # Dockerfile for building the application image
├── compose.yml               # Docker Compose file for default environment
├── compose-dev.yml           # Docker Compose file for development environment
└── target/                   # Compiled code and packages

```

---

Clean coding! ✌️
