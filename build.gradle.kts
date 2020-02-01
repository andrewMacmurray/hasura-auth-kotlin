import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.apollographql.apollo") version "1.3.0"
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
}

group = "com.andrew"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    jcenter()
    mavenCentral()
}

apollo {
    generateKotlinModels.set(true)
}

dependencies {
//    implementation("com.graphql-java:graphql-java-spring-boot-starter-webflux:1.0")
//    implementation("com.graphql-java:graphiql-spring-boot-starter:5.0.2")
//    implementation("com.graphql-java:graphql-java-tools:5.2.4")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.auth0:java-jwt:3.9.0")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.apollographql.apollo:apollo-runtime:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.3.3")
    implementation("com.apollographql.apollo:apollo-coroutines-support:1.3.0")
    compileOnly("org.jetbrains:annotations:13.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
