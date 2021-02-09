"""
Contains all versions of third-party dependencies for the Oppia Android app. Note: dependencies
added to the version dictionary below will automatically become available as a new third-party
dependency elsewhere in the app.

Note that dependencies can only be represented once in the list--that's by design to protect against
one-version violations. See https://docs.bazel.build/versions/master/best-practices.html#versioning
for more information on mutli-version violations.

Note that each of the dependencies will have all ':'s & '.'s replaced with underscores. For example,
the dependency "android.arch.core:core-testing": "1.1.1" will be referencable with the following:
//third_party:android_arch_core_core-testing (no version is included so that versions can be easily
updated here).
"""

# Note to developers: Please keep this list sorted by key to make it easier to find dependencies.
DEPENDENCY_VERSIONS = {
    "android.arch.core:core-testing": "1.1.1",
    "androidx.annotation:annotation": "1.1.0",
    "androidx.appcompat:appcompat": "1.0.2",
    "androidx.arch.core:core-testing": "2.1.0",
    "androidx.constraintlayout:constraintlayout": "1.1.3",
    "androidx.core:core": "1.0.1",
    "androidx.core:core-ktx": "1.0.1",
    "androidx.databinding:databinding-adapters": "3.4.2",
    "androidx.databinding:databinding-common": "3.4.2",
    "androidx.databinding:databinding-compiler": "3.4.2",
    "androidx.databinding:databinding-runtime": "3.4.2",
    "androidx.lifecycle:lifecycle-extensions": "2.2.0",
    "androidx.lifecycle:lifecycle-livedata-core": "2.2.0",
    "androidx.lifecycle:lifecycle-livedata-ktx": "2.2.0",
    "androidx.lifecycle:lifecycle-viewmodel-ktx": "2.2.0",
    "androidx.multidex:multidex": "2.0.1",
    "androidx.multidex:multidex-instrumentation": "2.0.0",
    "androidx.navigation:navigation-fragment": "2.0.0",
    "androidx.navigation:navigation-fragment-ktx": "2.0.0",
    "androidx.navigation:navigation-ui": "2.0.0",
    "androidx.navigation:navigation-ui-ktx": "2.0.0",
    "androidx.recyclerview:recyclerview": "1.0.0",
    "androidx.room:room-runtime": "2.2.5",
    "androidx.test.espresso:espresso-contrib": "3.1.0",
    "androidx.test.espresso:espresso-core": "3.2.0",
    "androidx.test.espresso:espresso-intents": "3.1.0",
    "androidx.test.ext:junit": "1.1.1",
    "androidx.test:runner": "1.2.0",
    "androidx.viewpager:viewpager": "1.0.0",
    "androidx.work:work-runtime": "2.4.0",
    "androidx.work:work-runtime-ktx": "2.4.0",
    "androidx.work:work-testing": "2.4.0",
    "com.android.support:support-annotations": "28.0.0",
    "com.caverock:androidsvg-aar": "1.4",
    "com.chaos.view:pinview": "1.4.4",
    "com.crashlytics.sdk.android:crashlytics": "2.9.8",
    "com.github.bumptech.glide:glide": "4.11.0",
    "com.github.bumptech.glide:mocks": "4.11.0",
    "com.google.android.material:material": "1.2.0-alpha02",
    "com.google.android:flexbox": "2.0.1",
    "com.google.firebase:firebase-analytics": "17.5.0",
    "com.google.firebase:firebase-crashlytics": "17.1.1",
    "com.google.gms:google-services": "4.3.3",
    "com.google.guava:guava": "28.1-android",
    "com.google.truth:truth": "0.43",
    "com.squareup.retrofit2:converter-gson": "2.5.0",
    "com.squareup.retrofit2:retrofit": "2.9.0",
    "de.hdodenhof:circleimageview": "3.0.1",
    "io.fabric.sdk.android:fabric": "1.4.7",
    "javax.annotation:javax.annotation-api:jar": "1.3.2",
    "junit:junit": "4.12",
    "nl.dionsegijn:konfetti": "1.2.5",
    "org.jetbrains.kotlin:kotlin-reflect": "1.3.41",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk7:jar": "1.3.72",
    "org.jetbrains.kotlin:kotlin-test-junit": "1.3.72",
    "org.jetbrains.kotlinx:kotlinx-coroutines-android": "1.3.2",
    "org.jetbrains.kotlinx:kotlinx-coroutines-test": "1.2.2",
    "org.jetbrains:annotations:jar": "13.0",
    "org.mockito:mockito-core": "2.19.0",
    "org.robolectric:annotations": "4.3",
    "org.robolectric:robolectric": "4.3",
}

def get_maven_dependencies():
    """
    Returns a list of maven dependencies to install to fulfill third-party dependencies.
    """
    return ["%s:%s" % (name, version) for name, version in DEPENDENCY_VERSIONS.items()]
