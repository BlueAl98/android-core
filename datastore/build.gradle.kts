import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("signing")
}

android {
    namespace = "com.nayibit.datastore"
    compileSdk = 35

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

signing {
    useGpgCmd()
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("io.github.blueal98", "datastore", "1.0.0")

    pom {
        name.set("Nayibit Datastore")
        description.set("Android DataStore library")
        url.set("https://github.com/BlueAl98/android-core")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("BlueAl98")
                name.set("Najib Loera")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/BlueAl98/android-core.git")
            developerConnection.set("scm:git:ssh://github.com/BlueAl98/android-core.git")
            url.set("https://github.com/BlueAl98/android-core")
        }
    }
}
