import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
        publishLibraryVariants("release")
    }

    val xcf = XCFramework("FSensor")
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "FSensor"
            isStatic = true
            xcf.add(this)
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {}
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation("androidx.annotation:annotation:1.8.2")
        }
    }
}

android {
    namespace = "com.tracqi.fsensor"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "com.tracqi"
        artifactId = "fsensor"
    }
}
