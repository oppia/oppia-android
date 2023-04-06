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

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should contain only production (non-test) dependencies. Note also that Dagger artifacts
# are manually included here for better integration with version list maintenance despite this being
# contrary to Dagger's suggested Bazel setup instructions.
_MAVEN_PRODUCTION_DEPENDENCY_VERSIONS = {
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
    "androidx.multidex:multidex": "2.0.1",
    "androidx.navigation:navigation-fragment": "2.0.0",
    "androidx.navigation:navigation-ui": "2.0.0",
    "androidx.recyclerview:recyclerview": "1.1.0",
    "androidx.viewpager2:viewpager2": "1.0.0",
    "androidx.viewpager:viewpager": "1.0.0",
    "androidx.work:work-runtime": "2.4.0",
    "androidx.work:work-runtime-ktx": "2.4.0",
    "com.github.bumptech.glide:compiler": "4.11.0",
    "com.github.bumptech.glide:glide": "4.11.0",
    "com.google.android.flexbox:flexbox": "3.0.0",
    "com.google.android.material:material": "1.3.0",
    "com.google.dagger:dagger": "2.41",
    "com.google.dagger:dagger-compiler": "2.41",
    "com.google.dagger:dagger-producers": "2.41",
    "com.google.dagger:dagger-spi": "2.41",
    "com.google.errorprone:error_prone_annotations": "2.11.0",
    "com.google.firebase:firebase-analytics": "17.5.0",
    "com.google.firebase:firebase-common": "19.3.0",
    "com.google.firebase:firebase-crashlytics": "17.1.1",
    "com.google.guava:failureaccess": "1.0.1",
    "com.google.j2objc:j2objc-annotations": "1.3",
    "com.google.protobuf:protobuf-javalite": "3.17.3",
    "com.squareup.moshi:moshi-kotlin": "1.13.0",
    "com.squareup.moshi:moshi-kotlin-codegen": "1.13.0",
    "com.squareup.okhttp3:okhttp": "4.7.2",
    "com.squareup.retrofit2:converter-moshi": "2.5.0",
    "com.squareup.retrofit2:retrofit": "2.9.0",
    "de.hdodenhof:circleimageview": "3.0.1",
    "io.fabric.sdk.android:fabric": "1.4.7",
    "javax.annotation:javax.annotation-api": "1.3.2",
    "javax.inject:javax.inject": "1",
    "nl.dionsegijn:konfetti": "1.2.5",
    "org.checkerframework:checker-compat-qual": "2.5.5",
    "org.checkerframework:checker-qual": "3.21.3",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8": "1.6.21",
    "org.jetbrains.kotlinx:kotlinx-coroutines-android": "1.6.4",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core": "1.6.4",
    "org.jetbrains.kotlinx:kotlinx-coroutines-guava": "1.6.4",
}

