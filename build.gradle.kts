import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "com.hibiscusmc"
version = "2.7.5${getGitCommitHash()}"

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

        // ParticleHelper
        maven("https://repo.bytecode.space/repository/maven-public/")

        // PlayerAnimator
        maven("https://mvn.lumine.io/repository/maven/")

        // md-5 Repo
        maven("https://repo.md-5.net/content/groups/public/")

        // MMOItems
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")

        // Eco-Suite/Auxilor Repo
        maven("https://repo.auxilor.io/repository/maven-public/")

        // Triumph GUI
        maven("https://repo.triumphteam.dev/snapshots")

        // Hibiscus Commons
        maven("https://repo.hibiscusmc.com/releases")
    }

    dependencies {
        compileOnly(fileTree("${project.rootDir}/lib") { include("*.jar") })
        compileOnly("com.mojang:authlib:1.5.25")
        //compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
        compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
        compileOnly("org.jetbrains:annotations:24.1.0")
        compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.6")
        compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.12")
        //compileOnly("it.unimi.dsi:fastutil:8.5.14")
        compileOnly("org.projectlombok:lombok:1.18.34")
        compileOnly("me.lojosho:HibiscusCommons:0.6.0-85d65299")

        // Handled by Spigot Library Loader
        compileOnly("net.kyori:adventure-api:4.18.0")
        compileOnly("net.kyori:adventure-text-minimessage:4.18.0")
        compileOnly("net.kyori:adventure-platform-bukkit:4.3.4")

        annotationProcessor("org.projectlombok:lombok:1.18.36")
        testCompileOnly("org.projectlombok:lombok:1.18.36")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.36")

        implementation("dev.triumphteam:triumph-gui:3.1.12-SNAPSHOT") {
            exclude("net.kyori") // Already have adventure API
        }
        implementation("com.owen1212055:particlehelper:1.0.0-SNAPSHOT")
        implementation("com.ticxo.playeranimator:PlayerAnimator:R1.2.7")
    }
}

dependencies {
    implementation(project(path = ":common"))
}

tasks {

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.21.4")

        downloadPlugins {
            hangar("PlaceholderAPI", "2.11.6")
            url("https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/build/libs/ProtocolLib.jar")
            url("https://download.luckperms.net/1567/bukkit/loader/LuckPerms-Bukkit-5.4.150.jar")
        }
    }

    shadowJar {
        mergeServiceFiles()

        relocate("dev.triumphteam.gui", "com.hibiscusmc.hmccosmetics.shaded.gui")
        relocate("com.owen1212055.particlehelper", "com.hibiscusmc.hmccosmetics.shaded.particlehelper")
        relocate("com.ticxo.playeranimator", "com.hibiscusmc.hmccosmetics.shaded.playeranimator")
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
    apiVersion = "1.20"
    authors = listOf("LoJoSho")
    depend = listOf("HibiscusCommons", "ProtocolLib")
    softDepend = listOf("ModelEngine", "Oraxen", "ItemsAdder", "Geary", "HMCColor", "WorldGuard", "MythicMobs", "PlaceholderAPI", "SuperVanish", "PremiumVanish", "LibsDisguises", "Denizen", "MMOItems", "Eco")
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
        register("hmccosmetics.cmd.disableall") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.hiddenreasons") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("hmccosmetics.cmd.clearhiddenreasons") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

fun getGitCommitHash(): String {
    var includeHash = true
    val includeHashVariable = System.getenv("HMCC_INCLUDE_HASH")

    if (!includeHashVariable.isNullOrEmpty()) includeHash = includeHashVariable.toBoolean()

    if (includeHash) {
        return try {
            val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
                .redirectErrorStream(true)
                .start()

            process.inputStream.bufferedReader().use { "-" + it.readLine().trim() }
        } catch (e: Exception) {
            "-unknown" // Fallback if Git is not available or an error occurs
        }
    }
    return ""
}