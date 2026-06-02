plugins {
    id("jsignal.publish-java")
    id("jsignal.deps")
}

val skijaVersion = "0.116.2"

val lwjglVersion = "3.3.3"
val lwjglNatives = listOf(
    "natives-windows",
    "natives-linux",
    "natives-macos",
    "natives-macos-arm64"
)

dependencies {
    annotationProcessor(project(":prop"))
    compileOnly(project(":prop"))

    api(project(":rx"))

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-yoga")
    implementation("org.lwjgl", "lwjgl-nfd")

    for (classifier in lwjglNatives) {
        runtimeOnly("org.lwjgl", "lwjgl", classifier = classifier)
        runtimeOnly("org.lwjgl", "lwjgl-yoga", classifier = classifier)
        runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = classifier)
    }

    api("org.joml:joml:1.10.8")

    // 2024-03-26
    compileOnly(files("./hotswap-agent-1.4.2-SNAPSHOT.jar"))

    implementation("org.graalvm.espresso:hotswap:24.0.1")
}