_MAVEN_PRODUCTION_TRANSITIVE_DEPENDENCY_VERSIONS = {
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
    "androidx.navigation:navigation-runtime": "2.0.0",
    "androidx.print:print": "1.0.0",
    "androidx.room:room-common": "2.2.5",
    "androidx.room:room-runtime": "2.2.5",
    "androidx.savedstate:savedstate": "1.0.0",
    "androidx.sqlite:sqlite": "2.1.0",
    "androidx.sqlite:sqlite-framework": "2.1.0",
    "androidx.transition:transition": "1.2.0",
    "androidx.vectordrawable:vectordrawable": "1.1.0",
    "androidx.vectordrawable:vectordrawable-animated": "1.1.0",
    "androidx.versionedparcelable:versionedparcelable": "1.1.0",
    "com.android.databinding:baseLibrary": "3.4.2",
    "com.android.tools.build.jetifier:jetifier-core": "1.0.0-beta04",
    "com.android.tools:annotations": "26.4.2",
    "com.github.bumptech.glide:annotations": "4.11.0",
    "com.github.bumptech.glide:disklrucache": "4.11.0",
    "com.github.bumptech.glide:gifdecoder": "4.11.0",
    "com.google.android.datatransport:transport-api": "2.2.0",
    "com.google.android.datatransport:transport-backend-cct": "2.3.0",
    "com.google.android.datatransport:transport-runtime": "2.2.3",
    "com.google.android.gms:play-services-ads-identifier": "17.0.0",
    "com.google.android.gms:play-services-base": "17.0.0",
    "com.google.android.gms:play-services-basement": "17.0.0",
    "com.google.android.gms:play-services-measurement": "17.5.0",
    "com.google.android.gms:play-services-measurement-api": "17.5.0",
    "com.google.android.gms:play-services-measurement-base": "17.5.0",
    "com.google.android.gms:play-services-measurement-impl": "17.5.0",
    "com.google.android.gms:play-services-measurement-sdk": "17.5.0",
    "com.google.android.gms:play-services-measurement-sdk-api": "17.5.0",
    "com.google.android.gms:play-services-stats": "17.0.0",
    "com.google.android.gms:play-services-tasks": "17.0.0",
    "com.google.auto.service:auto-service-annotations": "1.0",
    "com.google.auto.value:auto-value-annotations": "1.8.1",
    "com.google.code.findbugs:jsr305": "3.0.2",
    "com.google.code.gson:gson": "2.8.0",
    "com.google.devtools.ksp:symbol-processing-api": "1.5.30-1.0.0",
    "com.google.errorprone:javac-shaded": "9-dev-r4023-3",
    "com.google.firebase:firebase-components": "16.0.0",
    "com.google.firebase:firebase-encoders-json": "16.1.0",
    "com.google.firebase:firebase-iid": "20.1.5",
    "com.google.firebase:firebase-iid-interop": "17.0.0",
    "com.google.firebase:firebase-installations": "16.3.2",
    "com.google.firebase:firebase-installations-interop": "16.0.0",
    "com.google.firebase:firebase-measurement-connector": "18.0.0",
    "com.google.googlejavaformat:google-java-format": "1.5",
    "com.google.guava:guava": "31.0.1-jre",
    "com.google.guava:listenablefuture": "9999.0-empty-to-avoid-conflict-with-guava",
    "com.googlecode.juniversalchardet:juniversalchardet": "1.0.3",
    "com.squareup.moshi:moshi": "1.13.0",
    "com.squareup.okio:okio": "2.10.0",
    "com.squareup:javapoet": "1.13.0",
    "com.squareup:kotlinpoet": "1.10.2",
    "commons-codec:commons-codec": "1.10",
    "commons-io:commons-io": "2.4",
    "net.ltgt.gradle.incap:incap": "0.2",
    "org.antlr:antlr4": "4.5.3",
    "org.jetbrains.kotlin:kotlin-reflect": "1.6.0",
    "org.jetbrains.kotlin:kotlin-stdlib": "1.6.21",
    "org.jetbrains.kotlin:kotlin-stdlib-common": "1.6.21",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk7": "1.6.21",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm": "1.6.4",
    "org.jetbrains.kotlinx:kotlinx-metadata-jvm": "0.3.0",
    "org.jetbrains:annotations": "13.0",
    "org.ow2.asm:asm": "9.2",
}

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should only contain test-only dependencies. These are dependencies that are guaranteed
# cannot be included in production builds of the app.
_MAVEN_TEST_DEPENDENCY_VERSIONS = {
    "androidx.arch.core:core-testing": "2.1.0",
    "androidx.test.espresso:espresso-accessibility": "3.1.0",
    "androidx.test.espresso:espresso-contrib": "3.1.0",
    "androidx.test.espresso:espresso-core": "3.2.0",
    "androidx.test.espresso:espresso-intents": "3.1.0",
    "androidx.test.ext:junit": "1.1.1",
    "androidx.test.ext:truth": "1.4.0",
    "androidx.test:core": "1.4.0",
    "androidx.test:runner": "1.2.0",
    "androidx.work:work-testing": "2.4.0",
    "com.github.bumptech.glide:mocks": "4.11.0",
    "com.google.truth.extensions:truth-liteproto-extension": "1.1.3",
    "com.google.truth:truth": "1.1.3",
    "com.squareup.okhttp3:mockwebserver": "4.7.2",
    "com.squareup.retrofit2:retrofit-mock": "2.5.0",
    "junit:junit": "4.13.2",
    "org.jetbrains.kotlin:kotlin-reflect": "1.6.0",
    "org.jetbrains.kotlin:kotlin-test-junit": "1.3.72",
    "org.jetbrains.kotlinx:kotlinx-coroutines-test": "1.6.4",
    "org.mockito:mockito-core": "3.9.0",
    "org.robolectric:annotations": "4.5",
    "org.robolectric:robolectric": "4.5",
}

