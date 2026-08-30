// Intentionally empty: plugins are applied per-module (:core applies the
// Kotlin JVM plugin; :app applies AGP, Kotlin Android, Compose and KSP).
// Declaring them here would (a) force Android Gradle Plugin resolution on
// machines without the Android SDK and (b) leak the Kotlin plugin onto the
// root classpath, clashing with :app's versioned kotlin.android request.
