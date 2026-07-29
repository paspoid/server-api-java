plugins {
    id("com.android.library") version "8.2.2"
    id("org.jetbrains.kotlin.android") version "1.9.22"
    `maven-publish`
}

android {
    namespace = "id.paspo.sdk"
    compileSdk = 34

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java", "examples")
        }
    }

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "consumer-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

val jvmRunOnly: Configuration by configurations.creating

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Real org.json for running on JVM (replaces Android stubs)
    jvmRunOnly("org.json:json:20240303")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "id.paspo"
                artifactId = "sdk"
                version = "0.1.0"
                from(components["release"])
            }
        }
    }
}

tasks.register<JavaExec>("runJava") {
    group = "application"
    description = "Runs the Java SDK example"
    mainClass.set("examples.Main")
    dependsOn("compileReleaseJavaWithJavac", "compileReleaseKotlin")
    doFirst {
        // jvmRunOnly placed first so real org.json shadows Android stubs
        classpath = files(
            jvmRunOnly,
            tasks.named("compileReleaseJavaWithJavac").get().outputs.files,
            tasks.named("compileReleaseKotlin").get().outputs.files,
            configurations.getByName("releaseRuntimeClasspath")
        )
    }
}

tasks.register<JavaExec>("runKotlin") {
    group = "application"
    description = "Runs the Kotlin SDK example"
    mainClass.set("examples.MainKt")
    dependsOn("compileReleaseJavaWithJavac", "compileReleaseKotlin")
    doFirst {
        // jvmRunOnly placed first so real org.json shadows Android stubs
        classpath = files(
            jvmRunOnly,
            tasks.named("compileReleaseJavaWithJavac").get().outputs.files,
            tasks.named("compileReleaseKotlin").get().outputs.files,
            configurations.getByName("releaseRuntimeClasspath")
        )
    }
}

