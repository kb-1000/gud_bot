import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
}

repositories {
    mavenCentral()
    maven(url = "https://maven.kotlindiscord.com/repository/maven-public/") {
        name = "Kotlin Discord"
    }
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(platform("org.jetbrains:annotations:24.1.0"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.6.3"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.8.0"))

    implementation(kotlin("stdlib"))

    implementation("com.charleskorn.kaml:kaml:0.58.0")

    implementation("org.codehaus.groovy:groovy:3.0.11")
    implementation("ch.qos.logback:logback-classic:1.5.4")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation("org.ktorm:ktorm-support-postgresql:3.6.0")
    implementation("org.postgresql:postgresql:42.7.3")

    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.6.0")
    implementation("com.kotlindiscord.kord.extensions:unsafe:1.6.0")
}

application {
    mainClass.set("de.kb1000.gudbot.Main")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
