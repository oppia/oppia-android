load("@rules_jvm_external//:defs.bzl", "artifact")
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

def oppia_android_test(name, srcs, test_manifest, custom_package, test_class, src_library_name,
                       resource_files=None, assets=None, assets_dir=None):
  '''
  This macro exists as a way to set up a test in Oppia Android to be run with Bazel.
  This macro creates a library for an individual test that is fed as a dependency into an
  android_local_test() rule which configures the test to be run with Roboletric and Bazel.

  Args:
      name: str. The name of the Kotlin test file without the '.kt' suffix.
      srcs: str. The name of the Kotlin test file to be run.
      test_manifest: str. The path to the test manifest file.
      custom_package: str. The module's package. Example: 'org.oppia.utility'.
      resource_files: str. The path to the resource files. This is typically a glob([]).
      test_class: The package of the src file. For example, if the src is 'FakeEventLoggerTest.kt',
          then the test_class would be "org.oppia.testing.FakeEventLoggerTest".
      src_library_name: str. The name of the library that builds the module's test files.
  '''

  kt_android_library(
    name = name + "_lib",
    custom_package = custom_package,
    srcs = srcs,
    resource_files = resource_files,
    manifest = test_manifest,
    deps = [
        ":" + src_library_name,
    ],
    assets = assets,
    assets_dir = assets_dir,
  )

  native.android_local_test(
    name = name,
    custom_package = custom_package,
    test_class = test_class,
    manifest = test_manifest,
    deps = [
       ":" + name + "_lib",
       ":dagger",
       "@robolectric//bazel:android-all",
       artifact("org.jetbrains.kotlin:kotlin-reflect"),
       artifact("org.robolectric:robolectric"),
    ],
  )
