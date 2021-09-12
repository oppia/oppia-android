# No special Proguard rules are needed for Moshi in general since the app relies on compile-time
# generation. However, some of the reflection dependencies are still pulled in & produce warnings.

-dontwarn com.squareup.moshi.kotlin.codegen.api.ProguardConfig
-dontwarn com.squareup.moshi.kotlin.reflect.**

# This is a really specific, implementation-specific silence whose need isn't entirely clear.
-dontwarn com.squareup.moshi.kotlinpoet.classinspector.elements.shaded.com.google.auto.common.Overrides*
