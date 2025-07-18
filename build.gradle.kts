plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    `maven-publish`
    signing
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "com.closedbrain"
version = "1.0.0"
description = "A slightly modified version of the awesome kotlin wrapper made by JackBeBack."

repositories {
    mavenCentral()
}

val ktorVersion: String = "3.2.2"

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("ch.qos.logback:logback-classic:1.5.18")
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()
}

mavenPublishing {
    coordinates(group.toString(), "kollama", version.toString())

    pom {
        name.set("KOllama")
        description.set(project.description)
        url.set("https://github.com/JackBeBack/Ollama-KotlinWrapper/")

        licenses {
            license {
                name="MIT"
                url.set("https://www.opensource.org/licenses/mit-license.php")
            }
        }

        developers {
            developer {
                name.set("Jack BeBack")
            }
        }

        scm {
            url.set("https://github.com/ClosedBrain/KOllama")
        }
    }
}
