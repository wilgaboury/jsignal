plugins {
    id("jsignal.common")
}

val projectNames = listOf("prop", "rx", "std", "ui")

tasks.register<Javadoc>("allJavadoc") {
    group = "documentation"

    dependsOn(":std:build")


    source = files(
        *(projectNames.flatMap { project(it).java.sourceSets["main"].allJava }.toTypedArray()),
        fileTree("std/build/generated/sources/annotationProcessor/java")
    ).asFileTree

    classpath = files(*(projectNames.flatMap { project(it).java.sourceSets["main"].compileClasspath }
        .toTypedArray())).asFileTree
    setDestinationDir(file("docs/public/javadoc"))
}