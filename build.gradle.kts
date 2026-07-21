plugins {
	java
	kotlin("jvm") version "2.1.0"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.road.to"
version = "0.0.1-SNAPSHOT"
description = "Payments service for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

kotlin {
	jvmToolchain(21)
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}

    configureEach {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
}

repositories {
	mavenCentral()
}

dependencies {

    // Spring boot
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // jetty
    implementation("org.springframework.boot:spring-boot-starter-jetty")

    // Spring Kafka
	implementation("org.springframework.kafka:spring-kafka")

    // PostgreSQL
    implementation("org.postgresql:postgresql:42.6.0")

    // Liquibase (DB migrations live in this repo, version managed by the Boot BOM)
    implementation("org.liquibase:liquibase-core")

    // Springdoc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // mapstruct
    implementation("org.mapstruct:mapstruct:${property("mapstructVersion")}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

    // Lombok
	compileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${property("lombokMapstructBindingVersion")}")

    // docker-compose
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // Spring test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")

    // Kotlin test (mockito-kotlin per the team unit-test convention)
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    // JUnit
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
