plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {

    // Burp Montoya API (provided by Burp at runtime)
    compileOnly("net.portswigger.burp.extensions:montoya-api:2025.12")

    // Jackson Core (streaming parser)
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")

    // Jackson Databind (ObjectMapper, JsonNode)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")

    // Explicit annotations dependency (safer for fat-jar embedding)
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.named<Jar>("jar") {

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Embed runtime dependencies into extension jar
    from({
        configurations.runtimeClasspath.get()
            .filter { it.isDirectory }
    })

    from({
        configurations.runtimeClasspath.get()
            .filterNot { it.isDirectory }
            .map { zipTree(it) }
    })

    manifest {
        attributes(
            "Implementation-Title" to "LogicHunter",
            "Implementation-Version" to "1.0"
        )
    }
}