plugins {
    id("jsignal.publish-java")
    id("jsignal.deps")
}

dependencies {
    annotationProcessor(project(":prop"))
    compileOnly(project(":prop"))

    api(project(":ui"))
    api(project(":rx"))
    api("com.google.guava:guava:33.3.0-jre") // needed for MediaType
    api("org.apache.xmlgraphics:batik-bridge:1.17")
    api("org.apache.xmlgraphics:batik-gvt:1.17")
    api("org.apache.xmlgraphics:batik-dom:1.17")
    api("org.apache.xmlgraphics:batik-svggen:1.17")
}