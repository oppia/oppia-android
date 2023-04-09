"""
Contains all of the HTTP archive/jar & Git repository dependencies that are directly required for
production & test builds of the app. These are exposed via DIRECT_REMOTE_DEPENDENCIES.
"""

load(
    "//third_party/macros:direct_dep_defs.bzl",
    "EXPORT_TOOLCHAIN",
    "create_export_library_details",
    "create_git_repository_reference",
    "create_http_archive_reference",
    "create_http_jar_reference",
)
load(":direct_maven_versions.bzl", "PRODUCTION_DEPENDENCY_VERSIONS")

# External dependencies that are directly imported rather than made accessible through Maven. See
# the individual create*reference() functions for more details on available properties and
# configurations. Note that the order of the references in this list mostly matter (that is, they
# can be configured to reference earlier references, and references are evaluated in-order.
DIRECT_REMOTE_DEPENDENCIES = [
    create_http_archive_reference(
        name = "android_test_support",
        sha = "dcd1ff76aef1a26329d77863972780c8fe1fc8ff625747342239f0489c2837ec",
        version = "1edfdab3134a7f01b37afabd3eebfd2c5bb05151",
        test_only = True,
        url = "https://github.com/android/android-test/archive/{0}.tar.gz",
        strip_prefix_template = "android-test-{0}",
    ),
    create_http_archive_reference(
        name = "bazel_skylib",
        sha = "b8a1527901774180afc798aeb28c4634bdccf19c4d98e7bdd1ce79d1fe9aaad7",
        version = "1.4.1",
        test_only = False,
        urls = [
            "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/{0}/bazel-skylib-{0}.tar.gz",
            "https://github.com/bazelbuild/bazel-skylib/releases/download/{0}/bazel-skylib-{0}.tar.gz",
        ],
    ),
    create_http_archive_reference(
        name = "dagger",
        sha = "5c2b22e88e52110178afebda100755f31f5dd505c317be0bfb4f7ad88a88db86",
        version = PRODUCTION_DEPENDENCY_VERSIONS["com.google.dagger:dagger"],
        test_only = False,
        url = "https://github.com/google/dagger/archive/dagger-{0}.zip",
        strip_prefix_template = "dagger-dagger-{0}",
    ),
    create_http_archive_reference(
        name = "protobuf_tools",
        sha = "efcb0b9004200fce79de23be796072a055105273905a5a441dbb5a979d724d20",
        version = "3.11.0",
        test_only = False,
        url = "https://github.com/protocolbuffers/protobuf/releases/download/v{0}/protobuf-all-{0}.zip",
        strip_prefix_template = "protobuf-{0}",
    ),
    create_http_archive_reference(
        name = "robolectric",
        sha = "af0177d32ecd2cd68ee6e9f5d38288e1c4de0dd2a756bb7133c243f2d5fe06f7",
        version = "4.5",
        test_only = True,
        url = "https://github.com/robolectric/robolectric-bazel/archive/{0}.tar.gz",
        strip_prefix_template = "robolectric-bazel-{0}",
        export_details = create_export_library_details(
            exposed_artifact_name = "robolectric_android-all",
            exportable_target = "bazel:android-all",
            export_toolchain = EXPORT_TOOLCHAIN.ANDROID,
        ),
    ),
    create_http_archive_reference(
        name = "rules_java",
        sha = "c73336802d0b4882e40770666ad055212df4ea62cfa6edf9cb0f9d29828a0934",
        version = "5.3.5",
        test_only = False,
        url = "https://github.com/bazelbuild/rules_java/releases/download/{0}/rules_java-{0}.tar.gz",
    ),
    create_http_archive_reference(
        name = "rules_jvm",
        sha = "c4cd0fd413b43785494b986fdfeec5bb47eddca196af5a2a98061faab83ed7b2",
        version = "5.1",
        test_only = False,
        url = "https://github.com/bazelbuild/rules_jvm_external/archive/{0}.zip",
        import_bind_name = "rules_jvm_external",
        strip_prefix_template = "rules_jvm_external-{0}",
    ),
    # TODO: Move this back to rules_kotlin once it has a 1.7.x release with the needed fix.
    create_http_archive_reference(
        name = "rules_kotlin",
        sha = "1d872b9c6546f0f737a356d873b164d70282760fe4c880349770abc9e494c9ce",
        version = "v1.7.2",
        test_only = False,
        url = "https://github.com/oppia/rules_kotlin/releases/download/{0}/rules_kotlin_release.tgz",
        import_bind_name = "io_bazel_rules_kotlin",
    ),
    create_http_archive_reference(
        name = "rules_proto",
        sha = "e0cab008a9cdc2400a1d6572167bf9c5afc72e19ee2b862d18581051efab42c9",
        version = "c0b62f2f46c85c16cb3b5e9e921f0d00e3101934",
        test_only = False,
        url = "https://github.com/bazelbuild/rules_proto/archive/{0}.tar.gz",
        strip_prefix_template = "rules_proto-{0}",
    ),
    create_http_jar_reference(
        name = "guava_android",
        sha = "9425a423a4cb9d9db0356300722d9bd8e634cf539f29d97bb84f457cccd16eb8",
        version = "31.0.1",
        maven_url_suffix = "com/google/guava/guava/android-{0}/guava-android-{0}.jar",
        test_only = False,
        # Create an alias for Guava Android to support maven_install's override of Guava. This
        # ensures that Guava-Android is always used in builds, even if dependencies request a newer
        # version of Guava-JRE.
        export_details = create_export_library_details(
            exposed_artifact_name = "com_google_guava_guava",
            exportable_target = "jar",
            export_toolchain = EXPORT_TOOLCHAIN.ANDROID,
            should_be_visible_to_maven_targets = True,
            additional_exports = [
                "//third_party:com_google_errorprone_error_prone_annotations",
                "//third_party:com_google_guava_failureaccess",
                "//third_party:com_google_j2objc_j2objc-annotations",
                "//third_party:org_checkerframework_checker-compat-qual",
                "//third_party:org_checkerframework_checker-qual",
            ],
        ),
    ),
    create_http_jar_reference(
        name = "kotlinx-coroutines-core-jvm",
        sha = "acc8c74b1fb88121c51221bfa7b6f5e920201bc20183ebf74165dcf5d45a8003",
        version = "1.6.0",
        maven_url_suffix = "org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/{0}/kotlinx-coroutines-core-jvm-{0}.jar",
        test_only = False,
        export_details = create_export_library_details(
            exposed_artifact_name = "kotlinx-coroutines-core-jvm",
            exportable_target = "jar",
            export_toolchain = EXPORT_TOOLCHAIN.KOTLIN,
            should_be_visible_to_maven_targets = True,
            additional_exports = [
                "//third_party:org_jetbrains_kotlin_kotlin-stdlib-jdk8",
            ],
        ),
    ),
    create_git_repository_reference(
        name = "android-spotlight",
        commit = "cc23499d37dc8533a2876e45b5063e981a4583f4",
        remote = "https://github.com/oppia/android-spotlight",
        shallow_since = "1680147372 -0700",
        test_only = False,
        repo_mapping = {"@maven": "@maven_app"},
        export_details = create_export_library_details(
            exposed_artifact_name = "com_github_takusemba_spotlight",
            exportable_target = "spotlight",
            export_toolchain = EXPORT_TOOLCHAIN.ANDROID,
        ),
    ),
    create_git_repository_reference(
        name = "androidsvg",
        commit = "4bc1d26412f0fb9fd4ef263fa93f6a64f4d4dbcf",
        remote = "https://github.com/oppia/androidsvg",
        shallow_since = "1647295507 -0700",
        test_only = False,
        export_details = create_export_library_details(
            exposed_artifact_name = "com_caverock_androidsvg",
            exportable_target = "androidsvg",
            export_toolchain = EXPORT_TOOLCHAIN.ANDROID,
        ),
    ),
    create_git_repository_reference(
        name = "circularimageview",
        commit = "35d08ba88a4a22e6e9ac96bdc5a68be27b55d09f",
        remote = "https://github.com/oppia/CircularImageview",
        shallow_since = "1622148929 -0700",
        test_only = False,
        export_details = create_export_library_details(
            exposed_artifact_name = "circularimageview_circular_image_view",
            exportable_target = "circularimageview:circular_image_view",
            export_toolchain = EXPORT_TOOLCHAIN.ANDROID,
        ),
    ),
    create_git_repository_reference(
        name = "kotlitex",
        commit = "ccdf4170817fa3b48b8e1e452772dd58ecb71cf2",
        remote = "https://github.com/oppia/kotlitex",
        shallow_since = "1679426649 -0700",
        test_only = False,
        repo_mapping = {"@maven": "@maven_app"},
        export_details = create_export_library_details(
            exposed_artifact_name = "io_github_karino2_kotlitex",
            exportable_target = "kotlitex",
            export_toolchain = EXPORT_TOOLCHAIN.ANDROID,
        ),
    ),
    create_git_repository_reference(
        name = "tools_android",
        commit = "00e6f4b7bdd75911e33c618a9bc57bab7a6e8930",
        remote = "https://github.com/bazelbuild/tools_android",
        shallow_since = "1594238320 -0400",
        test_only = False,
    ),
]
