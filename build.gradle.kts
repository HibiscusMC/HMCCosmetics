import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.0.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "com.hibiscusmc"
version = "2.6.5-DEV"

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    repositories {
        mavenCentral()

        // Paper Repo
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")

        // Jitpack
        maven("https://jitpack.io")

        // ProtocolLib repo
        maven("https://repo.dmulloy2.net/repository/public/") //ProtocolLib Repo, constantly down
        maven("https://repo.mineinabyss.com/releases/")
        maven("https://repo.mineinabyss.com/snapshots/")

        // PlaceholderAPI
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

        // Citizens & Denizen
        maven("https://maven.citizensnpcs.co/repo")

        // Worldguard
        maven("https://maven.enginehub.org/repo/")

        // Backup Oraxen repo
        maven("https://repo.skyslycer.de/")

        // MythicMobs
        maven {
            url = uri("https://mvn.lumine.io/repository/maven-public")
            metadataSources {
                artifact()
            }
        }

        // UpdateChecker
        maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")

        // ParticleHelper
        maven("https://repo.bytecode.space/repository/maven-public/")

        // PlayerAnimator
        maven("https://mvn.lumine.io/repository/maven/")

        // md-5 Repo
        maven("https://repo.md-5.net/content/groups/public/")

        // MMOItems
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    }

    dependencies {
        compileOnly(fileTree("${project.rootDir}/lib") { include("*.jar") })
        compileOnly("com.mojang:authlib:1.5.25")
        compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
        compileOnly("org.jetbrains:annotations:23.0.0")
        compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
        compileOnly("me.clip:placeholderapi:2.11.3")
        compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.2")
        compileOnly("com.github.oraxen:oraxen:1.160.0")
        compileOnly("com.github.LoneDev6:API-ItemsAdder:3.2.5")
        compileOnly("com.mineinabyss:geary-papermc:0.26.2")
        compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
        compileOnly("it.unimi.dsi:fastutil:8.5.11")
        compileOnly("com.github.LeonMangler:SuperVanish:6.2.17")
        compileOnly("org.projectlombok:lombok:1.18.2")

        annotationProcessor("org.projectlombok:lombok:1.18.28")
        testCompileOnly("org.projectlombok:lombok:1.18.28")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.28")
    }
}

dependencies {
    implementation(project(path = ":common"))
    implementation(project(path = ":v1_18_R2", configuration = "reobf"))
    implementation(project(path = ":v1_19_R1", configuration = "reobf"))
    implementation(project(path = ":v1_19_R2", configuration = "reobf"))
    implementation(project(path = ":v1_19_R3", configuration = "reobf"))
    implementation(project(path = ":v1_20_R1", configuration = "reobf"))
    implementation(project(path = ":v1_20_R2", configuration = "reobf"))

    //compileOnly("com.github.Fisher2911:FisherLib:master-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.11.0")
    implementation("net.kyori:adventure-text-minimessage:4.11.0")
    implementation("net.kyori:adventure-platform-bukkit:4.1.2")
    implementation("dev.triumphteam:triumph-gui:3.1.3")
    implementation("org.spongepowered:configurate-yaml:4.2.0-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.0")
    implementation("com.jeff_media:SpigotUpdateChecker:3.0.0")
    implementation("com.owen1212055:particlehelper:1.0.0-SNAPSHOT")
    implementation("com.ticxo.playeranimator:PlayerAnimator:R1.2.7")
    implementation("com.github.BG-Software-LLC:CommentedConfiguration:bed3c46369")
    //implementation("com.ticxo.playeranimator:PlayerAnimator:R1.2.5")
}

tasks {

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.20.2")
    }

    shadowJar {
        dependsOn(":v1_18_R2:reobfJar")
        dependsOn(":v1_19_R1:reobfJar")
        dependsOn(":v1_19_R2:reobfJar")
        dependsOn(":v1_19_R3:reobfJar")
        dependsOn(":v1_20_R1:reobfJar")
        dependsOn(":v1_20_R2:reobfJar")
        mergeServiceFiles()

        relocate("dev.triumphteam.gui", "com.hisbiscusmc.hmccosmetics.gui")
        relocate("me.mattstudios.mf", "com.hisbiscusmc.hmccosmetics.mf")
        relocate("net.kyori.adventure", "com.hisbiscusmc.hmccosmetics.adventure")
        relocate("org.spongepowered.configurate", "com.hisbiscusmc.hmccosmetics.configurate")
        relocate("org.bstats", "com.hisbiscusmc.hmccosmetics.bstats")
        relocate("com.zaxxer.hikaricp", "com.hisbiscusmc.hmccosmetics.hikaricp")
        relocate("com.j256.ormlite", "com.hisbiscusmc.hmccosmetics.ormlite")
        relocate("com.jeff_media.updatechecker", "com.hisbiscusmc.hmccosmetics.updatechecker")
        relocate("com.owen1212055.particlehelper", "com.hisbiscusmc.hmccosmetics.particlehelper")
        relocate("com.ticxo.playeranimator", "com.hisbiscusmc.hmccosmetics.playeranimator")
        relocate("com.bgsoftware", "com.hisbiscusmc.hmccosmetics.configupdater")
        archiveFileName.set("HMCCosmeticsRemapped-${project.version}.jar")

        dependencies {
            exclude(dependency("org.yaml:snakeyaml"))
        }

        doLast {
            archiveFile.get().asFile.copyTo(layout.projectDirectory.file("run/plugins/HMCCosmeticsRemapped.jar").asFile, true)
            println("If you use the plugin, consider buying it for: ")
            println("The custom resource pack, Oraxen + ItemAdder configurations, and Discord support!")
            println("Polymart: https://polymart.org/resource/1879")
            println("Spigot: https://www.spigotmc.org/resources/100107/")
        }
    }

    build {
        dependsOn(shadowJar)
    }
}


bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin"
    apiVersion = "1.18"
    authors = listOf("LoJoSho")
    depend = listOf("ProtocolLib")
    softDepend = listOf("ModelEngine", "Oraxen", "ItemsAdder", "Geary", "HMCColor", "WorldGuard", "MythicMobs", "PlaceholderAPI", "SuperVanish", "PremiumVanish", "LibsDisguises", "Denizen", "MMOItems")
    version = "${project.version}"
    loadBefore = listOf(
        "Cosmin" // Fixes an issue with Cosmin loading before and taking /cosmetic, when messing with what we do.
    )

    commands {
        register("cosmetic") {
            description = "Base Cosmetic Command"
            aliases = listOf("hmccosmetics", "cosmetics")
        }
    }
    permissions {
        register("hmccosmetics.cmd.default") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("hmccosmetics.cmd.apply") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("hmccosmetics.cmd.unapply") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("hmccosmetics.cmd.dye") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("hmccosmetics.cmd.wardrobe") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("hmccosmetics.cmd.menu") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("hmccosmetics.emote.shiftrun") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("hmccosmetics.cmd.emote") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("hmccosmetics.cmd.playemote") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.playemote.other") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.emote.other") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.setwardrobesetting") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.dataclear") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.reload") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.apply.other") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.unapply.other") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.hide") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.show") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.hide.other") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.show.other") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.wardrobe.other") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.menu.other") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.debug") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.unapplydeath.bypass") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17
    ))
}
