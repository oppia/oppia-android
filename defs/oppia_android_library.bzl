"""
Macros for creating various Android library targets that are specifically compatible with Oppia
Android's bazel configuration.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
load("//defs:build_vars.bzl", "MIN_SDK_VERSION", "TARGET_SDK_VERSION")
load(":generate_android_manifest.bzl", "generate_android_manifest")

_PROD_RESOURCES_PACKAGE = "org.oppia.android"

def oppia_android_library(
        name,
        srcs,
        visibility = ["//visibility:private"],
        deps = [],
        android_merge_deps = [],
        requires_android_resources_support = False,
        testonly = False,
        manifest = None,
        exports_manifest = False):
    """Creates an Android-compatible library target, similar to kt_android_library().

    This macro must be used instead of android_library()/kt_android_library() because it ensures
    proper set-up for both resource processing and rules_kotlin's unused dependencies checker. This
    macro may automatically generate a manifest for the library (if any resource processing is
    needed). The default resources package used is "org.oppia.android" but, generally, the package
    of referenced resources will depend on *their* declared package--see
    oppia_android_resource_library(). The auto-generated manifest will also default the library's
    minimum and target Android SDK versions to that of the production app.

    When sources need access to Android assets, resources, or manifest entries (such as for
    intenting to an Activity), they need to indicate this one of two ways:
    1. (Preferred) Adding the dependency to 'android_merge_deps'.
    2. Setting 'requires_android_resources_support' to 'True'.

    (2) should only be done if (1) won't work (i.e. the sources depend both on code from a
    dependency and that dependency's Android assets/resources/manifest).

    A note for development: this macro intentionally doesn't expose android_library's extra
    parameters (e.g. with kwargs) to help restrict access from other functionality that might cause
    interoperability or difficult-to-debug issues. The most common features are already exposed via
    parameters, or via other macros (see oppia_android_resource_library and
    oppia_android_assets_library). This also simplifies importing resources since android_library()
    automatically re-namespaces Android resources that are part of its merging operation (in some
    cases--this is still not fully understood).

    Args:
        name: str. The name of the library target being defined. Conventionally, this is always in
            snake_case.
        srcs: list of label. The sources to compile into this library.
        visibility: list of label. The visibility of the new library target. This is by default
            private and ignores the default visibility of the calling package.
        deps: list of label. The Java, Kotlin, or Android dependencies that this library requires to
            build and run. This defaults to an empty list.
        android_merge_deps: list of label. The Android dependencies that this library requires
            resources and/or assets from (but not code--those dependencies should go in 'deps').
            This defaults to empty list.
        requires_android_resources_support: boolean. Whether the generated library should be forced
            to have support for resources from dependencies. This may need to be set if 'deps'
            includes targets with resources or assets, but 'android_merge_deps' is empty. Enable
            this if you receive errors like "cannot find symbol R" and android_merge_deps is empty.
            This cannot be set together with 'android_merge_deps'. This defaults to 'False'.
        testonly: boolean. Whether this library should only be accessible to tests and test
            libraries. This defaults to 'False'.
        manifest: label. The AndroidManifest.xml file to use as this library's manifest, instead of
            having no manifest (in the event of not needing Android resources or assets), or a
            generated manifest. Generally, this should only be used by libraries exposing new
            functionality through their manifest (such as an Activity to intent to). This defaults
            to 'None'.
        exports_manifest: boolean. Whether to export the manifest generated and/or provided by this
            library. This needs to be 'True' for manifests providing functionality elsewhere in the
            app (such as declaring an Activity that can be intented). This defaults to 'False'.
    """
    if requires_android_resources_support and len(android_merge_deps) != 0:
        fail(
            "requires_android_resources_support doesn't need to be set when android_merge_deps" +
            " are provided.",
        )
    requires_manifest = requires_android_resources_support or len(android_merge_deps) != 0

    # Only use kt_android_library if there are sources to build.
    kt_android_library(
        name = name,
        srcs = srcs,
        deps = deps,
        custom_package = "org.oppia.android" if requires_manifest else None,
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
    """Creates an Android-compatible library that only holds Android resources.

    This macro introduces a different pattern than other Bazel Android apps use in that it forces
    resources to be separately declared from Android library code. This is done to largely simplify
    splitting out resources to make them separately available as a dependency for cases when
    downstream libraries need access to the resources, but not to attached Kotlin/Java code. This
    helps both with build graph simplification and unused dependency checking. As such, this is the
    only exposed way to define Android resources. Note that databinding resources are not currently
    supported and are isolated to their own package.

    Args:
        name: str. The name of the library target being defined. Conventionally, this is always in
            snake_case.
        resource_files: list of label. The Android resources to build into the new library.
        package: str. The package used for the generated resources. This is the package that
            downstream dependent code will need to use to import the resource unless the resources
            are merged and re-namespaced. This defaults to "org.oppia.android".
        deps: list of label. The Android dependencies needed to build this resource library. This
            should only be libraries that have their own Android resources and whose resources are
            directly referenced by the resources in this library. This defaults to empty list.
        visibility: list of label. The visibility of the new library target. This is by default
            private and ignores the default visibility of the calling package.
        testonly: boolean. Whether this library should only be accessible to tests and test
            libraries. This defaults to 'False'.
    """
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
    """Creates an Android-compatible library that only holds asset files.

    This has many of the same caveats as oppia_android_resource_library() except it is a bit simpler
    as it can only hold asset files, those are, files that should be directly available to
    downstream Kotlin/Java code during runtime (and are exposed via Android's AssetManager).

    Args:
        name: str. The name of the library target being defined. Conventionally, this is always in
            snake_case.
        assets: list of label. The files that should be included as assets in this library. Note
            that these files must include the path specified by assets_dir or they will otherwise be
            ignored during library creation and not exposed to downstream dependencies.
        assets_dir: str. The directory prefix from which 'assets' should be pulled. This essentially
            acts as both a filter and a prefix stripper: only the assets under the specified
            directory will be included in the final library, and those assets will be exposed
            starting at the provided directory (meaning the directory path itself doesn't need to be
            part of the referenced path). For example, suppose an asset with path
            "src/main/assets/protos/my_proto.pb" is being imported with an assets_dir of
            "src/main/assets". This will result in a file being included at runtime that's
            accessible with asset path (i.e. through Android's AssetManager) of
            "protos/my_proto.pb".
        visibility: list of label. The visibility of the new library target. This is by default
            private and ignores the default visibility of the calling package.
        testonly: boolean. Whether this library should only be accessible to tests and test
            libraries. This defaults to 'False'.
    """
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
        min_sdk_version = MIN_SDK_VERSION,
        target_sdk_version = TARGET_SDK_VERSION,
    )
