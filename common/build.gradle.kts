import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

group = "io.github.fisher2911"
version = "1.12.0-BETA-4"
description = "Intuitive, easy-to-use cosmetics plugin, designed for servers using resource packs.\n"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.mattstudios.me/artifactory/public/")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://mvnrepository.com/artifact/com.zaxxer/HikariCP")
    maven("https://repo.jeff-media.de/maven2/")
    maven("https://repo.citizensnpcs.co")
    //maven("https://mvn.lumine.io/repository/maven-public")
    maven {
        url = uri("https://mvn.lumine.io/repository/maven-public")
        metadataSources {
            artifact()
        }
    }
    maven("https://jitpack.io/")
}

dependencies {
//    implementation(project(":1.16"))
//    implementation(project(":1.17"))
//    implementation(project(":1.18"))
//    implementation(project(":nms"))
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:22.0.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("com.github.oraxen:oraxen:-SNAPSHOT")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:2.5.4")
    compileOnly("net.citizensnpcs:citizens-main:2.0.30-SNAPSHOT")
    compileOnly("com.ticxo.modelengine:api:R2.5.0")
    //compileOnly("com.github.retrooper.packetevents:spigot:2.0-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.10.0")
    implementation ("net.kyori:adventure-text-minimessage:4.10.0-SNAPSHOT")
    implementation("net.kyori:adventure-platform-bukkit:4.0.1")
    implementation("dev.triumphteam:triumph-gui:3.1.2")
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
        relocate("net.kyori.adventure", "io.github.fisher2911.hmccosmetics.adventure")
        relocate("org.spongepowered.configurate", "io.github.fisher2911.hmccosmetics.configurate")
        relocate("org.bstats", "io.github.fisher2911.hmccosmetics.bstats")
        relocate("com.zaxxer.hikaricp", "io.github.fisher2911.hmccosmetics.hikaricp")
        relocate("com.j256.ormlite", "io.github.fisher2911.hmccosmetics.ormlite")
        //relocate("com.github.retrooper.packetevents", "io.github.fisher2911.hmccosmetics.packetevents")
        //relocate("io.github.retrooper.packetevents", "io.github.fisher2911.hmccosmetics.packetevents")
        archiveFileName.set("HMCCosmetics.jar")

        dependencies {
            exclude(dependency("org.yaml:snakeyaml"))
        }

        // todo - remove (Testing only)
        //destinationDirectory.set(file("D:\\paper-1.18.1\\plugins"))
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16
    ))
}

bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "io.github.fisher2911.hmccosmetics.HMCCosmetics"
    apiVersion = "1.16"
    name = "HMCCosmetics"
    authors = listOf("MasterOfTheFish")
    softDepend = listOf("Multiverse", "PlaceholderAPI", "Oraxen", "ItemsAdder", "Citizens", "ModelEngine")
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
        register("hmccosmetics.cmd.wardrobe.portable") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Permission to use a portable wardrobe"
        }
        register("hmccosmetics.cmd.wardrobe") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Permission to view the wardrobe"
        }
        register("hmccosmetics.cmd.wardrobe.other") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Permission to open another player's wardrobe"
        }
        register("hmccosmetics.cmd.token.give") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Permission to give other players tokens"
        }
    }
}

val copyJar: String? by project
val pluginPath = project.findProperty("hibiscusmc_plugin_path")

if(copyJar != "false" && pluginPath != null) {
    tasks {
        register<Copy>("copyJar") {
            from(findByName("reobfJar") ?: findByName("shadowJar") ?: findByName("jar"))
            into(pluginPath)
            doLast {
                println("Copied to plugin directory $pluginPath")
            }
        }

        named<DefaultTask>("build") {
            dependsOn("copyJar")
        }
    }
}
