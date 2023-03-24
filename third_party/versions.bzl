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

# TODO: Make sure these versions align with those used in Gradle (pick latest?).
# TODO: Find a new way to represent versions to avoid duplicating them.
# TODO: Reverify that all deps are needed in their corresponding buckets.
# TODO: Add checks to verify duplication rules between different version buckets, and to ensure transitive deps are correctly accounted for.
# TODO: Maybe have four files, but split as two productions: app & scripts (prod & tests).
# TODO: Maybe have a single list of artifacts & versions (including for HTTP), then reference them in specific lists. Could be enforced. Makes it easier to custom search for versions?

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should contain only production (non-test) dependencies.
_MAVEN_APP_PRODUCTION_DEPENDENCY_VERSIONS = {
    "androidx.annotation:annotation": "1.1.0",
    "androidx.appcompat:appcompat": "1.2.0",
    "androidx.exifinterface:exifinterface": "1.0.0",
    "androidx.constraintlayout:constraintlayout": "2.0.1",
    "androidx.core:core": "1.3.1",
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
    "androidx.recyclerview:recyclerview": "1.1.0",
    "androidx.room:room-runtime": "2.2.5",
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
    "com.google.errorprone:error_prone_annotations": "2.11.0",
    "com.google.firebase:firebase-analytics": "17.5.0",
    "com.google.firebase:firebase-common": "19.3.0",
    "com.google.firebase:firebase-crashlytics": "17.1.1",
    "com.google.gms:google-services": "4.3.3",
    "com.google.guava:failureaccess": "1.0.1",
    "com.google.j2objc:j2objc-annotations": "1.3",
    "com.google.protobuf:protobuf-javalite": "3.17.3",
    "com.squareup.moshi:moshi-kotlin": "1.14.0",
    "com.squareup.moshi:moshi-kotlin-codegen": "1.14.0",
    "com.squareup.okhttp3:okhttp": "4.7.2",
    "com.squareup.retrofit2:converter-moshi": "2.5.0",
    "com.squareup.retrofit2:retrofit": "2.9.0",
    "de.hdodenhof:circleimageview": "3.0.1",
    "io.fabric.sdk.android:fabric": "1.4.7",
    "javax.annotation:javax.annotation-api:jar": "1.3.2",
    "javax.inject:javax.inject": "1",
    "nl.dionsegijn:konfetti": "1.2.5",
    "org.checkerframework:checker-compat-qual": "2.5.5",
    "org.checkerframework:checker-qual": "3.21.3",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8:jar": "1.7.0",
    "org.jetbrains.kotlinx:kotlinx-coroutines-android": "1.4.1",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core": "1.4.1",
    "org.jetbrains:annotations:jar": "13.0",
}

