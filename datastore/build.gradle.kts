plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("maven-publish")
    id("signing")
}


group = "io.github.BlueAl98"
version = "1.0.0"
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }



}

dependencies {

    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)

    implementation (libs.androidx.datastore.preferences) // Asegúrate de usar la versión adecuada


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


signing {
    useGpgCmd()
    sign(publishing.publications)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "io.github.BlueAl98"
                artifactId = "datastore"
                version = "1.0.0"

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
        }
    }
}