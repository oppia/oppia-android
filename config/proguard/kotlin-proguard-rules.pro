# Proguard rules to workaround Kotlin-specific issues that come up with Proguard.

# These dependencies are actually wrong: the AndroidX versions should be available but current
# Kotlin dependencies seem to reference the support library ones, instead. This could potentially
# run into runtime issues if something is unintentionally removed.
-dontwarn android.support.annotation.Keep
-dontwarn android.support.annotation.VisibleForTesting
