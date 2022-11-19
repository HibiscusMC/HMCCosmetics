import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("io.papermc.paperweight.userdev") version "1.3.8"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "com.hibiscusmc"
version = "Infdev"

bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin"
    apiVersion = "1.19"
    authors = listOf("LoJoSho")
    depend = listOf("ProtocolLib")
    softDepend = listOf("ModelEngine")
    version = "${project.version}"

    commands {
        register("cosmetic") {
            description = "Base command"
        }
    }
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
    //maven("https://repo.dmulloy2.net/repository/public/") ProtocolLib Repo, constantly down
    maven("https://repo.mineinabyss.com/releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://mvnrepository.com/artifact/com.zaxxer/HikariCP")
    maven("https://repo.citizensnpcs.co")
    //maven("https://mvn.lumine.io/repository/maven-public")
    maven {
        url = uri("https://mvn.lumine.io/repository/maven-public")
        metadataSources {
            artifact()
        }
    }
}

dependencies {
    paperDevBundle("1.19.2-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("com.ticxo.modelengine:api:R3.0.1")
    implementation("net.kyori:adventure-api:4.11.0")
    implementation ("net.kyori:adventure-text-minimessage:4.11.0")
    implementation("net.kyori:adventure-platform-bukkit:4.1.2")
    implementation("dev.triumphteam:triumph-gui:3.1.3")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.bstats:bstats-bukkit:3.0.0")
}

tasks {

    build {
        dependsOn(shadowJar)
    }

    assemble {
        dependsOn(reobfJar)
    }

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

    reobfJar {
        outputJar.set(layout.projectDirectory.file("run/plugins/HMCCosmeticsRemapped.jar"))
    }

    shadowJar {
        relocate("dev.triumphteam.gui", "com.hisbiscus.hmccosmetics.gui")
        relocate("me.mattstudios.mf", "com.hisbiscus.hmccosmetics.mf")
        relocate("net.kyori.adventure", "com.hisbiscus.hmccosmetics.adventure")
        relocate("org.spongepowered.configurate", "com.hisbiscus.hmccosmetics.configurate")
        relocate("org.bstats", "com.hisbiscus.hmccosmetics.bstats")
        relocate("com.zaxxer.hikaricp", "com.hisbiscus.hmccosmetics.hikaricp")
        relocate("com.j256.ormlite", "com.hisbiscus.hmccosmetics.ormlite")
        archiveFileName.set("HMCCosmetics.jar")

        dependencies {
            exclude(dependency("org.yaml:snakeyaml"))
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17
    ))
}