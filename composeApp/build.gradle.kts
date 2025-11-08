import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("com.itextpdf:kernel:7.2.1")
            implementation("com.itextpdf:layout:7.2.1")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("com.sun.mail:javax.mail:1.6.2")
            implementation("javax.activation:activation:1.1.1")
        }
    }
}

buildConfig {
    packageName("org.example.cgeproject")
    buildConfigField("String", "EMAIL_FROM", "\"${localProperties.getProperty("email.from") ?: ""}\"")
    buildConfigField("String", "EMAIL_PASSWORD", "\"${localProperties.getProperty("email.password") ?: ""}\"")
}

compose.desktop {
    application {
        mainClass = "org.example.cgeproject.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.cgeproject"
            packageVersion = "1.0.0"
        }
    }
}