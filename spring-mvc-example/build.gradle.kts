plugins {
    java
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":spring-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
