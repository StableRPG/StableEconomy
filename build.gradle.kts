import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml.Load

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.2.1"
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

    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:10.0.0")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    compileOnly("com.zaxxer:HikariCP:6.3.0")
    compileOnly("org.xerial:sqlite-jdbc:3.49.1.0")
    compileOnly("com.h2database:h2:2.3.232")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.3")
    compileOnly("org.postgresql:postgresql:42.7.5")
    compileOnly("org.mongodb:mongodb-driver-sync:5.4.0")
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

        relocate("dev.jorel.commandapi", "${project.group}.libs.commandapi")

        minimize()
    }
    assemble {
        dependsOn(shadowJar)
    }
    paperweight {
        reobfArtifactConfiguration.set(ReobfArtifactConfiguration.MOJANG_PRODUCTION)
    }
    paperPluginYaml {
        apiVersion.set("1.21")
        name.set(rootProject.name)
        version.set(project.version.toString())
        main.set("${project.group}.${rootProject.name}")
        loader.set("${project.group}.${rootProject.name}Loader")
        author.set("StableRPG")
        website.set("https://github.com/StableRPG/StableEconomy")
        dependencies {
            server("Vault", Load.BEFORE, required = false, joinClasspath = true)
            server("PlaceholderAPI", Load.BEFORE, required = false, joinClasspath = true)
        }
    }
}
