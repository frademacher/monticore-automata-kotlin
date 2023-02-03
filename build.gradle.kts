import de.monticore.MCTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id("monticore") version "7.5.0-SNAPSHOT"
}

group = "org.example"
version = "1.0-SNAPSHOT"
val generatedSourcesDir = "$buildDir/generated-sources/monticore/sourcecode"

repositories {
    mavenCentral()
    maven {
        url = uri("https://nexus.se.rwth-aachen.de/repository/public/")
    }
}

sourceSets {
    main {
        java {
            srcDir(generatedSourcesDir)
        }
    }
}

buildscript {
    extra.set("montiCoreVersion", "7.5.0-SNAPSHOT")
}

dependencies {
    val montiCoreVersion: String by rootProject.extra

    implementation("de.monticore:monticore-runtime:$montiCoreVersion")
    implementation("de.se_rwth.commons:se-commons-logging:$montiCoreVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

fileTree("src/main/grammars").matching {
    include("**/*.mc4")
}.visit {
    if (file.isFile)
        tasks.register<MCTask>("Process Grammar ${file.absolutePath.replace(File.separator, "_")}") {
            dependsOn(tasks.clean)
            grammar.set(file)
            outputDir.set(File(generatedSourcesDir))
        }
}

tasks.withType<JavaCompile> {
    options.release.set(11)
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    dependsOn(tasks.withType<MCTask>())
}

val standalone = task("standalone", type = Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    archiveClassifier.set("standalone")

    from(configurations.compileClasspath.get().filter{ it.exists() }.map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)

    manifest {
        attributes("Main-Class" to "automata.AutomataToolKt")
    }
}

tasks.getByName("jar") {
    dependsOn(standalone)
}