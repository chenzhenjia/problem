plugins {

}

dependencies {
    val jacksonVersion: String by project
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    compileOnly("org.jetbrains:annotations:21.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}