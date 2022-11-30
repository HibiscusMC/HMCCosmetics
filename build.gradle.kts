import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "2.0.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "com.hibiscusmc"
version = "Infdev"

allprojects {
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
    }
}

dependencies {
    implementation(project(":common"))
    //implementation(project(":v1_19_R1"))
    implementation(files("v1_19_R1/build/libs/1_19_R1-unspecified.jar"))
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
        minecraftVersion("1.19.2")
    }

    assemble {
        dependsOn(shadowJar)
    }

    shadowJar {
        dependsOn(":common:reobfJar")
        dependsOn(":v1_19_R1:reobfJar")
        mergeServiceFiles()

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

        archiveFile.get().asFile.copyTo(layout.projectDirectory.file("run/plugins/HMCCosmeticsRemapped.jar").asFile, true)
    }
}


bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin"
    apiVersion = "1.19"
    authors = listOf("LoJoSho")
    depend = listOf("ProtocolLib")
    softDepend = listOf("ModelEngine", "Oraxen")
    version = "${project.version}"

    commands {
        register("cosmetic") {
            description = "Base command"
        }
    }
}