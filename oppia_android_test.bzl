load("@rules_jvm_external//:defs.bzl", "artifact")
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

def oppia_android_test(name, srcs, test_manifest, custom_package, resource_files,
                       test_class, src_library_name):

    kt_android_library(
        name = name + "_lib",
        custom_package = custom_package,
        srcs = srcs,
        resource_files = resource_files,
        manifest = test_manifest,
        deps = [
            ":" + src_library_name,
            ":dagger",
            "@robolectric//bazel:android-all",
            "@maven//:org_robolectric_robolectric",
            artifact("org.jetbrains.kotlin:kotlin-test-junit"),
            artifact("com.google.truth:truth"),
            artifact("org.jetbrains.kotlinx:kotlinx-coroutines-core"),
            artifact("org.jetbrains.kotlinx:kotlinx-coroutines-android"),
            artifact("org.jetbrains.kotlinx:kotlinx-coroutines-test"),
            artifact("androidx.lifecycle:lifecycle-livedata-ktx"),
            artifact("io.fabric.sdk.android:fabric:1.4.7"),
            artifact("org.mockito:mockito-core:2.19.0"),
            artifact("androidx.test.ext:junit"),
            artifact("com.github.bumptech.glide:glide"),
            artifact("com.github.bumptech.glide:compiler"),
            artifact("com.caverock:androidsvg-aar"),
            artifact("androidx.appcompat:appcompat"),
            artifact("androidx.core:core-ktx"),
            artifact("junit:junit"),
            artifact("org.jetbrains.kotlin:kotlin-stdlib-jdk7:jar"),
        ],
    )

    native.android_local_test(
       name = name + "_test",
       custom_package = custom_package,
       test_class = test_class,
       manifest = test_manifest,
       deps = [
           ":" + name + "_lib",
           "@robolectric//bazel:android-all",
           "@maven//:org_robolectric_robolectric",
           artifact("com.google.truth:truth"),
           artifact("org.jetbrains.kotlin:kotlin-test-junit:1.3.72"),
           artifact("org.jetbrains.kotlinx:kotlinx-coroutines-core"),
           artifact("org.jetbrains.kotlinx:kotlinx-coroutines-android"),
           artifact("org.jetbrains.kotlinx:kotlinx-coroutines-test"),
           artifact("androidx.lifecycle:lifecycle-livedata-ktx"),
           artifact("io.fabric.sdk.android:fabric:1.4.7"),
           artifact("org.mockito:mockito-core:2.19.0"),
           artifact("androidx.test.ext:junit"),
           artifact("com.github.bumptech.glide:glide"),
           artifact("com.github.bumptech.glide:compiler"),
           artifact("com.caverock:androidsvg-aar"),
           artifact("androidx.appcompat:appcompat"),
           artifact("androidx.core:core-ktx"),
           artifact("junit:junit"),
           artifact("org.jetbrains.kotlin:kotlin-stdlib-jdk7:jar"),
       ],
     )