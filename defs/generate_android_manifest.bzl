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
        "output_file": attr.output(mandatory = True),
        "package": attr.string(mandatory = True),
        "min_sdk_version": attr.int(mandatory = True),
        "target_sdk_version": attr.int(mandatory = True),
    },
    implementation = _generate_android_manifest_impl,
)

# TODO: Add docs
def generate_android_manifest(name, package, min_sdk_version, target_sdk_version):
    manifest_target_name = "_generate_%s_manifest" % name
    _generate_android_manifest(
        name = manifest_target_name,
        output_file = "AndroidManifest_%s.xml" % name,
        package = package,
        min_sdk_version = min_sdk_version,
        target_sdk_version = target_sdk_version,
    )
    return native.package_relative_label(manifest_target_name)
