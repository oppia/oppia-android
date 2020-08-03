load("//:oppia_android_test.bzl", "oppia_android_test")

def domain_test(name, srcs, test_class, deps):

  oppia_android_test(
    name = name,
    srcs = srcs,
    custom_package = "org.oppia.domain",
    test_class = test_class,
    test_manifest = "src/test/AndroidManifest.xml",
    assets = native.glob(["src/main/assets/**"]),
    assets_dir = "src/main/assets/",
    deps = deps,
  )
