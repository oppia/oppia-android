load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
load(":generate_android_manifest.bzl", "generate_android_manifest")

_PROD_MIN_SDK_VERSION = 19
_PROD_TARGET_SDK_VERSION = 31
_PROD_RESOURCES_PACKAGE = "org.oppia.android"

# TODO: Add docs. Note specifically kwargs is NOT used to restrict access to params.
# TODO: Consider moving assets to own package?
def oppia_android_library(
        name,
        srcs,
        visibility = ["//visibility:private"],
        deps = [],
        android_merge_deps = [],
        testonly = False,
        manifest = None,
        exports_manifest = False):
    requires_manifest = len(android_merge_deps) != 0

    # Only use kt_android_library if there are sources to build.
    kt_android_library(
        name = name,
        srcs = srcs,
        deps = deps,
        android_merge_deps = android_merge_deps,
        manifest = manifest or (
            _generate_prod_android_manifest(name) if requires_manifest else None
        ),
        testonly = testonly,
        visibility = visibility,
    )

def oppia_android_resource_library(
        name,
        resource_files,
        package = _PROD_RESOURCES_PACKAGE,
        deps = [],
        visibility = ["//visibility:private"],
        testonly = False):
    if resource_files == None or len(resource_files) == 0:
        fail("oppia_android_resource_library must be passed non-empty list of resource files.")
    native.android_library(
        name = name,
        manifest = _generate_prod_android_manifest(name, package = package),
        resource_files = resource_files,
        deps = deps,
        visibility = visibility,
        testonly = testonly,
    )

def oppia_android_assets_library(
        name,
        assets,
        assets_dir,
        visibility = ["//visibility:private"],
        testonly = False):
    native.android_library(
        name = name,
        assets = assets,
        assets_dir = assets_dir,
        manifest = _generate_prod_android_manifest(name),
        visibility = visibility,
        testonly = testonly,
    )

def _generate_prod_android_manifest(name, package = _PROD_RESOURCES_PACKAGE):
    return generate_android_manifest(
        name,
        package = package,
        min_sdk_version = _PROD_MIN_SDK_VERSION,
        target_sdk_version = _PROD_TARGET_SDK_VERSION,
    )
