load("@build_bazel_rules_android//android:rules.bzl", "android_binary")

android_binary(
  name = "app",
  custom_package = "org.oppia.app",
  incremental_dexing = 0,
  multidex = "native",
  manifest = "AndroidManifest.xml",
  enable_data_binding = True,
  deps = [
    "//app/src/main:app",
    "//model",
    "//utility",

    # TODO: figure out if we actually need this dep here.
    #"@maven//:androidx_databinding_databinding_runtime",
    "@maven//:com_android_databinding_baseLibrary",
    "@maven//:com_android_databinding_library",
    "@maven//:com_android_databinding_adapters",
    "@maven//:com_android_databinding_viewbinding",
  ],
)
