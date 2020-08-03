load("//:oppia_android_test.bzl", "oppia_android_test")

def utility_test(name, src, test_class, deps):
  '''
  This macro exists as a way to customize the oppia_android_test() macro for the utility module.
  This macro calls the oppia_android_test() macro such that the only necessary parameters for this
  macro are the parameters specific to the individual test being run.

  Args:
      name: str. The name of the Kotlin test file without the '.kt' suffix.
      src: list of str. The list of test files to be run.
      test_class: str. The package of the src file. Example: If the src is 'AsyncResultTest.kt',
          then the test_class would be "org.oppia.util.data.AsyncResultTest".
      deps: list of str. The list of dependencies needed to build and run this test.
  '''

  oppia_android_test(
    name = name,
    srcs = src,
    custom_package = "org.oppia.util",
    test_class = test_class,
    test_manifest = "src/test/AndroidManifest.xml",
    deps = deps,
  )
