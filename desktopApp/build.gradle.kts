import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "io.github.tkostas.kscanutils.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KScanUtils"
            packageVersion = "1.0.2"
            vendor = "Kostas Tsolis"
            licenseFile.set(rootProject.file("LICENSE"))

            windows {
                iconFile.set(project.file("src/main/resources/icons/app-icon.ico"))
                menuGroup = "KScanUtils"
                menu = true
                shortcut = true
                upgradeUuid = "DBC0F992-C2B1-3338-BF05-BC580B4F8031"
            }
            linux {
                iconFile.set(project.file("src/main/resources/icons/app-icon-512.png"))
            }
        }
    }
}
