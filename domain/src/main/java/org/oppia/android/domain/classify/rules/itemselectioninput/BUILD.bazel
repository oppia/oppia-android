"""
Classifiers for the 'ItemSelectionInput' interaction.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "item_selection_input_providers",
    srcs = [
        "ItemSelectionInputContainsAtLeastOneOfRuleClassifierProvider.kt",
        "ItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProvider.kt",
        "ItemSelectionInputEqualsRuleClassifierProvider.kt",
        "ItemSelectionInputIsProperSubsetOfRuleClassifierProvider.kt",
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
    name = "item_selection_input_module",
    srcs = [
        "ItemSelectionInputModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":item_selection_input_providers",
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/classify:rule_classifier",
        "//domain/src/main/java/org/oppia/android/domain/classify/rules:rule_classifier_provider",
    ],
)
