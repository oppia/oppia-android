load("//:oppia_android_test.bzl", "oppia_android_test")

def testing_test(name, src, test_class):
  '''
  This macro exists as a way to customize the oppia_android_test() macro for the testing module.
  This macro calls the oppia_android_test() macro such that the only necessary parameters for this
  macro are the parameters specific to the individual test being run.

  Args:
      name: str. The name of the Kotlin test file without the '.kt' suffix.
      src: str. The name of the Kotlin test file to be run.
      test_class: str. The package of the src file. Example: If the src is 'FakeEventLoggerTest.kt',
          then the test_class would be "org.oppia.testing.FakeEventLoggerTest".
  '''

  oppia_android_test(
    name = name,
    srcs = src,
    resource_files = native.glob(["src/main/res/**/*.xml"]),
    src_library_name = "testing_tests",
    custom_package = "org.oppia.testing",
    test_class = test_class,
    test_manifest = "src/test/AndroidManifest.xml",
  )
