plugins {
    id("java-library")
    id("maven-publish")
    signing
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    group = "org.jsignal"
    version = "0.0.1"

    java {
        withSourcesJar()
        withJavadocJar()
    }

    repositories {
        mavenCentral()
        maven {
            url = uri("https://releases.usethesource.io/maven")
        }
        mavenLocal()
    }

    publishing {
        repositories {
            maven {
                url = uri(layout.buildDirectory.dir("repo"))
            }
        }

        publications {
            create<MavenPublication>("mavenCentral") {
                from(components["java"])

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