plugins {
    id("java")
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
    maxHeapSize = "4G"
}