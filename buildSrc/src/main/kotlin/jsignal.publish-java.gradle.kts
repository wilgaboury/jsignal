import gradle.kotlin.dsl.accessors._273c41c017a30de2a6ca8df633927332.publishing
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

plugins {
    id("jsignal.publish")
}

publishing {
    publications {
        named<MavenPublication>("mavenCentral") {
            from(components["java"])
        }
    }
}