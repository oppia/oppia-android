# Proguard rules to workaround Kotlin-specific issues that come up with Proguard.

# These dependencies are actually wrong: the AndroidX versions should be available but current
# Kotlin dependencies seem to reference the support library ones, instead. This could potentially
# run into runtime issues if something is unintentionally removed.
-dontwarn android.support.annotation.Keep
-dontwarn android.support.annotation.VisibleForTesting

# https://github.com/Kotlin/kotlinx.coroutines/issues/2046 describes some of the classes which are
# safe to ignore due to kotlinx.coroutines dependencies. All of the following are sourced from:
# https://github.com/Kotlin/kotlinx.coroutines/blob/bc120a/kotlinx-coroutines-core/jvm/resources/META-INF/com.android.tools/proguard/coroutines.pro
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-dontwarn java.lang.ClassValue
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.SignalHandler
-dontwarn sun.misc.Signal
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# It seems like this can be safely ignored since it shouldn't be used on Android (though it's
# unclear why it's causing a Proguard issue in the first place). For reference, see:
# https://github.com/Kotlin/kotlinx.coroutines/blob/3574c2feca23c3e8a1ad00b5bf92e2bf04d95060/kotlinx-coroutines-core/jvm/src/internal/ExceptionsConstructor.kt#L17
-dontwarn kotlinx.coroutines.internal.ClassValueCtorCache

# It's not clear why there's a buildSequence issue here as buildSequence was removed (though not
# until Kotlin 1.7 which isn't being used by the build tools yet). This one is odd, but it seems
# fine to silence.
-dontwarn kotlin.sequences.SequencesKt__SequenceBuilderKt
