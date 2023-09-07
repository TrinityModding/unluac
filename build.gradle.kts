plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.hydos.unluac"
version = "0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("it.unimi.dsi:fastutil:8.5.12")
    shadow("it.unimi.dsi:fastutil:8.5.12")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "me.hydos.unluac.Main"
    }
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "2G"
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}