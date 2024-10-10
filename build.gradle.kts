import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "me.pliexe.discordeconomybridge"
version = "5.3"

repositories {
    mavenCentral()
    jcenter()

    maven(url = "https://nexus.scarsz.me/content/groups/public/")
    maven(url = "https://repo.codemc.org/repository/nms/")
    maven(url = "https://m2.dv8tion.net/releases")
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven(url = "https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test-junit"))

    compileOnly("org.spigotmc:spigot:1.8-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("com.discordsrv:discordsrv:1.28.0")

    compileOnly(fileTree("libs"))

    api("ch.qos.logback:logback-classic:1.5.9")
    api("com.github.simplix-softworks:simplixstorage:3.2.7")

    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("org.apache.commons:commons-jexl3:3.3")

    api("net.dv8tion:JDA:4.4.1_DiscordSRV.fix-6") {
        exclude(module = "opus-java")
    }
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}