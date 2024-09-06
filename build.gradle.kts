plugins {
    id("java-library")
    id("maven-publish")
    signing
}

subprojects {
    group = "org.jsignal"
    version = "0.0.3"

    repositories {
        mavenCentral()
        maven {
            url = uri("https://releases.usethesource.io/maven")
        }
    }

    if (project.name == "examples") {
        return@subprojects
    }

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    java {
        withSourcesJar()
        withJavadocJar()
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