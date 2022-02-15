plugins {
    id("java")
}

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
    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:22.0.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
}
