pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
rootProject.name = "HMCCosmetics"
include(
    "common",
    "v1_19_R1",
    "v1_19_R2"
)
