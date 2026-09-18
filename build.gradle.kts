import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder

plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.plugin.yml)
}

group = "com.hibiscusmc"
version = "2.9.2"

val javaVersion = JavaLanguageVersion.of(libs.versions.java.get())

allprojects {
    apply(plugin = "java-library")

    java {
        toolchain.languageVersion = javaVersion
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
    }

    tasks.withType<ProcessResources>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }

    tasks.withType<Javadoc>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        with(options as StandardJavadocDocletOptions) {
            tags("apiNote:a:API:", "implSpec:a:Implementation Requirements", "implNote:a:Implementation Note:")
            // The codebase isn't fully javadoc'd, so silence doclint's "no comment" noise
            addStringOption("Xdoclint:none", "-quiet")
        }
    }
}

dependencies {
    implementation(project(":common"))
}

// Own task rather than a shadowJar doLast, which a build cache hit would skip while leaving run/plugins stale
val copyToTestServer = tasks.register<Copy>("copyToTestServer") {
    from(tasks.shadowJar)
    into(layout.projectDirectory.dir("run/plugins"))
    rename { "HMCCosmeticsRemapped.jar" }

    doLast {
        println("If you use the plugin, consider buying it for: ")
        println("The custom resource pack, Oraxen + ItemAdder configurations, and Discord support!")
        println("Polymart: https://polymart.org/resource/1879")
        println("Spigot: https://www.spigotmc.org/resources/100107/")
    }
}

tasks {
    runServer {
        minecraftVersion("1.21.11")

        downloadPlugins {
            hangar("PlaceholderAPI", "2.12.2")
            hangar("Multiverse-Core", "5.3.4")
            url("https://download.luckperms.net/1624/bukkit/loader/LuckPerms-Bukkit-5.5.36.jar")
            github("Test-Account666", "PlugManX", "2.4.1", "PlugManX-2.4.1.jar")
            github("gecolay", "GSit", "3.2.1", "GSit-3.2.1.jar")
        }
    }

    shadowJar {
        mergeServiceFiles()
        relocate("dev.triumphteam.gui", "com.hibiscusmc.hmccosmetics.shaded.gui")
        archiveFileName = "HMCCosmeticsRemapped-${project.version}.jar"
    }

    build {
        dependsOn(copyToTestServer)
    }
}

bukkit {
    main = "com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin"
    version = "${project.version}"
    apiVersion = "1.20"
    load = PluginLoadOrder.POSTWORLD
    authors = listOf("LoJoSho", "boy0000")
    depend = listOf("HibiscusCommons")
    softDepend = listOf(
        "Nexo", "BetterHud", "ModelEngine", "Oraxen", "ItemsAdder", "Geary", "HMCColor", "WorldGuard",
        "MythicMobs", "PlaceholderAPI", "SuperVanish", "PremiumVanish", "LibsDisguises", "Denizen", "MMOItems", "Eco"
    )

    commands {
        register("hmccosmetics") {
            description = "Base Cosmetic Command"
            aliases = listOf("cosmetic", "cosmetics")
        }
    }

    permissions {
        val everyone = listOf(
            "cmd.default", "cmd.apply", "cmd.unapply", "cmd.dye", "cmd.wardrobe", "cmd.menu",
            "emote.shiftrun", "cmd.emote"
        )
        val operators = listOf(
            "cmd.playemote", "cmd.playemote.other", "cmd.emote.other", "cmd.setwardrobesetting", "cmd.dataclear",
            "cmd.reload", "cmd.apply.other", "cmd.unapply.other", "cmd.hide", "cmd.show", "cmd.toggle",
            "cmd.hide.other", "cmd.show.other", "cmd.toggle.other", "cmd.wardrobe.other", "cmd.menu.other",
            "cmd.debug", "unapplydeath.bypass", "cmd.disableall", "cmd.hiddenreasons", "cmd.clearhiddenreasons",
            "cmd.hiddenreasons.other", "cmd.clearhiddenreasons.other"
        )

        for (node in everyone) register("hmccosmetics.$node") { default = Default.TRUE }
        for (node in operators) register("hmccosmetics.$node") { default = Default.OP }
    }
}