_MAVEN_TEST_TRANSITIVE_DEPENDENCY_VERSIONS = {
    "androidx.test.espresso:espresso-idling-resource": "3.2.0",
    "androidx.test:monitor": "1.4.0",
    "androidx.test:rules": "1.1.0",
    "com.almworks.sqlite4java:sqlite4java": "1.0.392",
    "com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework": "2.0",
    "com.ibm.icu:icu4j": "53.1",
    "com.squareup:javawriter": "2.1.1",
    "net.bytebuddy:byte-buddy": "1.10.20",
    "net.bytebuddy:byte-buddy-agent": "1.10.20",
    "net.sf.kxml:kxml2": "2.3.0",
    "org.bouncycastle:bcprov-jdk15on": "1.65",
    "org.hamcrest:hamcrest-core": "1.3",
    "org.hamcrest:hamcrest-integration": "1.3",
    "org.hamcrest:hamcrest-library": "1.3",
    "org.jetbrains.kotlin:kotlin-test": "1.3.72",
    "org.jetbrains.kotlin:kotlin-test-annotations-common": "1.3.72",
    "org.jetbrains.kotlin:kotlin-test-common": "1.3.72",
    "org.jetbrains.kotlinx:kotlinx-coroutines-test-jvm": "1.6.4",
    "org.objenesis:objenesis": "3.2",
    "org.ow2.asm:asm-analysis": "9.0",
    "org.ow2.asm:asm-commons": "9.0",
    "org.ow2.asm:asm-tree": "9.0",
    "org.ow2.asm:asm-util": "9.0",
    "org.robolectric:junit": "4.5",
    "org.robolectric:pluginapi": "4.5",
    "org.robolectric:plugins-maven-dependency-resolver": "4.5",
    "org.robolectric:resources": "4.5",
    "org.robolectric:sandbox": "4.5",
    "org.robolectric:shadowapi": "4.5",
    "org.robolectric:shadows-framework": "4.5",
    "org.robolectric:utils": "4.5",
    "org.robolectric:utils-reflector": "4.5",
}

_MAVEN_SCRIPTS_PRODUCTION_DEPENDENCY_VERSIONS = {
    "com.android.tools.apkparser:apkanalyzer": "30.0.4",
    "com.android.tools.build:aapt2-proto": "7.3.1-8691043",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8": "1.6.21",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core": "1.4.1",
    "org.jetbrains.kotlin:kotlin-compiler-embeddable": "1.5.0",
    "com.google.errorprone:error_prone_annotations": "2.11.0",
    "com.google.guava:failureaccess": "1.0.1",
    "com.google.j2objc:j2objc-annotations": "1.3",
    "com.google.protobuf:protobuf-java": "3.17.3",
    "com.squareup.moshi:moshi-kotlin": "1.13.0",
    "com.squareup.moshi:moshi-kotlin-codegen": "1.13.0",
    "org.checkerframework:checker-compat-qual": "2.5.5",
    "org.checkerframework:checker-qual": "3.21.3",
}

_MAVEN_SCRIPTS_TEST_DEPENDENCY_VERSIONS = {
    "com.google.truth.extensions:truth-liteproto-extension": "1.1.3",
    "com.google.truth:truth": "1.1.3",
    "junit:junit": "4.13.2",
    "org.jetbrains.kotlin:kotlin-test-junit": "1.3.72",
    "org.mockito.kotlin:mockito-kotlin": "3.2.0",
    "org.mockito:mockito-core": "3.9.0",
}

MAVEN_REPOSITORIES = [
    "https://maven.fabric.io/public",
    "https://maven.google.com",
    "https://repo1.maven.org/maven2",
]

