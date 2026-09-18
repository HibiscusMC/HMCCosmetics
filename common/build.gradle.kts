plugins {
    `java-library`
    `maven-publish`
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnly(fileTree("${rootDir}/lib") { include("*.jar") })
    compileOnly(libs.paper.api)
    compileOnly(libs.annotations)
    compileOnly(libs.commons.io) // Shipped by the server, not by us
    compileOnly(libs.hibiscuscommons)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.modelengine)
    compileOnly(libs.nexo)
    compileOnly(libs.bundles.betterhud)
    compileOnly(libs.bettercommand) // BetterHud's api signatures reference it
    compileOnly(libs.worldguard) {
        exclude(group = "org.bukkit")
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
        exclude(group = "it.unimi.dsi")
        exclude(group = "com.sk89q.jnbt")
        exclude(group = "org.enginehub.lin-bus.format")
    }

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    implementation(libs.triumph.gui) { exclude("net.kyori") }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${rootProject.group}"
            artifactId = rootProject.name
            version = "${rootProject.version}"

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "HibiscusMCRepository"
            url = uri(hibiscusRepository())

            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
}

/** Target repository picked from the branch being built, defaulting to releases for local publishes */
fun hibiscusRepository(): String {
    val branch = System.getenv("GITHUB_REF")?.removePrefix("refs/heads/") ?: "local"
    return when {
        branch == "master" || branch == "local" -> "https://repo.hibiscusmc.com/releases/"
        branch.startsWith("dev") -> "https://repo.hibiscusmc.com/development/"
        else -> "https://repo.hibiscusmc.com/snapshots/"
    }
}
