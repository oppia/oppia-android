load("//:oppia_android_test.bzl", "oppia_android_test")

def testing_test(name, src, test_class):

    oppia_android_test(
        name = name,
        srcs = src,
        resource_files = native.glob(["src/main/res/**/*.xml"]),
        src_library_name = "testing_tests",
        custom_package = "org.oppia.testing",
        test_class = test_class,
        test_manifest = "src/test/AndroidManifest.xml",
    )
