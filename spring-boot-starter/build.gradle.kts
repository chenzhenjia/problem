plugins {
    java
    id("org.springframework.boot")
}
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>() {
    enabled = false
}
tasks {
    jar {
        enabled = true
        archiveClassifier.set("")
    }
}
dependencies {
    api(project(":spring"))
    api("org.springframework.boot:spring-boot-autoconfigure") {
        exclude("commons-logging", "commons-logging")
        exclude("org.springframework", "spring-jcl")
    }
    compileOnly("org.springframework.security:spring-security-core")
    compileOnly("org.springframework:spring-webflux")
    compileOnly("org.springframework:spring-webmvc")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("jakarta.validation:jakarta.validation-api:3.0.0")
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
