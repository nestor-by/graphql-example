import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Files
import java.nio.file.Paths
import deps
import deps.versions
import org.gradle.initialization.StartParameterBuildOptions.BuildScanOption
import org.gradle.internal.scan.config.BuildScanConfig

group = "com.mm.graphql"
version = "1.0-SNAPSHOT"

buildscript {
  repositories {
    mavenCentral()
    jcenter()
    maven { url = uri(deps.build.repositories.snapshots) }
    maven { url = uri(deps.build.repositories.plugins) }
    maven { url = uri(deps.build.repositories.snapshots) }
  }

  configurations.all {
    resolutionStrategy {
      force("net.sf.proguard:proguard-base:6.1.0beta1")
    }
  }

  dependencies {
    classpath(deps.kotlin.gradlePlugin)
    classpath(deps.apollo.gradlePlugin)
    classpath("com.github.jengelman.gradle.plugins:shadow:4.0.2")
  }
}

plugins {
  id("com.github.ben-manes.versions") version "0.20.0"
}

repositories {
  mavenCentral()
}

allprojects {

  repositories {
    google()
    mavenCentral()
    jcenter()
    maven { url = uri(deps.build.repositories.kotlineap) }
    maven { url = uri(deps.build.repositories.kotlinx) }
    maven { url = uri(deps.build.repositories.jitpack) }
    maven { url = uri(deps.build.repositories.snapshots) }
  }

  configurations.all {
    resolutionStrategy.eachDependency {
      when {
        requested.name.startsWith("kotlin-stdlib") -> {
          useTarget("${requested.group}:${requested.name.replace("jre", "jdk")}:${requested.version}")
        }
        else -> when (requested.group) {
          "org.jetbrains.kotlin" -> useVersion(versions.kotlin)
        }
      }
    }
  }
}

fun path(project: Project, vararg folderName: String): Boolean {
  return folderName.any { Files.isDirectory(Paths.get(project.projectDir.absolutePath, "src", "main", it)) }
}


fun projects(vararg type: String): List<Project> {
  return subprojects.filter { path(it, *type) }
}

configure(projects("java")) {
  apply(plugin = "java")
}

configure(projects("kotlin")) {
  apply(plugin = "kotlin")
  dependencies {
    "compile"(kotlin("stdlib-jdk8"))
  }
}

configure(projects("java") + projects("kotlin")) {
  apply(plugin = "idea")
  apply(plugin = "com.github.johnrengelman.shadow")
  repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
    maven(url = "http://repository.jetbrains.com/spek")
    maven(url = "http://repository.jetbrains.com/kotlin-nosql")
  }

  val jupiterVersion = "5.1.1"
  dependencies {
    "testCompile"("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    "testCompile"("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    "testRuntime"("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")

    "testCompile"("com.nhaarman:mockito-kotlin:1.5.0") {
      exclude(group = "org.jetbrains.kotlin")
    }
  }
}

tasks {
  withType<Wrapper> {
    gradleVersion = "4.10.2"
    distributionUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
  }


  withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }

  withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = build.standardFreeKotlinCompilerArgs
      jvmTarget = "1.8"
    }
  }

  "dependencyUpdates"(DependencyUpdatesTask::class) {
    resolutionStrategy {
      componentSelection {
        all {
          val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview")
              .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
              .any { it.matches(candidate.version) }
          if (rejected) {
            reject("Release candidate")
          }
        }
      }
    }
  }
}