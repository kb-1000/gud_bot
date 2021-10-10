import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
}

repositories {
    mavenCentral()
    maven(url = "https://maven.kotlindiscord.com/repository/maven-public/") {
        name = "Kotlin Discord"
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(platform(kotlin("bom")))
    implementation(platform("org.jetbrains:annotations:22.0.0"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.0"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.5.2"))

    implementation("com.charleskorn.kaml:kaml:0.36.0")

    implementation("org.codehaus.groovy:groovy:3.0.9")
    implementation("ch.qos.logback:logback-classic:1.2.6")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")

    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.ktorm:ktorm-support-postgresql:3.4.1")
    implementation("org.postgresql:postgresql:42.2.24")

    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.1-SNAPSHOT")
    implementation("com.kotlindiscord.kord.extensions:unsafe:1.5.1-SNAPSHOT")
}

application {
    mainClass.set("de.kb1000.gudbot.Main")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"

    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
