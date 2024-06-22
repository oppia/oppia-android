"""
Contains all of the Maven dependencies that required to build the app and its test targets. These
are exposed via PRODUCTION_DEPENDENCY_VERSIONS and TEST_DEPENDENCY_VERSIONS.

Note that all versions listed in this file will automatically be exposed via //third_party library
wrappers. For example, the "androidx.lifecycle:lifecycle-extensions" dependency will be available
via //third_party:androidx_lifecycle_lifecycle-extensions. Test dependencies are only visible to
tests and test targets. None of these libraries are available to script builds.

Note also that dependencies can only be represented once in the list--that's by design to protect
against one-version violations. See
https://docs.bazel.build/versions/master/best-practices.html#versioning for more information on
multi-version violations.

The transitive dependencies required for each of the dependencies listed in this file are provided
in transitive_maven_versions.bzl. Both this & that file should be audited using
//scripts:validate_maven_dependencies to ensure that the lists are up-to-date and minimized.

Also, when changing the lists below please refer to
https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies.
"""

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should contain only production (non-test) dependencies. Note also that Dagger artifacts
# are manually included here for better integration with version list maintenance despite this being
# contrary to Dagger's suggested Bazel setup instructions.
PRODUCTION_DEPENDENCY_VERSIONS = {
    "androidx.annotation:annotation": "1.1.0",
    "androidx.appcompat:appcompat": "1.2.0",
    "androidx.constraintlayout:constraintlayout": "2.0.1",
    "androidx.core:core": "1.3.1",
    "androidx.core:core-ktx": "1.0.2",
    "androidx.databinding:databinding-adapters": "3.4.2",
    "androidx.databinding:databinding-common": "3.4.2",
    "androidx.databinding:databinding-runtime": "3.4.2",
    "androidx.drawerlayout:drawerlayout": "1.1.0",
    "androidx.exifinterface:exifinterface": "1.0.0",
    "androidx.fragment:fragment": "1.2.0",
    "androidx.lifecycle:lifecycle-common": "2.2.0",
    "androidx.lifecycle:lifecycle-livedata": "2.2.0",
    "androidx.lifecycle:lifecycle-livedata-core": "2.2.0",
    "androidx.lifecycle:lifecycle-livedata-ktx": "2.2.0",
    "androidx.lifecycle:lifecycle-process": "2.2.0",
    "androidx.multidex:multidex": "2.0.1",
    "androidx.navigation:navigation-ui": "2.0.0",
    "androidx.recyclerview:recyclerview": "1.1.0",
    "androidx.viewpager2:viewpager2": "1.0.0",
    "androidx.viewpager:viewpager": "1.0.0",
    "androidx.work:work-runtime": "2.4.0",
    "androidx.work:work-runtime-ktx": "2.4.0",
    "com.crashlytics.sdk.android:crashlytics": "2.9.8",
    "com.github.bumptech.glide:annotations": "4.11.0",
    "com.github.bumptech.glide:compiler": "4.11.0",
    "com.github.bumptech.glide:glide": "4.11.0",
    "com.google.android.flexbox:flexbox": "3.0.0",
    "com.google.android.gms:play-services-measurement-api": "17.5.0",
    "com.google.android.material:material": "1.3.0",
    "com.google.auto.value:auto-value-annotations": "1.8.1",
    "com.google.dagger:dagger": "2.41",
    "com.google.dagger:dagger-compiler": "2.41",
    "com.google.dagger:dagger-producers": "2.41",
    "com.google.dagger:dagger-spi": "2.41",
    "com.google.errorprone:error_prone_annotations": "2.11.0",
    "com.google.firebase:firebase-analytics": "17.5.0",
    "com.google.firebase:firebase-appcheck": "16.0.0",
    "com.google.firebase:firebase-appcheck-debug": "16.0.0",
    "com.google.firebase:firebase-appcheck-playintegrity": "16.0.0",
    "com.google.firebase:firebase-auth-ktx": "19.3.1",
    "com.google.firebase:firebase-common": "20.1.1",
    "com.google.firebase:firebase-crashlytics": "17.1.1",
    "com.google.firebase:firebase-firestore-ktx": "24.2.1",
    "com.google.guava:failureaccess": "1.0.1",
    "com.google.j2objc:j2objc-annotations": "1.3",
    "com.google.protobuf:protobuf-javalite": "3.19.2",
    "com.squareup.moshi:moshi": "1.13.0",
    "com.squareup.moshi:moshi-kotlin-codegen": "1.13.0",
    "com.squareup.okhttp3:okhttp": "4.7.2",
    "com.squareup.retrofit2:converter-moshi": "2.7.2",
    "com.squareup.retrofit2:retrofit": "2.9.0",
    "de.hdodenhof:circleimageview": "3.0.1",
    "io.fabric.sdk.android:fabric": "1.4.7",
    "javax.annotation:javax.annotation-api": "1.3.2",
    "javax.inject:javax.inject": "1",
    "nl.dionsegijn:konfetti": "1.2.5",
    "org.checkerframework:checker-compat-qual": "2.5.5",
    "org.checkerframework:checker-qual": "3.21.3",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8": "1.6.21",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core": "1.6.4",
    "org.jetbrains.kotlinx:kotlinx-coroutines-guava": "1.6.4",
}

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should only contain test-only dependencies. These are dependencies that are guaranteed
# cannot be included in production builds of the app.
TEST_DEPENDENCY_VERSIONS = {
    "androidx.test.espresso:espresso-accessibility": "3.1.0",
    "androidx.test.espresso:espresso-contrib": "3.1.0",
    "androidx.test.espresso:espresso-core": "3.2.0",
    "androidx.test.espresso:espresso-idling-resource": "3.2.0",
    "androidx.test.espresso:espresso-intents": "3.1.0",
    "androidx.test.ext:junit": "1.1.1",
    "androidx.test.ext:truth": "1.4.0",
    "androidx.test.uiautomator:uiautomator": "2.2.0",
    "androidx.test:core": "1.4.0",
    "androidx.test:monitor": "1.4.0",
    "androidx.test:rules": "1.1.0",
    "androidx.test:runner": "1.2.0",
    "androidx.work:work-testing": "2.4.0",
    "com.github.bumptech.glide:mocks": "4.11.0",
    "com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework": "2.0",
    "com.google.truth.extensions:truth-liteproto-extension": "1.1.3",
    "com.google.truth:truth": "1.1.3",
    "com.squareup.okhttp3:mockwebserver": "4.7.2",
    "com.squareup.retrofit2:retrofit-mock": "2.9.0",
    "junit:junit": "4.13.2",
    "org.hamcrest:hamcrest-core": "1.3",
    "org.hamcrest:hamcrest-library": "1.3",
    "org.jetbrains.kotlin:kotlin-reflect": "1.6.21",
    "org.mockito:mockito-core": "3.9.0",
    "org.robolectric:annotations": "4.5",
    "org.robolectric:robolectric": "4.5",
    "org.robolectric:shadowapi": "4.5",
    "org.robolectric:shadows-framework": "4.5",
}
