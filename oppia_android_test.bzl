load("@rules_jvm_external//:defs.bzl", "artifact")
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

def oppia_android_test(name, srcs, test_manifest, custom_package, test_class, deps, assets=None,
                       assets_dir=None):
  '''
  Creates an Oppia test target for running the specified test as an Android local test with Kotlin
  support. Note that this creates an additional, internal library.

  Args:
      name: str. The name of the Kotlin test file without the '.kt' suffix.
      srcs: list of str. The name of the Kotlin test files to be run.
      test_manifest: str. The path to the test manifest file.
      custom_package: str. The module's package. Example: 'org.oppia.utility'.
      test_class: The package of the src file. For example, if the src is 'FakeEventLoggerTest.kt',
          then the test_class would be "org.oppia.testing.FakeEventLoggerTest".
      deps: list of str. The list of dependencies needed to run the tests.
      assets: list of str. A list of assets needed to run the tests.
      assets_dir: str. The path to the assets directory.
  '''

  kt_android_library(
    name = name + "_lib",
    custom_package = custom_package,
    srcs = srcs,
    deps = deps,
    testonly = True,
    assets = assets,
    assets_dir = assets_dir,
  )

  native.android_local_test(
    name = name,
    custom_package = custom_package,
    test_class = test_class,
    manifest = test_manifest,
    deps = [ ":" + name + "_lib",] + deps,
  )
