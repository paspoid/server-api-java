plugins {
    kotlin("jvm") version "1.9.22"
    `java-library`
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
    sourceSets {
        main {
            kotlin.setSrcDirs(listOf("src/main/java"))
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            groupId = "paspoid"
            artifactId = "server-api"
            version = "0.1.0"
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "ServerApiCdn"
            url = uri("${rootProject.layout.buildDirectory.get()}/server-api-repo")
        }
    }
}

tasks.register<JavaExec>("runJava") {
    group = "application"
    description = "Runs the Java SDK example"
    mainClass.set("examples.Main")
    classpath = sourceSets["main"].runtimeClasspath + files("examples")
}

tasks.register<JavaExec>("runKotlin") {
    group = "application"
    description = "Runs the Kotlin SDK example"
    mainClass.set("examples.MainKt")
    classpath = sourceSets["main"].runtimeClasspath + files("examples")
}
