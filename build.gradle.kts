import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "me.pliexe.discordeconomybridge"
version = "1.4"

repositories {
    mavenCentral()
    maven(url = "https://repo.codemc.org/repository/nms/")
//    maven(url = "https://jitpack.io")
    jcenter()
}

dependencies {
    testImplementation(kotlin("test-junit"))
    compileOnly("org.spigotmc:spigot:1.8-R0.1-SNAPSHOT")
    compileOnly(fileTree("libs"))
    api("ch.qos.logback:logback-classic:1.2.10")
//    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    api ("net.dv8tion:JDA:4.2.0_168") {
        exclude("opus-java")
        exclude("commons")
    }
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}