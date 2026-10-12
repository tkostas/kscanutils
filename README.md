# KScanUtils

KScanUtils is a private, cross-platform desktop application for scanning documents and working with PDF files. It can scan and export documents as images or PDFs, merge PDF files, and rotate or remove PDF pages.

This is a Kotlin Multiplatform project targeting Desktop (JVM).

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
      folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and
options:

- Desktop app:
    - Hot reload: `./gradlew :desktopApp:hotRun --auto`
    - Standard run: `./gradlew :desktopApp:run`

### Building installers

- Windows MSI: `./gradlew :desktopApp:packageMsi`
- Linux DEB: `./gradlew :desktopApp:packageDeb`

Publishing a GitHub Release automatically builds both installers and attaches them to the release. The same workflow can be started manually from the repository's **Actions** tab; manual runs keep the installers as workflow artifacts instead of attaching them to a release.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Desktop tests: `./gradlew :shared:jvmTest`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## License

KScanUtils is licensed under the [Apache License 2.0](LICENSE).

The application uses Apache PDFBox 3.0.8, which is also distributed under the Apache License 2.0.
