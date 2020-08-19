load("//:oppia_android_test.bzl", "oppia_android_test")
# TODO(#1620): Remove module-specific test macros once Gradle is removed
def utility_test(name, srcs, test_class, deps):
  '''
  Creates individual tests for test files in the utility module.

  Args:
      name: str. The name of the Kotlin test file without the '.kt' suffix.
      srcs: list of str. The list of test files to be run.
      test_class: str. The package of the src file. Example: If the src is 'AsyncResultTest.kt',
          then the test_class would be "org.oppia.util.data.AsyncResultTest".
      deps: list of str. The list of dependencies needed to build and run this test.
  '''

  oppia_android_test(
    name = name,
    srcs = srcs,
    custom_package = "org.oppia.util",
    test_class = test_class,
    test_manifest = "src/test/AndroidManifest.xml",
    deps = deps,
  )
