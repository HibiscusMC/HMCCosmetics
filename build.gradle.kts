import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

group = "io.github.fisher2911"
version = "1.6.2"
description = "The ultimate cosmetic plugin for your server."

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.mattstudios.me/artifactory/public/")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://mvnrepository.com/artifact/com.zaxxer/HikariCP")
    maven("https://repo.jeff-media.de/maven2/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:22.0.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("com.github.oraxen:oraxen:-SNAPSHOT")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:2.5.4")
    implementation("net.kyori:adventure-api:4.9.3")
    implementation("net.kyori:adventure-text-minimessage:4.10.0-SNAPSHOT")
    implementation("net.kyori:adventure-platform-bukkit:4.0.1")
    implementation("dev.triumphteam:triumph-gui:3.0.3")
    implementation("me.mattstudios.utils:matt-framework:1.4.6")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.bstats:bstats-bukkit:2.2.1")
    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")
    implementation("com.j256.ormlite:ormlite-core:6.1")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(16)
    }

    shadowJar {
        relocate("dev.triumphteam.gui", "io.github.fisher2911.hmccosmetics.gui")
        relocate("me.mattstudios.mf", "io.github.fisher2911.hmccosmetics.mf")
        relocate("net.kyori.adventure.text.minimessage", "io.github.fisher2911.hmccosmetics.adventure.minimessage")
        relocate("net.kyori.adventure.platform", "io.github.fisher2911.hmccosmetics.adventure.platform")
        relocate("org.spongepowered.configurate", "io.github.fisher2911.hmccosmetics.configurate")
        relocate("org.bstats", "io.github.fisher2911.hmccosmetics.bstats")
        relocate("com.zaxxer.hikaricp", "io.github.fisher2911.hmccosmetics.hikaricp")
        relocate("com.j256.ormlite", "io.github.fisher2911.hmccosmetics.ormlite")
        archiveFileName.set("HMCCosmetics.jar")
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "io.github.fisher2911.hmccosmetics.HMCCosmetics"
    apiVersion = "1.17"
    name = "HMCCosmetics"
    authors = listOf("MasterOfTheFish")
    softDepend = listOf("Multiverse", "PlaceholderAPI", "Oraxen", "ItemsAdder")
    depend = listOf("ProtocolLib")
    permissions {
        register("hmccosmetics.cmd.default") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Permission to execute the default command."
        }
        register("hmccosmetics.cmd.dye") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Permission to dye armor."
        }
        register("hmccosmetics.cmd.reload") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Permission to use the reload command."
        }
        register("hmccosmetics.cmd.set") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Permission to set other users' cosmetics."
        }
    }
}