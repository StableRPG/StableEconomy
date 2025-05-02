import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.2.0"
}

setGroup("org.stablerpg.stableeconomy")
setVersion("1.0.0")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")

    compileOnly("com.zaxxer:HikariCP:6.2.1")
    compileOnly("org.xerial:sqlite-jdbc:3.49.1.0")
    compileOnly("com.h2database:h2:2.3.232")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.1")
    compileOnly("org.postgresql:postgresql:42.7.4")
    compileOnly("org.mongodb:mongodb-driver-sync:5.3.0-beta0")

    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:10.0.0")
}

tasks {
    val javaVersion = JavaLanguageVersion.of(21)
    java {
        toolchain.languageVersion.set(javaVersion)
    }
    compileJava {
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.asInt())
    }
    jar {
        enabled = false
    }
    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
        minimize()
    }
    assemble {
        dependsOn(shadowJar)
    }
    paperweight {
        reobfArtifactConfiguration.set(ReobfArtifactConfiguration.MOJANG_PRODUCTION)
    }
    paperPluginYaml {
        name.set(rootProject.name)
        main.set("${project.group}.${rootProject.name}")
        apiVersion.set("1.21")
        author.set("ImNotStable")
    }
}
