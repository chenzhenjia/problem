rootProject.name = "problem"
include("core")
include("spring")
//include("spring-mvc")
include("spring-boot-starter")
include("spring-mvc-example")
include("spring-flux-example")
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
        gradlePluginPortal()
    }
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    plugins {
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("org.springframework.boot") version springBootVersion
    }
}