_MAVEN_APP_TRANSITIVE_DEPENDENCY_VERSIONS = {
    "androidx.activity:activity": "1.1.0",
    "androidx.annotation:annotation-experimental": "1.0.0",
    "androidx.appcompat:appcompat-resources": "1.2.0",
    "androidx.arch.core:core-common": "2.1.0",
    "androidx.arch.core:core-runtime": "2.1.0",
    "androidx.cardview:cardview": "1.0.0",
    "androidx.collection:collection": "1.1.0",
    "androidx.constraintlayout:constraintlayout-solver": "2.0.1",
    "androidx.coordinatorlayout:coordinatorlayout": "1.1.0",
    "androidx.cursoradapter:cursoradapter": "1.0.0",
    "androidx.customview:customview": "1.1.0",
    "androidx.databinding:databinding-compiler-common": "3.4.2",
    "androidx.documentfile:documentfile": "1.0.0",
    "androidx.dynamicanimation:dynamicanimation": "1.0.0",
    "androidx.fragment:fragment": "1.2.0",
    "androidx.interpolator:interpolator": "1.0.0",
    "androidx.legacy:legacy-support-core-utils": "1.0.0",
    "androidx.lifecycle:lifecycle-common": "2.2.0",
    "androidx.lifecycle:lifecycle-livedata": "2.2.0",
    "androidx.lifecycle:lifecycle-livedata-core-ktx": "2.2.0",
    "androidx.lifecycle:lifecycle-process": "2.2.0",
    "androidx.lifecycle:lifecycle-runtime": "2.2.0",
    "androidx.lifecycle:lifecycle-service": "2.2.0",
    "androidx.lifecycle:lifecycle-viewmodel": "2.2.0",
    "androidx.lifecycle:lifecycle-viewmodel-savedstate": "1.0.0",
    "androidx.loader:loader": "1.0.0",
    "androidx.localbroadcastmanager:localbroadcastmanager": "1.0.0",
    "androidx.navigation:navigation-common": "2.0.0",
    "androidx.navigation:navigation-common-ktx": "2.0.0",
    "androidx.navigation:navigation-runtime": "2.0.0",
    "androidx.navigation:navigation-runtime-ktx": "2.0.0",
    "androidx.navigation:navigation-ui": "2.0.0",
    "androidx.navigation:navigation-ui-ktx": "2.0.0",
    "androidx.print:print": "1.0.0",
    "androidx.recyclerview:recyclerview": "1.1.0",
    "androidx.room:room-common": "2.2.5",
    "androidx.room:room-runtime": "2.2.5",
    "androidx.savedstate:savedstate": "1.0.0",
    "androidx.sqlite:sqlite": "2.1.0",
    "androidx.sqlite:sqlite-framework": "2.1.0",
    "androidx.transition:transition": "1.2.0",
    "androidx.vectordrawable:vectordrawable": "1.1.0",
    "androidx.vectordrawable:vectordrawable-animated": "1.1.0",
    "androidx.versionedparcelable:versionedparcelable": "1.1.0",
    "androidx.viewpager2:viewpager2": "1.0.0",
    "androidx.viewpager:viewpager": "1.0.0",
    "androidx.work:work-runtime": "2.4.0",
    "androidx.work:work-runtime-ktx": "2.4.0",
    "com.android.databinding:baseLibrary": "3.4.2",
    "com.android.support:support-annotations": "28.0.0",
    "com.android.tools.build.jetifier:jetifier-core": "1.0.0-beta04",
    "com.android.tools:annotations": "26.4.2",
    "com.crashlytics.sdk.android:crashlytics": "2.9.8",
    "com.github.bumptech.glide:annotations": "4.11.0",
    "com.github.bumptech.glide:compiler": "4.11.0",
    "com.github.bumptech.glide:disklrucache": "4.11.0",
    "com.github.bumptech.glide:gifdecoder": "4.11.0",
    "com.github.bumptech.glide:glide": "4.11.0",
    "com.google.android.datatransport:transport-api": "2.2.0",
    "com.google.android.datatransport:transport-backend-cct": "2.3.0",
    "com.google.android.datatransport:transport-runtime": "2.2.3",
    "com.google.android.flexbox:flexbox": "3.0.0",
    "com.google.android.gms:play-services-base": "17.0.0",
    "com.google.android.gms:play-services-basement": "17.0.0",
    "com.google.android.gms:play-services-measurement": "17.5.0",
    "com.google.android.gms:play-services-measurement-api": "17.5.0",
    "com.google.android.gms:play-services-measurement-base": "17.5.0",
    "com.google.android.gms:play-services-measurement-impl": "17.5.0",
    "com.google.android.gms:play-services-measurement-sdk": "17.5.0",
    "com.google.android.gms:play-services-measurement-sdk-api": "17.5.0",
    "com.google.android.gms:strict-version-matcher-plugin": "1.2.1",
    "com.google.android.material:material": "1.3.0",
    "com.google.auto.service:auto-service-annotations": "1.0",
    "com.google.auto.value:auto-value-annotations": "1.6.5",
    "com.google.code.findbugs:jsr305": "3.0.2",
    "com.google.code.gson:gson": "2.8.5",
    "com.google.dagger:dagger": "2.41",
    "com.google.dagger:dagger-compiler": "2.41",
    "com.google.dagger:dagger-producers": "2.41",
    "com.google.dagger:dagger-spi": "2.41",
    "com.google.devtools.ksp:symbol-processing-api": "1.5.30-1.0.0",
    "com.google.errorprone:error_prone_annotations": "2.11.0",
    "com.google.errorprone:javac-shaded": "9-dev-r4023-3",
    "com.google.firebase:firebase-analytics": "17.5.0",
    "com.google.firebase:firebase-common": "19.3.0",
    "com.google.firebase:firebase-components": "16.0.0",
    "com.google.firebase:firebase-crashlytics": "17.1.1",
    "com.google.firebase:firebase-encoders-json": "16.1.0",
    "com.google.firebase:firebase-iid": "20.1.5",
    "com.google.firebase:firebase-installations": "16.3.2",
    "com.google.firebase:firebase-installations-interop": "16.0.0",
    "com.google.gms:google-services": "4.3.3",
    "com.google.googlejavaformat:google-java-format": "1.5",
    "com.google.guava:failureaccess": "1.0.1",
    "com.google.guava:guava": "31.0.1-jre",
    "com.google.guava:listenablefuture": "9999.0-empty-to-avoid-conflict-with-guava",
    "com.google.j2objc:j2objc-annotations": "1.3",
    "com.google.protobuf:protobuf-javalite": "3.17.3",
    "com.googlecode.juniversalchardet:juniversalchardet": "1.0.3",
    "com.squareup.moshi:moshi": "1.14.0",
    "com.squareup.moshi:moshi-kotlin": "1.14.0",
    "com.squareup.moshi:moshi-kotlin-codegen": "1.14.0",
    "com.squareup.okhttp3:okhttp": "4.7.2",
    "com.squareup.okio:okio": "2.10.0",
    "com.squareup.retrofit2:converter-moshi": "2.5.0",
    "com.squareup.retrofit2:retrofit": "2.9.0",
    "com.squareup:javapoet": "1.13.0",
    "com.squareup:kotlinpoet": "1.12.0",
    "com.squareup:kotlinpoet-ksp": "1.12.0",
    "commons-codec:commons-codec": "1.10",
    "commons-io:commons-io": "2.4",
    "de.hdodenhof:circleimageview": "3.0.1",
    "io.fabric.sdk.android:fabric": "1.4.7",
    "javax.annotation:javax.annotation-api": "1.3.2",
    "javax.inject:javax.inject": "1",
    "net.ltgt.gradle.incap:incap": "0.2",
    "nl.dionsegijn:konfetti": "1.2.5",
    "org.antlr:antlr4": "4.5.3",
    "org.checkerframework:checker-compat-qual": "2.5.5",
    "org.checkerframework:checker-qual": "3.21.3",
    "org.jetbrains.kotlin:kotlin-reflect": "1.7.0",
    "org.jetbrains.kotlin:kotlin-stdlib": "1.7.0",
    "org.jetbrains.kotlin:kotlin-stdlib-common": "1.7.0",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk7": "1.7.0",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8": "1.7.0",
    "org.jetbrains.kotlinx:kotlinx-coroutines-android": "1.4.1",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core": "1.4.1",
    "org.jetbrains.kotlinx:kotlinx-metadata-jvm": "0.3.0",
    "org.jetbrains:annotations": "13.0",
    "org.ow2.asm:asm": "9.3",
}

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should only contain test-only dependencies. These are dependencies that are guaranteed
# cannot be included in production builds of the app.
_MAVEN_APP_TEST_DEPENDENCY_VERSIONS = {
    "androidx.arch.core:core-testing": "2.1.0",
    "androidx.test.espresso:espresso-accessibility": "3.1.0",
    "androidx.test.espresso:espresso-contrib": "3.1.0",
    "androidx.test.espresso:espresso-core": "3.2.0",
    "androidx.test.espresso:espresso-intents": "3.1.0",
    "androidx.test.ext:junit": "1.1.1",
    "androidx.test.ext:truth": "1.4.0",
    "androidx.test:core": "1.4.0",
    "androidx.test.uiautomator:uiautomator": "2.2.0",
    "androidx.test:rules": "1.1.0",
    "androidx.test:runner": "1.2.0",
    "androidx.work:work-testing": "2.4.0",
    "com.github.bumptech.glide:mocks": "4.11.0",
    "com.google.truth.extensions:truth-liteproto-extension": "1.1.3",
    "com.google.truth:truth": "1.1.3",
    "com.squareup.okhttp3:mockwebserver": "4.7.2",
    "com.squareup.retrofit2:retrofit-mock": "2.5.0",
    "junit:junit": "4.13.2",
    "org.jetbrains.kotlin:kotlin-reflect": "1.7.0",
    "org.jetbrains.kotlin:kotlin-test-junit": "1.3.72",
    "org.jetbrains.kotlinx:kotlinx-coroutines-test": "1.2.2",
    "org.mockito.kotlin:mockito-kotlin": "3.2.0",
    "org.mockito:mockito-core": "3.9.0",
    "org.robolectric:annotations": "4.5",
    "org.robolectric:robolectric": "4.5",
    "org.robolectric:shadowapi": "4.5",
}

