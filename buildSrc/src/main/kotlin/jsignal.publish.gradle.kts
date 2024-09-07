plugins {
    id("jsignal.common")
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