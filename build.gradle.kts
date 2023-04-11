import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "me.pliexe.discordeconomybridge"
version = "4.6"

repositories {
    mavenCentral()
    jcenter()

//    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
//    maven(url = "https://jitpack.io")
    maven(url = "https://repo.mrivanplays.com/repository/other-developers")
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
    compileOnly("com.discordsrv:discordsrv:1.26.0")

    compileOnly(fileTree("libs"))

    api("ch.qos.logback:logback-classic:1.2.10")
    api("com.github.simplix-softworks:simplixstorage:3.2.4")

    implementation ("com.google.code.gson:gson:2.10.1")

    implementation("net.dv8tion:JDA:4.4.0_350") {
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