_MAVEN_SCRIPTS_DEPENDENCY_VERSIONS = {
    "com.android.tools.build:aapt2-proto": "7.3.1-8691043",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8:jar": "1.7.0",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core": "1.4.1",
    "org.jetbrains.kotlin:kotlin-compiler-embeddable": "1.5.0",
    "com.google.protobuf:protobuf-java": "3.17.3",
    "com.squareup.moshi:moshi-kotlin": "1.14.0",
    "com.squareup.moshi:moshi-kotlin-codegen": "1.14.0",
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
    "protobuf_tools": {
        "version": "3.11.0",
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
        "sha": "1d872b9c6546f0f737a356d873b164d70282760fe4c880349770abc9e494c9ce",
        "version": "v1.7.2",
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

# TODO: Maybe have tests as a separate build context, instead?
# TODO: Document transitive_deps.
MAVEN_ARTIFACT_TREES = {
    "app_prod": {
        "deps": _MAVEN_APP_PRODUCTION_DEPENDENCY_VERSIONS,
        "transitive_deps": _MAVEN_APP_TRANSITIVE_DEPENDENCY_VERSIONS,
        "include_dagger_artifacts": True,
        "test_only": False,
        "target_overrides": {
            "com.google.guava:guava": "@//third_party/app_prod:com_google_guava_guava",
        },
        "repositories": MAVEN_REPOSITORIES,
    },
    "app_test": {
        "deps": _MAVEN_APP_TEST_DEPENDENCY_VERSIONS,
        "transitive_deps": _MAVEN_APP_TRANSITIVE_DEPENDENCY_VERSIONS,
        "include_dagger_artifacts": False,
        "test_only": True,
        "target_overrides": {},
        "repositories": MAVEN_REPOSITORIES,
    },
    "scripts": {
        "deps": _MAVEN_SCRIPTS_DEPENDENCY_VERSIONS,
        "transitive_deps": _MAVEN_APP_TRANSITIVE_DEPENDENCY_VERSIONS,
        "include_dagger_artifacts": False,
        "test_only": False,
        "target_overrides": {},
        "repositories": MAVEN_REPOSITORIES,
    },
}

def extract_maven_dependencies(maven, parse, artifact_tree, dagger_artifacts):
    """
    Returns a list of Maven dependency artifacts to install to fulfill all third-party dependencies.
    """
    is_test_only = artifact_tree["test_only"]
    main_artifacts = (
        _create_maven_deps(maven, parse, artifact_tree["deps"], is_test_only) +
        _create_maven_deps(maven, parse, artifact_tree["transitive_deps"], is_test_only)
    )
    additional_artifacts = dagger_artifacts if artifact_tree["include_dagger_artifacts"] else []
    return main_artifacts + additional_artifacts

def _create_maven_deps(maven, parse, dependency_versions, test_only):
    """
    Returns a list of Maven dependency artifacts to install to fulfill specific third-party
    dependencies.
    """
    return [
        _create_maven_artifact(maven, parse, name, version, test_only)
        for name, version in dependency_versions.items()
    ]

def _create_maven_artifact(maven, parse, name, version, test_only):
    # Create production & test specific dependencies per:
    # https://github.com/bazelbuild/rules_jvm_external#test-only-dependencies.
    coordinate = parse.parse_maven_coordinate("%s:%s" % (name, version))
    return maven.artifact(
        coordinate["group"],
        coordinate["artifact"],
        coordinate["version"],
        packaging = coordinate.get("packaging"),
        #        testonly = test_only, # TODO: fix (per https://github.com/bazelbuild/rules_jvm_external/issues/350)
    )
