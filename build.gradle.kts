import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
}

repositories {
    mavenCentral()
    maven(url = "https://maven.kotlindiscord.com/repository/maven-public/") {
        name = "Kotlin Discord"
    }
}

dependencies {
    implementation(enforcedPlatform(kotlin("bom")))
    implementation(enforcedPlatform("org.jetbrains:annotations:23.0.0"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.2"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.1"))

    implementation(kotlin("stdlib-jdk8"))

    implementation("com.charleskorn.kaml:kaml:0.43.0")

    implementation("org.codehaus.groovy:groovy:3.0.10")
    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.ktorm:ktorm-support-postgresql:3.4.1")
    implementation("org.postgresql:postgresql:42.3.4")

    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.2-RC1")
    implementation("com.kotlindiscord.kord.extensions:unsafe:1.5.2-RC1")
}

application {
    mainClass.set("de.kb1000.gudbot.Main")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"

    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
