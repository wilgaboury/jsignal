plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

val skijaArtifact = "skija-linux-x64";
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
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-yoga")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)

    implementation("io.github.humbleui:${skijaArtifact}:${skijaVersion}")
    implementation("io.github.humbleui:jwm:0.4.15")

    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.functionaljava:functionaljava:5.0")
    implementation("com.github.davidmoten:rtree:0.11")

    implementation("org.hotswapagent:hotswap-agent-core:1.4.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}