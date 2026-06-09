import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.owasp.dependency.check)
}

android {
    namespace = "com.example.lumina"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lumina"
        minSdk = 29
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Logic to load API Key:
        // 1. Check local.properties (for local development)
        // 2. Check System Environment variable (for GitHub Actions)
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        
        val safeBrowsingKey = localProperties.getProperty("GOOGLE_SAFE_BROWSING_KEY") 
            ?: System.getenv("GOOGLE_SAFE_BROWSING_KEY") 
            ?: ""

        buildConfigField("String", "SAFE_BROWSING_KEY", "\"$safeBrowsingKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = false
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
}

configurations.all {
    // ─── Force-upgrade vulnerable transitive dependencies ────────────────────────
    // io.netty pulled by ML Kit barcode scanning (grpc-netty 1.57.2 u0026 1.69.1)
    // Force all netty modules to 4.1.121.Final which patches all known CVEs.
    resolutionStrategy {
        force("io.netty:netty-all:4.1.121.Final")
        force("io.netty:netty-buffer:4.1.121.Final")
        force("io.netty:netty-codec:4.1.121.Final")
        force("io.netty:netty-codec-http:4.1.121.Final")
        force("io.netty:netty-codec-http2:4.1.121.Final")
        force("io.netty:netty-codec-socks:4.1.121.Final")
        force("io.netty:netty-common:4.1.121.Final")
        force("io.netty:netty-handler:4.1.121.Final")
        force("io.netty:netty-handler-proxy:4.1.121.Final")
        force("io.netty:netty-resolver:4.1.121.Final")
        force("io.netty:netty-transport:4.1.121.Final")
        force("io.netty:netty-transport-native-unix-common:4.1.121.Final")
        // Protobuf pulled by gRPC - force to latest patched version
        force("com.google.protobuf:protobuf-java:4.30.2")
        force("com.google.protobuf:protobuf-java-util:4.30.2")
        force("com.google.protobuf:protobuf-kotlin:4.30.2")
    }
    exclude(group = "com.intellij", module = "annotations")
}

dependencies {
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.onnxruntime.android)

    // SQLCipher for database encryption
    implementation(libs.sqlcipher)
    implementation(libs.androidx.ui.graphics)

    // ML Kit Barcode Scanning
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.camera.mlkit.vision)

    // CameraX dependencies for camera integration
    implementation(libs.androidx.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // GeckoView
    implementation(libs.geckoview)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.datastore.core)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    implementation(libs.google.material)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Mozilla Components
    implementation(libs.mozilla.concept.engine)
    implementation(libs.mozilla.feature.addons)
    implementation(libs.mozilla.support.webextensions)
    implementation(libs.mozilla.browser.state)
    implementation(libs.mozilla.lib.state)
    implementation(libs.mozilla.feature.prompts)
    implementation(libs.mozilla.support.base)
    implementation(libs.mozilla.support.utils)
    implementation(libs.androidx.recyclerview)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Tor
    implementation(libs.tor.android)
    implementation(libs.jtorctl)
}

// ─── OWASP Dependency-Check ───────────────────────────────────────────────────
// Resolve NVD API key at configuration time (local.properties → env var → empty)
val nvdApiKey: String = run {
    val localProps = Properties()
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) localProps.load(localPropsFile.inputStream())
    localProps.getProperty("NVD_API_KEY") ?: System.getenv("NVD_API_KEY") ?: ""
}

dependencyCheck {
    // Fail the build if any dependency has a CVSS score >= 7 (High/Critical)
    failBuildOnCVSS = 7.0f

    // Report formats: HTML (human-readable) + JSON (CI/CD parsing)
    formats = listOf("HTML", "JSON")

    // Output directory relative to this module's build dir
    outputDirectory = layout.buildDirectory.dir("reports/dependency-check").get().asFile.absolutePath

    // Suppress known false positives (edit the XML file as needed)
    suppressionFile = "${rootProject.projectDir}/dependency-check-suppressions.xml"

    // NVD API key — avoids severe rate limiting on the free tier
    // Obtain a free key at: https://nvd.nist.gov/developers/request-an-api-key
    // Set via: export NVD_API_KEY=your_key  OR add NVD_API_KEY=<key> to local.properties
    nvd {
        apiKey = nvdApiKey
        delay = 4000 // ms between NVD API calls (required for free-tier key)
    }

    // Disable analyzers irrelevant to Android/JVM projects
    analyzers {
        assemblyEnabled = false   // .NET assemblies — not needed
        nuspecEnabled = false     // NuGet packages — not needed
        nugetconfEnabled = false  // NuGet config — not needed
        pyDistributionEnabled = false
        pyPackageEnabled = false
        rubygemsEnabled = false
        cmakeEnabled = false
        autoconfEnabled = false
        composerEnabled = false
        nodeEnabled = false       // We only care about JVM/Android deps
        ossIndexEnabled = false   // Disable Sonatype OSS Index (requires Sonatype account, causes network failures)
        centralEnabled = false    // Disable Maven Central analyzer (same reason)
    }
}
