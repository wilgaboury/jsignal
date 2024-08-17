plugins {
    id("java-library")
}

val skijaVersion = "0.116.1"

val lwjglVersion = "3.3.3"
val lwjglNatives = listOf(
    "natives-windows-x86",
    "natives-linux-x86",
    "natives-linux-arm64",
    "natives-macos-x86",
    "natives-macos-arm64"
)

repositories {
    mavenCentral()
    maven {
        url = uri("https://releases.usethesource.io/maven")
    }
}

dependencies {
    api(project(":jsignal"))

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-yoga")
    implementation("org.lwjgl", "lwjgl-nfd")

    for (classifier in lwjglNatives) {
        runtimeOnly("org.lwjgl", "lwjgl", classifier = classifier)
        runtimeOnly("org.lwjgl", "lwjgl-yoga", classifier = classifier)
        runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = classifier)
    }

    api("io.github.humbleui:skija-windows-x64:${skijaVersion}")
    api("io.github.humbleui:skija-linux-x64:${skijaVersion}")
    api("io.github.humbleui:skija-linux-arm64:${skijaVersion}")
    api("io.github.humbleui:skija-macos-x64:${skijaVersion}")
    api("io.github.humbleui:skija-macos-arm64:${skijaVersion}")

    api("io.github.humbleui:jwm:0.4.15")

    // 2024-03-26
    compileOnly(files("./hotswap-agent-1.4.2-SNAPSHOT.jar"))

    implementation("org.graalvm.espresso:hotswap:24.0.1")

    implementation("org.jetbrains:annotations:24.0.0")

    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("com.google.guava:guava:32.1.3-jre")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}