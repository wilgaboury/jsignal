plugins {
    id("java-library")
    id("maven-publish")
    signing
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

subprojects {
    if (project.name == "examples") {
        apply(plugin = "java")
    } else {
        apply(plugin = "java-library")
    }

    group = "org.jsignal"
    version = "0.0.3"

    repositories {
        mavenCentral()
        maven {
            url = uri("https://releases.usethesource.io/maven")
        }
    }

    if (project.name != "rx") {
        dependencies {
            implementation("org.slf4j:slf4j-api:2.0.16")
            implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
            testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        }
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    if (project.name == "examples") {
        return@subprojects
    }

    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    java {
        withSourcesJar()
        withJavadocJar()

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    publishing {
        repositories {
            maven {
                url = uri(layout.buildDirectory.dir("repo"))
            }
        }

        publications {
            create<MavenPublication>("mavenCentral") {
                if (project.name != "rx") {
                    from(components["java"])
                }

                pom {
                    name = "JSignal"
                    description = "Declarative desktop GUI library"
                    url = "http://jsignal.org"
                    licenses {
                        license {
                            name = "MIT License"
                            url = "https://opensource.org/license/mit"
                        }
                    }
                    developers {
                        developer {
                            id = "wgaboruy"
                            name = "Wil Gaboury"
                            email = "wgaboury@gmail.com"
                        }
                    }
                    scm {
                        connection = "scm:git:git://wilgaboury/jsignal.git"
                        developerConnection = "scm:git:ssh://wilgaboury/jsignal.git"
                        url = "https://github.com/wilgaboury/jsignal"
                    }
                    issueManagement {
                        url = "https://github.com/wilgaboury/jsignal/issues"
                    }
                }
            }
        }
    }

    signing {
        sign(publishing.publications["mavenCentral"])
    }
}

val projectNames = subprojects.map { it.name }

tasks.register<Javadoc>("allJavadoc") {
    group = "documentation"
    source =
        files(*(projectNames.flatMap { project(it).java.sourceSets["main"].allJava }.toTypedArray())).asFileTree
    classpath = files(*(projectNames.flatMap { project(it).java.sourceSets["main"].compileClasspath }
        .toTypedArray())).asFileTree
    setDestinationDir(file("docs/public/javadoc"))
}