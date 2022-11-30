plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.3.8"
}

dependencies {
    paperDevBundle("1.19.2-R0.1-SNAPSHOT")
    implementation(project(":common"))
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
        filteringCharset = Charsets.UTF_8.name()
    }
}