# Contains dependencies to automatically download via Bazel's http_archive. Note that keys in this
# dict will be made available via their own workspace, e.g. 'dagger' would be available via
# '@dagger//'. Note also that URLs and strip_prefix values may include "{0}" to be replaced with the
# dependency's version when preparing to the dependency for downloading. strip_prefix is optional.
# An additional 'import_bind_name' may be used to specify an import name, otherwise the dictionary
# key will be used. Note to developers: Please keep this dict sorted by key to make it easier to
# find dependencies.
HTTP_ARCHIVE_DEPENDENCY_VERSIONS = {
    "android_test_support": {
        "sha": "dcd1ff76aef1a26329d77863972780c8fe1fc8ff625747342239f0489c2837ec",
        "strip_prefix": "android-test-{0}",
        "urls": ["https://github.com/android/android-test/archive/{0}.tar.gz"],
        "version": "1edfdab3134a7f01b37afabd3eebfd2c5bb05151",
    },
    "bazel_skylib": {
        "sha": "b8a1527901774180afc798aeb28c4634bdccf19c4d98e7bdd1ce79d1fe9aaad7",
        "urls": [
            "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/{0}/bazel-skylib-{0}.tar.gz",
            "https://github.com/bazelbuild/bazel-skylib/releases/download/{0}/bazel-skylib-{0}.tar.gz",
        ],
        "version": "1.4.1",
    },
    "dagger": {
        "sha": "5c2b22e88e52110178afebda100755f31f5dd505c317be0bfb4f7ad88a88db86",
        "strip_prefix": "dagger-dagger-{0}",
        "urls": ["https://github.com/google/dagger/archive/dagger-{0}.zip"],
        "version": _MAVEN_PRODUCTION_DEPENDENCY_VERSIONS["com.google.dagger:dagger"],
    },
    "protobuf_tools": {
        "sha": "efcb0b9004200fce79de23be796072a055105273905a5a441dbb5a979d724d20",
        "strip_prefix": "protobuf-{0}",
        "urls": ["https://github.com/protocolbuffers/protobuf/releases/download/v{0}/protobuf-all-{0}.zip"],
        "version": "3.11.0",
    },
    "robolectric": {
        "sha": "af0177d32ecd2cd68ee6e9f5d38288e1c4de0dd2a756bb7133c243f2d5fe06f7",
        "strip_prefix": "robolectric-bazel-{0}",
        "urls": ["https://github.com/robolectric/robolectric-bazel/archive/{0}.tar.gz"],
        "version": "4.5",
    },
    "rules_java": {
        "sha": "c73336802d0b4882e40770666ad055212df4ea62cfa6edf9cb0f9d29828a0934",
        "urls": ["https://github.com/bazelbuild/rules_java/releases/download/{0}/rules_java-{0}.tar.gz"],
        "version": "5.3.5",
    },
    "rules_jvm": {
        "import_bind_name": "rules_jvm_external",
        "sha": "c4cd0fd413b43785494b986fdfeec5bb47eddca196af5a2a98061faab83ed7b2",
        "strip_prefix": "rules_jvm_external-{0}",
        "urls": ["https://github.com/bazelbuild/rules_jvm_external/archive/{0}.zip"],
        "version": "5.1",
    },
    # TODO: Move this back to rules_kotlin once it has a 1.7.x release with the needed fix.
    "rules_kotlin": {
        "import_bind_name": "io_bazel_rules_kotlin",
        "sha": "1d872b9c6546f0f737a356d873b164d70282760fe4c880349770abc9e494c9ce",
        "urls": ["https://github.com/oppia/rules_kotlin/releases/download/{0}/rules_kotlin_release.tgz"],
        "version": "v1.7.2",
    },
    "rules_proto": {
        "sha": "e0cab008a9cdc2400a1d6572167bf9c5afc72e19ee2b862d18581051efab42c9",
        "strip_prefix": "rules_proto-{0}",
        "urls": ["https://github.com/bazelbuild/rules_proto/archive/{0}.tar.gz"],
        "version": "c0b62f2f46c85c16cb3b5e9e921f0d00e3101934",
    },
}

# Similar to HTTP_ARCHIVE_DEPENDENCY_VERSIONS except these dependencies are imported using http_jar.
# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
HTTP_JAR_DEPENDENCY_VERSIONS = {
    "android_bundletool": {
        "sha": "1e8430002c76f36ce2ddbac8aadfaf2a252a5ffbd534dab64bb255cda63db7ba",
        "urls": ["https://github.com/google/bundletool/releases/download/{0}/bundletool-all-{0}.jar"],
        "version": "1.8.0",
    },
    "guava_android": {
        "sha": "9425a423a4cb9d9db0356300722d9bd8e634cf539f29d97bb84f457cccd16eb8",
        "urls": [
            "%s/com/google/guava/guava/android-{0}/guava-android-{0}.jar" % url_base
            for url_base in MAVEN_REPOSITORIES
        ],
        "version": "31.0.1",
    },
    "guava_jre": {
        "sha": "d5be94d65e87bd219fb3193ad1517baa55a3b88fc91d21cf735826ab5af087b9",
        "urls": [
            "%s/com/google/guava/guava/jre-{0}/guava-jre-{0}.jar" % url_base
            for url_base in MAVEN_REPOSITORIES
        ],
        "version": "31.0.1",
    },
    "kotlinx-coroutines-core-jvm": {
        "sha": "acc8c74b1fb88121c51221bfa7b6f5e920201bc20183ebf74165dcf5d45a8003",
        "urls": [
            "%s/org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/{0}/kotlinx-coroutines-core-jvm-{0}.jar" % url_base
            for url_base in MAVEN_REPOSITORIES
        ],
        "version": "1.6.0",
    },
}

