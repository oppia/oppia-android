"""
Classifiers for the 'NumericInput' interaction.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "numeric_input_providers",
    srcs = [
        "NumericInputEqualsRuleClassifierProvider.kt",
        "NumericInputIsGreaterThanOrEqualToRuleClassifierProvider.kt",
        "NumericInputIsGreaterThanRuleClassifierProvider.kt",
        "NumericInputIsInclusivelyBetweenRuleClassifierProvider.kt",
        "NumericInputIsLessThanOrEqualToRuleClassifierProvider.kt",
        "NumericInputIsLessThanRuleClassifierProvider.kt",
        "NumericInputIsWithinToleranceRuleClassifierProvider.kt",
    ],
    deps = [
        "//domain/src/main/java/org/oppia/android/domain/classify:classification_context",
        "//domain/src/main/java/org/oppia/android/domain/classify:rule_classifier",
        "//domain/src/main/java/org/oppia/android/domain/classify/rules:generic_rule_classifier",
        "//domain/src/main/java/org/oppia/android/domain/classify/rules:rule_classifier_provider",
        "//domain/src/main/java/org/oppia/android/domain/util:extensions",
        "//model/src/main/proto:interaction_object_java_proto_lite",
        "//model/src/main/proto:translation_java_proto_lite",
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "numeric_input_rule_module",
    srcs = [
        "NumericInputRuleModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":numeric_input_providers",
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/classify:rule_classifier",
        "//domain/src/main/java/org/oppia/android/domain/classify/rules:rule_classifier_provider",
    ],
)
