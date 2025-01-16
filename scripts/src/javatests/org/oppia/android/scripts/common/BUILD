"""
Tests corresponding to common libraries that potentially support multiple scripts by performing
common or generic operations.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

kt_jvm_test(
    name = "BazelClientTest",
    srcs = ["BazelClientTest.kt"],
    shard_count = 4,
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:bazel_client",
        "//scripts/src/java/org/oppia/android/scripts/testing:test_bazel_workspace",
        "//testing:assertion_helpers",
        "//testing/src/main/java/org/oppia/android/testing/mockito",
        "//third_party:com_google_truth_truth",
        "//third_party:org_mockito_mockito-core",
    ],
)

kt_jvm_test(
    name = "AndroidBuildSdkPropertiesTest",
    srcs = ["AndroidBuildSdkPropertiesTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:android_build_sdk_properties",
        "//third_party:com_google_truth_truth",
    ],
)

kt_jvm_test(
    name = "CommandExecutorImplTest",
    srcs = ["CommandExecutorImplTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:command_executor",
        "//testing:assertion_helpers",
        "//third_party:com_google_truth_truth",
    ],
)

kt_jvm_test(
    name = "GitClientTest",
    srcs = ["GitClientTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:git_client",
        "//scripts/src/java/org/oppia/android/scripts/testing:test_git_repository",
        "//testing:assertion_helpers",
        "//third_party:com_google_truth_truth",
    ],
)

kt_jvm_test(
    name = "GitHubClientTest",
    srcs = ["GitHubClientTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:github_client",
        "//scripts/src/java/org/oppia/android/scripts/common/testing:fake_command_executor",
        "//testing:assertion_helpers",
        "//third_party:com_google_truth_truth",
        "//third_party:com_squareup_okhttp3_mockwebserver",
    ],
)

kt_jvm_test(
    name = "ProtoStringEncoderTest",
    srcs = ["ProtoStringEncoderTest.kt"],
    deps = [
        "//model/src/main/proto:test_models",
        "//scripts/src/java/org/oppia/android/scripts/common:proto_string_encoder",
        "//testing:assertion_helpers",
        "//third_party:com_google_truth_extensions_truth-liteproto-extension",
        "//third_party:com_google_truth_truth",
    ],
)

kt_jvm_test(
    name = "RepositoryFileTest",
    srcs = ["RepositoryFileTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:repository_file",
        "//third_party:com_google_truth_truth",
        "//third_party:org_jetbrains_kotlin_kotlin-test-junit",
    ],
)

kt_jvm_test(
    name = "ScriptBackgroundCoroutineDispatcherTest",
    srcs = ["ScriptBackgroundCoroutineDispatcherTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:script_background_coroutine_dispatcher",
        "//testing:assertion_helpers",
        "//third_party:org_jetbrains_kotlin_kotlin-test-junit",
        "//third_party:org_mockito_mockito-core",
    ],
)
