plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    application
}

group = "live.shirabox"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Ktor dependencies
    val ktorVersion = "2.3.5"

    implementation ("io.ktor:ktor-client-core:$ktorVersion")
    implementation ("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation ("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation ("io.ktor:ktor-client-cio:$ktorVersion")
    implementation ("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Logging
    implementation ("org.slf4j:slf4j-api:2.0.9")
    implementation ("org.slf4j:slf4j-simple:2.0.9")

    // Network
    implementation ("com.google.firebase:firebase-admin:9.2.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation ("com.github.kittinunf.fuel:fuel:3.0.0-alpha1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "${project.group}.notificationservice.MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

application {
    mainClass.set("MainKt")
}