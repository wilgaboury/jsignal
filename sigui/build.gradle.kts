plugins {
    id("java-library")
}

val skijaVersion = "0.116.1";

val lwjglVersion = "3.3.3"

val lwjglNatives = Pair(
        System.getProperty("os.name")!!,
        System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "FreeBSD", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else
                "natives-linux"
        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) }                ->
            "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"
        arrayOf("Windows").any { name.startsWith(it) }                           ->
            if (arch.contains("64"))
                "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
            else
                "natives-windows-x86"
        else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://releases.usethesource.io/maven")
    }
}

dependencies {
    api(project(":jsignal"))

    api(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    api("org.lwjgl", "lwjgl")
    api("org.lwjgl", "lwjgl-yoga")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-yoga", classifier = lwjglNatives)

    api("io.github.humbleui:skija-windows-x64:${skijaVersion}")
    api("io.github.humbleui:skija-linux-x64:${skijaVersion}")
    api("io.github.humbleui:skija-macos-x64:${skijaVersion}")
    api("io.github.humbleui:jwm:0.4.15")

    // 2024-03-26
    compileOnly(files("./hotswap-agent-1.4.2-SNAPSHOT.jar"))

    implementation("org.jetbrains:annotations:24.0.0")

    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("com.google.guava:guava:32.1.3-jre")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}