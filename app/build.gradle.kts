import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
	// Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
	id("org.jetbrains.kotlin.jvm") version "1.7.0"
	id("com.github.ben-manes.versions") version "0.42.0"
	id("org.springframework.boot") version "2.7.1"


	// Apply the application plugin to add support for building a CLI application in Java.
	application
}

repositories {
	// Use Maven Central for resolving dependencies.
	mavenCentral()
}

dependencies {
	implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.7.0"))
	implementation(platform("org.springframework.boot:spring-boot-dependencies:2.7.1"))

	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.springframework.boot:spring-boot-starter")
}

application {
	// Define the main class for the application.
	mainClass.set("dev.capybaralabs.shipa.LauncherKt")
}

fun isNonStable(version: String): Boolean {
	val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
	val regex = "^[0-9,.v-]+(-r)?$".toRegex()
	val isStable = stableKeyword || regex.matches(version)
	return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
	rejectVersionIf {
		isNonStable(candidate.version) && !isNonStable(currentVersion)
	}
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
	checkConstraints = true
	checkBuildEnvironmentConstraints = true
	gradleReleaseChannel = "current"
}

