load("//:oppia_android_test.bzl", "oppia_android_test")

def domain_test(name, src, test_class):

    oppia_android_test(
        name = name,
        srcs = src,
        src_library_name = "domain_test_lib",
        custom_package = "org.oppia.domain",
        test_class = test_class,
        test_manifest = "src/test/TestManifest.xml",
        data = native.glob(["src/main/assets/**"]),
    )
