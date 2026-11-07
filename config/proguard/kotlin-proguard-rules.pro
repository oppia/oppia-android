# Proguard rules to workaround Kotlin-specific issues that come up with Proguard.

# https://github.com/Kotlin/kotlinx.coroutines/issues/2046 describes some of the classes which are
# safe to ignore due to kotlinx.coroutines dependencies. Others that are needed of the following are
# sourced from: https://github.com/Kotlin/kotlinx.coroutines/blob/bc120a/kotlinx-coroutines-core/jvm/resources/META-INF/com.android.tools/proguard/coroutines.pro
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.android.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
