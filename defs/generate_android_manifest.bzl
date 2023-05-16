"""
Macros for generating a build-time AndroidManifest.xml file for android_*() targets.
"""

def _generate_android_manifest_impl(ctx):
    output_file = ctx.outputs.output_file
    package = ctx.attr.package
    min_sdk_version = ctx.attr.min_sdk_version
    target_sdk_version = ctx.attr.target_sdk_version

    ctx.actions.write(
        output_file,
        content =
            """<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="{0}">
    <uses-sdk android:minSdkVersion="{1}" android:targetSdkVersion="{2}" />
</manifest>
""".format(package, min_sdk_version, target_sdk_version),
    )

    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [output_file]),
    )

_generate_android_manifest = rule(
    attrs = {
        "min_sdk_version": attr.int(mandatory = True),
        "output_file": attr.output(mandatory = True),
        "package": attr.string(mandatory = True),
        "target_sdk_version": attr.int(mandatory = True),
    },
    implementation = _generate_android_manifest_impl,
)

def generate_android_manifest(name, package, min_sdk_version, target_sdk_version):
    """Generates a build-time AndroidManifest.xml file.

    A build-time manifest can be useful for cases when android_library requires a manifest to be
    provided (such as in the cases of processing resources or assets) without needing to maintain
    on-disk manifests that require updating.

    Args:
        name: str. The name of the target using this manifest. The actual name of the manifest
            target itself will be derived from this name.
        package: str. The Android Java package name used for the corresponding library that will use
            this manifest.
        min_sdk_version: int. The minimum SDK version of the Android package using manifest.
        target_sdk_version: int. The target SDK version of the Android package using manifest.

    Returns:
        label. The package-relative label to the newly generated manifest.
    """
    manifest_target_name = "_generate_%s_manifest" % name
    _generate_android_manifest(
        name = manifest_target_name,
        output_file = "AndroidManifest_%s.xml" % name,
        package = package,
        min_sdk_version = min_sdk_version,
        target_sdk_version = target_sdk_version,
    )
    return native.package_relative_label(manifest_target_name)
