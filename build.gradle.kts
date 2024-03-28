plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.3.5"
}

group = "ru.swiftail"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    compileOnly("org.projectlombok:lombok")
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("ch.qos.logback:logback-classic")
    implementation("org.yaml:snakeyaml")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
}

application {
    mainClass.set("ru.swiftail.KnopaApplication")
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

micronaut {
    version("4.3.5")
    processing {
        incremental(true)
        annotations("ru.swiftail.*")
    }
}
