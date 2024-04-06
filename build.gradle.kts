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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Logging
    implementation ("org.slf4j:slf4j-api:2.0.9")
    implementation ("org.slf4j:slf4j-simple:2.0.9")

    // Network
    implementation ("org.java-websocket:Java-WebSocket:1.5.4")
    implementation ("com.google.firebase:firebase-admin:9.2.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation ("com.github.kittinunf.fuel:fuel:3.0.0-alpha1")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
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