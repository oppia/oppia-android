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
    "androidx.activity:activity-compose": "1.4.0",
    "androidx.annotation:annotation": "1.1.0",
    "androidx.appcompat:appcompat": "1.3.1",
    "androidx.compose.compiler:compiler": "1.1.1",
    "androidx.compose.foundation:foundation": "1.1.1",
    "androidx.compose.foundation:foundation-layout": "1.1.1",
    "androidx.compose.material:material": "1.1.1",
    "androidx.compose.runtime:runtime": "1.1.1",
    "androidx.compose.ui:ui": "1.1.1",
    "androidx.constraintlayout:constraintlayout": "1.1.3",
    "androidx.core:core": "1.0.1",
    "androidx.core:core-ktx": "1.0.1",
    "androidx.databinding:databinding-adapters": "3.4.2",
    "androidx.databinding:databinding-common": "3.4.2",
    "androidx.databinding:databinding-compiler": "3.4.2",
    "androidx.databinding:databinding-runtime": "3.4.2",
    "androidx.drawerlayout:drawerlayout": "1.1.0",
    "androidx.fragment:fragment": "1.1.0",
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
    "com.android.tools.build:aapt2-proto": "7.3.1-8691043",
    "com.crashlytics.sdk.android:crashlytics": "2.9.8",
    "com.github.bumptech.glide:compiler": "4.11.0",
    "com.github.bumptech.glide:glide": "4.11.0",
    "com.google.android.flexbox:flexbox": "3.0.0",
    "com.google.android.material:material": "1.3.0",
    "com.google.auto.value:auto-value-annotations": "1.8.1",
    "com.google.errorprone:error_prone_annotations": "2.11.0",
    "com.google.firebase:firebase-analytics": "17.5.0",
    "com.google.firebase:firebase-appcheck": "16.0.0",
    "com.google.firebase:firebase-appcheck-debug": "16.0.0",
    "com.google.firebase:firebase-appcheck-playintegrity": "16.0.0",
    "com.google.firebase:firebase-auth-ktx": "19.3.1",
    "com.google.firebase:firebase-common": "19.3.0",
    "com.google.firebase:firebase-crashlytics": "17.1.1",
    "com.google.firebase:firebase-firestore-ktx": "24.2.1",
    "com.google.gms:google-services": "4.3.3",
    "com.google.guava:failureaccess": "1.0.1",
    "com.google.j2objc:j2objc-annotations": "1.3",
    "com.google.protobuf:protobuf-javalite": "3.17.3",
    "com.squareup.moshi:moshi-kotlin": "1.13.0",
    "com.squareup.moshi:moshi-kotlin-codegen": "1.13.0",
    "com.squareup.okhttp3:okhttp": "4.7.2",
    "com.squareup.retrofit2:converter-moshi": "2.5.0",
    "com.squareup.retrofit2:retrofit": "2.9.0",
    "io.fabric.sdk.android:fabric": "1.4.7",
    "javax.annotation:javax.annotation-api:jar": "1.3.2",
    "javax.inject:javax.inject": "1",
    "nl.dionsegijn:konfetti": "1.2.5",
    "org.checkerframework:checker-compat-qual": "2.5.5",
    "org.checkerframework:checker-qual": "3.21.3",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8:jar": "1.6.10",
    "org.jetbrains.kotlinx:kotlinx-coroutines-android": "1.6.4",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core": "1.6.4",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm": "1.6.4",
    "org.jetbrains.kotlinx:kotlinx-coroutines-guava": "1.6.4",
    "org.jetbrains:annotations:jar": "13.0",
}

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should only contain test-only dependencies. These are dependencies that are guaranteed
# cannot be included in production builds of the app.
MAVEN_TEST_DEPENDENCY_VERSIONS = {
    "androidx.arch.core:core-testing": "2.1.0",
    "androidx.compose.ui:ui-test-junit4": "1.1.1",
    "androidx.test.espresso:espresso-accessibility": "3.1.0",
    "androidx.test.espresso:espresso-contrib": "3.1.0",
    "androidx.test.espresso:espresso-core": "3.2.0",
    "androidx.test.espresso:espresso-intents": "3.1.0",
    "androidx.test.ext:junit": "1.1.1",
    "androidx.test.ext:truth": "1.4.0",
    "androidx.test:core": "1.0.0",
    "androidx.test:runner": "1.2.0",
    "androidx.work:work-testing": "2.4.0",
    "com.android.tools.apkparser:apkanalyzer": "30.0.4",
    "com.github.bumptech.glide:mocks": "4.11.0",
    "com.google.protobuf:protobuf-java": "3.17.3",
    "com.google.protobuf:protobuf-java-util": "3.17.3",
    "com.google.truth.extensions:truth-liteproto-extension": "1.1.3",
    "com.google.truth:truth": "0.43",
    "com.squareup.okhttp3:mockwebserver": "4.7.2",
    "com.squareup.retrofit2:retrofit-mock": "2.5.0",
    "io.xlate:yaml-json": "0.1.0",
    "jakarta.json:jakarta.json-api": "2.1.2",
    "junit:junit": "4.12",
    "org.eclipse.parsson:parsson": "1.1.2",
    "org.jetbrains.kotlin:kotlin-compiler-embeddable": "1.5.0",
    "org.jetbrains.kotlin:kotlin-reflect": "1.3.41",
    "org.jetbrains.kotlin:kotlin-test-junit": "1.3.72",
    "org.jetbrains.kotlinx:kotlinx-coroutines-test": "1.6.4",
    "org.mockito.kotlin:mockito-kotlin": "3.2.0",
    "org.mockito:mockito-core": "2.19.0",
    "org.robolectric:annotations": "4.5",
    "org.robolectric:robolectric": "4.5",
    "org.snakeyaml:snakeyaml-engine": "2.6",
}

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
HTTP_DEPENDENCY_VERSIONS = {
    "android_bundletool": {
        "sha": "1e8430002c76f36ce2ddbac8aadfaf2a252a5ffbd534dab64bb255cda63db7ba",
        "version": "1.8.0",
    },
    "dagger": {
        "sha": "5c2b22e88e52110178afebda100755f31f5dd505c317be0bfb4f7ad88a88db86",
        "version": "2.41",
    },
    "guava_android": {
        "sha": "9425a423a4cb9d9db0356300722d9bd8e634cf539f29d97bb84f457cccd16eb8",
        "version": "31.0.1",
    },
    "guava_jre": {
        "sha": "d5be94d65e87bd219fb3193ad1517baa55a3b88fc91d21cf735826ab5af087b9",
        "version": "31.0.1",
    },
    "oppia_proto_api": {
        "version": "9cf993ea0b798a67b3faa21c690c30b9027fb371",
    },
    "protobuf_tools": {
        "sha": "efcb0b9004200fce79de23be796072a055105273905a5a441dbb5a979d724d20",
        "version": "3.11.0",
    },
    "robolectric": {
        "sha": "af0177d32ecd2cd68ee6e9f5d38288e1c4de0dd2a756bb7133c243f2d5fe06f7",
        "version": "4.5",
    },
    "rules_java": {
        "sha": "c73336802d0b4882e40770666ad055212df4ea62cfa6edf9cb0f9d29828a0934",
        "version": "5.3.5",
    },
    "rules_jvm": {
        "sha": "c4cd0fd413b43785494b986fdfeec5bb47eddca196af5a2a98061faab83ed7b2",
        "version": "5.1",
    },
    "rules_kotlin": {
        "sha": "fd92a98bd8a8f0e1cdcb490b93f5acef1f1727ed992571232d33de42395ca9b3",
        "version": "v1.7.1",
    },
    "rules_proto": {
        "sha": "e0cab008a9cdc2400a1d6572167bf9c5afc72e19ee2b862d18581051efab42c9",
        "version": "c0b62f2f46c85c16cb3b5e9e921f0d00e3101934",
    },
}

MAVEN_REPOSITORIES = [
    "https://maven.fabric.io/public",
    "https://maven.google.com",
    "https://repo1.maven.org/maven2",
]

def get_maven_dependencies():
    """
    Returns a list of maven dependencies to install to fulfill third-party dependencies.
    """
    return (["%s:%s" % (name, version) for name, version in MAVEN_PRODUCTION_DEPENDENCY_VERSIONS.items()] +
            ["%s:%s" % (name, version) for name, version in MAVEN_TEST_DEPENDENCY_VERSIONS.items()])
