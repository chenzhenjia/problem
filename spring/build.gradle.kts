plugins {
    java
}
//configurations.forEach {
//    it.
//    it.exclude("commons-logging", "commons-logging")
//}

dependencies {
    api(project(":core"))
    api("org.springframework:spring-web") {
        exclude("commons-logging", "commons-logging")
        exclude("org.springframework", "spring-jcl")
    }
    compileOnly("org.springframework.security:spring-security-web")
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    compileOnly("jakarta.validation:jakarta.validation-api")
    compileOnly("org.springframework:spring-webflux")
    compileOnly("org.springframework:spring-webmvc")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("org.jetbrains:annotations:21.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
