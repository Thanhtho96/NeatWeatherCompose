import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = rootProject.file("keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    namespace = "com.tt.weatherapp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    signingConfigs {
        create("config") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
        }
    }

    defaultConfig {
        applicationId = "com.tt.weatherapp"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions += "app"
    productFlavors {
        create("production") {
            dimension = "app"
            buildConfigField(
                "String",
                "OPEN_WEATHER_MAP_APP_ID",
                "\"19150b28972e472b00d965a87f00b49a\""
            )
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "@string/app_name_debug")
        }

        getByName("release") {
            isShrinkResources = true
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("config")
            resValue("string", "app_name", "@string/app_name_release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        stabilityConfigurationFiles =
            listOf(rootProject.layout.projectDirectory.file("stability_config.conf"))
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.compose.material)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.preview)
    debugImplementation(libs.compose.ui.debug)
    implementation(libs.compose.activity)
    implementation(libs.compose.lifecycle)
    implementation(libs.ktx.core)
    implementation(libs.life.cycle)
    implementation(libs.data.store)
    implementation(libs.koin)
    implementation(libs.retrofit)
    implementation(libs.retrofit.json.converter)
    implementation(libs.ok.http)
    implementation(libs.ok.http.logging)
    implementation(libs.room)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)
    implementation(libs.coil)
    implementation(libs.coil.network)
    val firebaseBom = platform(libs.firebase.bom)
    implementation(firebaseBom)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.location)
    implementation(libs.system.ui.controller)
    implementation(libs.permissions)
    implementation(libs.flowlayout)
    implementation(libs.navigation)
    implementation(libs.widget)
    implementation(libs.kt.serialization)
    implementation(libs.worker)
}