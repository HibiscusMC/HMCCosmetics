plugins {
    id("java")
}

//group = "io.github.fisher2911"
//version = "1.7.1"
//description = "Intuitive, easy-to-use cosmetics plugin, designed for servers using resource packs.\n"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    implementation(project(":nms"))
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.spigotmc:spigot:1.18-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:22.0.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
}
