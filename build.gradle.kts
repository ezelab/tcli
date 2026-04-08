plugins {
    java
    application
    id("org.graalvm.buildtools.native") version "1.0.0"
}

group = "com.tcli"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // CLI framework
    implementation("info.picocli:picocli:4.7.6")
    annotationProcessor("info.picocli:picocli-codegen:4.7.6")

    // Apache Tika
    implementation("org.apache.tika:tika-core:3.3.0")
    implementation("org.apache.tika:tika-parsers-standard-package:3.3.0")

    // OCR via Tess4J
    implementation("net.sourceforge.tess4j:tess4j:5.13.0")

    // JSON output
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    // Logging (suppress Tika warnings)
    implementation("org.slf4j:slf4j-simple:2.0.16")

    // Required for GraalVM native-image with Tika's jakarta activation
    implementation("jakarta.mail:jakarta.mail-api:2.1.3")
    implementation("org.eclipse.angus:angus-mail:2.0.3")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.tcli.App")
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes["Main-Class"] = "com.tcli.App" }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get())
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

graalvmNative {
    binaries {
        named("main") {
            mainClass.set("com.tcli.App")
            imageName.set("tcli")

            val graalLibPath = (System.getenv("JAVA_HOME") ?: "") + "/lib"

            buildArgs.addAll(
                "--no-fallback",
                "--enable-url-protocols=https,http",
                "-H:+ReportExceptionStackTraces",
                "-H:+AddAllCharsets",
                "-Djava.awt.headless=true",
                "-H:CLibraryPath=$graalLibPath",
                "--initialize-at-build-time=java.awt,sun.awt,sun.java2d,sun.font,sun.lwawt,javax.imageio,com.github.jaiimageio,com.sun.imageio,org.apache.pdfbox,org.apache.fontbox,org.apache.commons.logging"
            )
        }
    }
    toolchainDetection.set(false)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Aproject=${project.group}/${project.name}"))
}
