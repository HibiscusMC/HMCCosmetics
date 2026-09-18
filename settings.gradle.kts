pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://repo.papermc.io/repository/maven-public/") // Paper
        maven("https://repo.hibiscusmc.com/releases") // HibiscusCommons
        maven("https://repo.triumphteam.dev/snapshots") // Triumph GUI
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://maven.enginehub.org/repo/") // WorldGuard
        maven("https://repo.nexomc.com/releases") // Nexo
        maven("https://jitpack.io") // BetterHud

        // ModelEngine publishes no metadata, only the artifact itself
        maven("https://mvn.lumine.io/repository/maven-public") {
            metadataSources { artifact() }
        }
    }
}

rootProject.name = "HMCCosmetics"

include("common")