# Similar to HTTP_ARCHIVE_DEPENDENCY_VERSIONS except these dependencies are imported using
# git_repository. Entries may contain an optional repo_mapping which will be passed to Bazel's
# git_repository macro. Note to developers: Please keep this dict sorted by key to make it easier to
# find dependencies.
GIT_REPOSITORY_DEPENDENCY_VERSIONS = {
    "android-spotlight": {
        "commit": "cc23499d37dc8533a2876e45b5063e981a4583f4",
        "remote": "https://github.com/oppia/android-spotlight",
        "repo_mapping": {"@maven": "@maven_app"},
        "shallow_since": "1680147372 -0700",
    },
    "androidsvg": {
        "commit": "4bc1d26412f0fb9fd4ef263fa93f6a64f4d4dbcf",
        "remote": "https://github.com/oppia/androidsvg",
        "shallow_since": "1647295507 -0700",
    },
    "archive_patcher": {
        "commit": "d1c18b0035d5f669ddaefadade49cae0748f9df2",
        "remote": "https://github.com/oppia/archive-patcher",
        "shallow_since": "1642022460 -0800",
    },
    "circularimageview": {
        "commit": "35d08ba88a4a22e6e9ac96bdc5a68be27b55d09f",
        "remote": "https://github.com/oppia/CircularImageview",
        "shallow_since": "1622148929 -0700",
    },
    "kotlitex": {
        "commit": "ccdf4170817fa3b48b8e1e452772dd58ecb71cf2",
        "remote": "https://github.com/oppia/kotlitex",
        "repo_mapping": {"@maven": "@maven_app"},
        "shallow_since": "1679426649 -0700",
    },
    "tools_android": {
        "commit": "00e6f4b7bdd75911e33c618a9bc57bab7a6e8930",
        "remote": "https://github.com/bazelbuild/tools_android",
        "shallow_since": "1594238320 -0400",
    },
}

# TODO: Maybe have tests as a separate build context, instead?
# TODO: Document transitive_deps.
MAVEN_ARTIFACT_TREES = {
    "app": {
        "deps": {
            "prod": {
                "direct": _MAVEN_PRODUCTION_DEPENDENCY_VERSIONS,
                "transitive": _MAVEN_PRODUCTION_TRANSITIVE_DEPENDENCY_VERSIONS,
            },
            "test": {
                "direct": _MAVEN_TEST_DEPENDENCY_VERSIONS,
                "transitive": _MAVEN_TEST_TRANSITIVE_DEPENDENCY_VERSIONS,
            },
        },
        "maven_install_json": "//third_party:maven_install.json",
        "target_overrides": {
            "com.google.guava:guava": "@//third_party:com_google_guava_guava",
            "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm": "@//third_party:kotlinx-coroutines-core-jvm",
        },
    },
    "scripts": {
        "deps": {
            "prod": {
                "direct": _MAVEN_SCRIPTS_PRODUCTION_DEPENDENCY_VERSIONS,
                "transitive": {},
            },
            "test": {
                "direct": _MAVEN_SCRIPTS_TEST_DEPENDENCY_VERSIONS,
                "transitive": {},
            },
        },
        "maven_install_json": "//scripts/third_party:maven_install.json",
    },
}

def install_maven_dependencies(maven, maven_install, parse, build_context):
    """
    Downloads all Maven dependencies corresponding to the specified build context.
    """
    artifact_tree = MAVEN_ARTIFACT_TREES[build_context]
    maven_install(
        name = "maven_%s" % build_context,
        artifacts = _extract_maven_dependencies(maven, parse, artifact_tree),
        duplicate_version_warning = "error",
        fail_if_repin_required = True,
        maven_install_json = artifact_tree["maven_install_json"],
        override_targets = artifact_tree.get("target_overrides") or {},
        repositories = MAVEN_REPOSITORIES,
        strict_visibility = True,
    )

def _extract_maven_dependencies(maven, parse, artifact_tree):
    """
    Returns a list of Maven dependency artifacts to install to fulfill all third-party dependencies.
    """
    return (
        _create_all_maven_deps(maven, parse, artifact_tree["deps"]["prod"], test_only = False) +
        _create_all_maven_deps(maven, parse, artifact_tree["deps"]["test"], test_only = True)
    )

def _create_all_maven_deps(maven, parse, deps_metadata, test_only):
    """
    Returns a list of Maven dependency artifacts to install to fulfill specific third-party
    dependencies.
    """
    return (
        _create_maven_deps(maven, parse, deps_metadata["direct"], test_only) +
        _create_maven_deps(maven, parse, deps_metadata["transitive"], test_only)
    )

def _create_maven_deps(maven, parse, dependency_versions, test_only):
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
