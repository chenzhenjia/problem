plugins {
    base
    id("io.spring.dependency-management") apply false
    id("org.springframework.boot") apply false
}

subprojects {
    apply(plugin = "checkstyle")

    configure<CheckstyleExtension> {
        toolVersion = "8.43"
        maxErrors = 0
        maxWarnings = 0
        config = rootProject.resources.text.fromFile("config/checkstyle/google_checks.xml")
        configProperties["org.checkstyle.google.suppressionfilter.config"] =
            rootDir.resolve("config/checkstyle/checkstyle-suppressions.xml").absoluteFile
    }

    repositories {
        mavenLocal()
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        mavenCentral()
    }

}
val libraryProject = setOf(
    "core", "spring", "spring-boot-starter",
)
configure(subprojects.filter { libraryProject.contains(it.name) }) {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
//    configurations {
//        create("optional") {
//            attributes {
//                attribute(
//                    Usage.USAGE_ATTRIBUTE,
//                    project.objects.named(Usage::class.java, Usage.JAVA_RUNTIME)
//                )
//            }
//            val sourceSets: SourceSetContainer by project
//            sourceSets.forEach { sourceSet ->
//                sourceSet.compileClasspath = sourceSet.compileClasspath.plus(this)
//                sourceSet.runtimeClasspath = sourceSet.runtimeClasspath.plus(this)
//            }
//            tasks.withType(Javadoc::class.java).forEach { javadoc ->
//                javadoc.classpath = javadoc.classpath.plus(this)
//            }
//        }
//    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }
    tasks {
        withType(Javadoc::class).configureEach {
            (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
        }
        withType(Javadoc::class) {
            isFailOnError = false
            options.encoding = "UTF-8"
        }
        withType(JavaCompile::class) {
            options.isIncremental = true
            options.encoding = "UTF-8"
            options.compilerArgs.add("-Xlint:unchecked")
            options.compilerArgs.add("-Xlint:deprecation")
        }

    }

    tasks.register<Jar>("sourcesJar") {
        val sourceSets: SourceSetContainer by project
        from(sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }
    tasks.register<Jar>("javadocJar") {
        val javadoc: Javadoc by tasks
        from(javadoc)
        archiveClassifier.set("javadoc")
    }
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                pom {
                    name.set("problem")
                    description.set("")
                    url.set("https://github.com/chenzhenjia/problem")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("chzhenjia")
                            name.set("陈圳佳")
                            email.set("chzhenjia@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:https://github.com/chenzhenjia/problem.git")
                        developerConnection.set("scm:https://github.com/chenzhenjia/problem.git")
                        url.set("https://github.com/chenzhenjia/problem")
                    }
                }
                from(components["java"])
                val sourcesJar by tasks
                val javadocJar by tasks
                artifact(sourcesJar)
                artifact(javadocJar)
            }
        }
        repositories {
            maven {
                credentials {
                    val mavenCentralUsername: String? by project
                    val mavenCentralPassword: String? by project
                    username = mavenCentralUsername
                    password = mavenCentralPassword
                }
                val releasesRepoUrl =
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl =
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if ("$version".endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            }
        }
    }
}

val springDependencyManagement = setOf(
    "spring", "spring-boot-starter",
)
configure(subprojects.filter { springDependencyManagement.contains(it.name) }) {
    apply(plugin = "io.spring.dependency-management")
    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }
//    dependencies {
//        val api by configurations
//        api(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
//    }
}
