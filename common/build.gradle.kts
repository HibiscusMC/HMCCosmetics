plugins {
    id("java")
    id("maven-publish")
}

dependencies {
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.3")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.2")
    compileOnly("com.github.oraxen:oraxen:1.160.0")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.2.5")
    compileOnly("com.mineinabyss:geary-papermc:0.24-SNAPSHOT")
    //compileOnly("com.hibiscus:hmccolor:0.3-SNAPSHOT") USE LIB PROVIDED FILE
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
    compileOnly("it.unimi.dsi:fastutil:8.5.11")
    compileOnly("io.lumine:Mythic-Dist:5.2.1")
    compileOnly("com.denizenscript:denizen:1.2.7-SNAPSHOT")
    compileOnly("com.github.LeonMangler:SuperVanish:6.2.17")
    compileOnly("net.Indyuce:MMOItems-API:6.9.4-SNAPSHOT")
    compileOnlyApi("LibsDisguises:LibsDisguises:10.0.21") {
        exclude("org.spigotmc", "spigot")
    }

    //compileOnly("com.github.Fisher2911:FisherLib:master-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.12.0")
    implementation("net.kyori:adventure-text-minimessage:4.12.0")
    implementation("net.kyori:adventure-platform-bukkit:4.2.0")
    implementation("dev.triumphteam:triumph-gui:3.1.3")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.bstats:bstats-bukkit:3.0.0")
    implementation("com.jeff_media:SpigotUpdateChecker:3.0.0")
    implementation("com.owen1212055:particlehelper:1.0.0-SNAPSHOT")
    implementation("com.ticxo.playeranimator:PlayerAnimator:R1.2.7")
    implementation("com.github.BG-Software-LLC:CommentedConfiguration:bed3c46369")
    //implementation("com.ticxo.playeranimator:PlayerAnimator:R1.2.5")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17
    ))
}

publishing {
    val publishData = PublishData(project)
    publications {
        create<MavenPublication>("maven") {
            groupId = "${rootProject.group}"
            artifactId = "${rootProject.name}"
            version = "${rootProject.version}"

            from(components["java"])
        }
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("REPO_USERNAME")
                    password = System.getenv("REPO_PASSWORD")
                }
            }

            name = "HibiscusMCRepository"
            url = uri(publishData.getRepository())
        }
    }
}

class PublishData(private val project: Project) {
    var type: Type = getReleaseType()
    var hashLength: Int = 7

    private fun getReleaseType(): Type {
        val branch = getCheckedOutBranch()
        return when {
            branch.contentEquals("master") || branch.contentEquals("local") -> Type.RELEASE
            branch.startsWith("dev") -> Type.DEV
            else -> Type.SNAPSHOT
        }
    }

    private fun getCheckedOutGitCommitHash(): String =
        System.getenv("GITHUB_SHA")?.substring(0, hashLength) ?: "local"

    private fun getCheckedOutBranch(): String =
        System.getenv("GITHUB_REF")?.replace("refs/heads/", "") ?: "local"

    fun getVersion(): String = getVersion(false)

    fun getVersion(appendCommit: Boolean): String =
        type.append(getVersionString(), appendCommit, getCheckedOutGitCommitHash())

    private fun getVersionString(): String =
        (rootProject.version as String).replace("-SNAPSHOT", "").replace("-DEV", "")

    fun getRepository(): String = type.repo

    enum class Type(private val append: String, val repo: String, private val addCommit: Boolean) {
        RELEASE("", "https://repo.hibiscusmc.com/releases/", false),
        DEV("-DEV", "https://repo.hibiscusmc.com/development/", true),
        SNAPSHOT("-SNAPSHOT", "https://repo.hibiscusmc.com/snapshots/", true);

        fun append(name: String, appendCommit: Boolean, commitHash: String): String =
            name.plus(append).plus(if (appendCommit && addCommit) "-".plus(commitHash) else "")
    }
}
