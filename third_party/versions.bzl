"""
Contains all versions of third-party dependencies for the Oppia Android app. Note: dependencies
added to the version dictionary below will automatically become available as a new third-party
dependency elsewhere in the app.

Note that dependencies can only be represented once in the list--that's by design to protect against
one-version violations. See https://docs.bazel.build/versions/master/best-practices.html#versioning
for more information on multi-version violations.

Note that each of the dependencies will have all ':'s & '.'s replaced with underscores. For example,
the dependency "android.arch.core:core-testing": "1.1.1" will be referencable with the following:
//third_party:android_arch_core_core-testing (no version is included so that versions can be easily
updated here).

Note that for making any change in 'MAVEN_PRODUCTION_DEPENDENCY_VERSIONS' or
'MAVEN_TEST_DEPENDENCY_VERSIONS' dicts please refer to:
https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies
"""

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should contain only production (non-test) dependencies.
MAVEN_PRODUCTION_DEPENDENCY_VERSIONS = {
    "androidx.annotation:annotation": "1.1.0",
    "androidx.appcompat:appcompat": "1.2.0",
    "androidx.constraintlayout:constraintlayout": "1.1.3",
    "androidx.core:core": "1.0.1",
    "androidx.core:core-ktx": "1.0.1",
    "androidx.databinding:databinding-adapters": "3.4.2",
    "androidx.databinding:databinding-common": "3.4.2",
    "androidx.databinding:databinding-compiler": "3.4.2",
    "androidx.databinding:databinding-runtime": "3.4.2",
    "androidx.drawerlayout:drawerlayout": "1.1.0",
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
    "androidx.test.uiautomator:uiautomator": "2.2.0",
    "androidx.viewpager2:viewpager2": "1.0.0",
    "androidx.viewpager:viewpager": "1.0.0",
    "androidx.work:work-runtime": "2.4.0",
    "androidx.work:work-runtime-ktx": "2.4.0",
    "com.android.support:support-annotations": "28.0.0",
    "com.crashlytics.sdk.android:crashlytics": "2.9.8",
    "com.github.bumptech.glide:compiler": "4.11.0",
    "com.github.bumptech.glide:glide": "4.11.0",
    "com.google.android.flexbox:flexbox": "3.0.0",
    "com.google.android.material:material": "1.3.0",
    "com.google.firebase:firebase-analytics": "17.5.0",
    "com.google.firebase:firebase-crashlytics": "17.1.1",
    "com.google.gms:google-services": "4.3.3",
    "com.google.guava:guava": "28.1-android",
    "com.google.protobuf:protobuf-javalite": "3.17.3",
    "com.squareup.moshi:moshi-kotlin": "1.11.0",
    "com.squareup.moshi:moshi-kotlin-codegen": "1.11.0",
    "com.squareup.okhttp3:okhttp": "4.7.2",
    "com.squareup.retrofit2:converter-moshi": "2.5.0",
    "com.squareup.retrofit2:retrofit": "2.9.0",
    "de.hdodenhof:circleimageview": "3.0.1",
    "io.fabric.sdk.android:fabric": "1.4.7",
    "javax.annotation:javax.annotation-api:jar": "1.3.2",
    "javax.inject:javax.inject": "1",
    "nl.dionsegijn:konfetti": "1.2.5",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk7:jar": "1.3.72",
    "org.jetbrains.kotlinx:kotlinx-coroutines-android": "1.4.1",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core": "1.4.1",
    "org.jetbrains:annotations:jar": "13.0",
}

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should only contain test-only dependencies. These are dependencies that are guaranteed
# cannot be included in production builds of the app.
MAVEN_TEST_DEPENDENCY_VERSIONS = {
    "android.arch.core:core-testing": "1.1.1",
    "androidx.arch.core:core-testing": "2.1.0",
    "androidx.test.espresso:espresso-accessibility": "3.1.0",
    "androidx.test.espresso:espresso-contrib": "3.1.0",
    "androidx.test.espresso:espresso-core": "3.2.0",
    "androidx.test.espresso:espresso-intents": "3.1.0",
    "androidx.test.ext:junit": "1.1.1",
    "androidx.test.ext:truth": "1.4.0",
    "androidx.test:core": "1.0.0",
    "androidx.test:runner": "1.2.0",
    "androidx.work:work-testing": "2.4.0",
    "com.github.bumptech.glide:mocks": "4.11.0",
    "com.google.protobuf:protobuf-java": "3.17.3",
    "com.google.truth.extensions:truth-liteproto-extension": "1.1.3",
    "com.google.truth:truth": "0.43",
    "com.squareup.okhttp3:mockwebserver": "4.7.2",
    "com.squareup.retrofit2:retrofit-mock": "2.5.0",
    "junit:junit": "4.12",
    "org.jetbrains.kotlin:kotlin-compiler-embeddable": "1.5.0",
    "org.jetbrains.kotlin:kotlin-reflect": "1.3.41",
    "org.jetbrains.kotlin:kotlin-test-junit": "1.3.72",
    "org.jetbrains.kotlinx:kotlinx-coroutines-test": "1.2.2",
    "org.mockito.kotlin:mockito-kotlin": "3.2.0",
    "org.mockito:mockito-core": "2.19.0",
    "org.robolectric:annotations": "4.5",
    "org.robolectric:robolectric": "4.5",
}

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
HTTP_DEPENDENCY_VERSIONS = {
    "android_bundletool": {
        "sha": "1e8430002c76f36ce2ddbac8aadfaf2a252a5ffbd534dab64bb255cda63db7ba",
        "version": "1.8.0",
    },
    "dagger": {
        "sha": "9e69ab2f9a47e0f74e71fe49098bea908c528aa02fa0c5995334447b310d0cdd",
        "version": "2.28.1",
    },
    "protobuf_tools": {
        "version": "3.11.0",
    },
    "rules_java": {
        "sha": "34b41ec683e67253043ab1a3d1e8b7c61e4e8edefbcad485381328c934d072fe",
        "version": "4.0.0",
    },
    "rules_jvm": {
        "sha": "f36441aa876c4f6427bfb2d1f2d723b48e9d930b62662bf723ddfb8fc80f0140",
        "version": "4.1",
    },
    "rules_kotlin": {
        "sha": "6194a864280e1989b6d8118a4aee03bb50edeeae4076e5bc30eef8a98dcd4f07",
        "version": "v1.5.0-alpha-2",
    },
    "rules_proto": {
        "sha": "e0cab008a9cdc2400a1d6572167bf9c5afc72e19ee2b862d18581051efab42c9",
        "version": "c0b62f2f46c85c16cb3b5e9e921f0d00e3101934",
    },
}

def get_maven_dependencies():
    """
    Returns a list of maven dependencies to install to fulfill third-party dependencies.
    """
    return (["%s:%s" % (name, version) for name, version in MAVEN_PRODUCTION_DEPENDENCY_VERSIONS.items()] +
            ["%s:%s" % (name, version) for name, version in MAVEN_TEST_DEPENDENCY_VERSIONS.items()])
