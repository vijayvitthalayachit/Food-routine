// Android plugins (AGP, KSP, Compose compiler) are applied only in :app so
// that :core remains buildable on machines without the Android SDK.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}
