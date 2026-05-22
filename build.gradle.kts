plugins {
    id("com.android.library") version "8.2.2"
    id("org.jetbrains.kotlin.android") version "1.9.22"
    id("maven-publish")
}

group = "com.github.MikTuga"
version = "0.1.0"

android {
    namespace = "com.miktuga.design"
    compileSdk = 34

    defaultConfig {
        minSdk = 22
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        disable += "ExpiredTargetSdkVersion"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    api("androidx.core:core-ktx:1.12.0")
    api("androidx.appcompat:appcompat:1.6.1")
    api("com.google.android.material:material:1.11.0")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.MikTuga"
            artifactId = "tuga-design"
            version = "0.1.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
