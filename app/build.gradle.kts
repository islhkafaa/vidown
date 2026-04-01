import com.android.build.api.variant.FilterConfiguration
import com.android.build.api.variant.impl.VariantOutputImpl
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)

    base
}

base {
    archivesName.set("vidown-\${android.defaultConfig.versionName}")
}

val localProperties = Properties()
val localPropertiesFile: File? = rootProject.file("local.properties")
if (localPropertiesFile!!.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "app.vidown"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.vidown"
        minSdk = 28
        targetSdk = 36
        versionCode = 13
        versionName = "0.6.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val storeFileProp = localProperties.getProperty("RELEASE_STORE_FILE")
                ?: project.findProperty("RELEASE_STORE_FILE") as String?

            storeFile = storeFileProp?.let { file(it) }
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
                ?: project.findProperty("RELEASE_STORE_PASSWORD") as String?
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                ?: project.findProperty("RELEASE_KEY_ALIAS") as String?
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
                ?: project.findProperty("RELEASE_KEY_PASSWORD") as String?
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = false
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}


androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val outputImpl = output as? VariantOutputImpl

            val abi = output.filters.find {
                it.filterType == FilterConfiguration.FilterType.ABI
            }?.identifier

            if (abi != null && outputImpl != null) {
                val version = variant.outputs.first().versionName.get()
                outputImpl.outputFileName.set("vidown-$abi-$version.apk")
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.work.runtime.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.youtubedl.android)
    implementation(libs.youtubedl.android.ffmpeg)
    implementation(libs.coil.compose)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.okhttp)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    implementation(libs.compose.material.icons.extended)

    implementation(libs.compose.ui.google.fonts)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.compose.ui.tooling)
}
