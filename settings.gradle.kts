pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FoodRoutine"

include(":core")

// The :app module needs the Android SDK. Include it only when an SDK is
// available so that :core can be built and tested on machines without one.
val localProps = File(rootDir, "local.properties")
val hasSdkInLocalProps = localProps.exists() &&
    localProps.readLines().any { it.trim().startsWith("sdk.dir=") }
val hasAndroidSdk = hasSdkInLocalProps ||
    System.getenv("ANDROID_HOME") != null ||
    System.getenv("ANDROID_SDK_ROOT") != null

if (hasAndroidSdk) {
    include(":app")
} else {
    logger.warn("Android SDK not found - skipping :app module (building :core only).")
}
