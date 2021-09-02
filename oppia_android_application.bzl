"""
Macros pertaining to building & managing Android app bundles.
"""

def _convert_apk_to_aab_module_impl(ctx):
    output_file = ctx.outputs.output_file
    input_file = ctx.attr.input_file.files.to_list()[0]

    # See aapt2 help documentation for details on the arguments passed here.
    arguments = [
        "convert",
        "--output-format",
        "proto",
        "-o",
        output_file.path,
        input_file.path,
    ]

    # Reference: https://docs.bazel.build/versions/master/skylark/lib/actions.html#run.
    ctx.actions.run(
        outputs = [output_file],
        inputs = ctx.files.input_file,
        tools = [ctx.executable._aapt2_tool],
        executable = ctx.executable._aapt2_tool.path,
        arguments = arguments,
        mnemonic = "GenerateAndroidAppBundleModuleFromApk",
        progress_message = "Generating deployable AAB",
    )
    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [output_file]),
    )

def _convert_module_aab_to_structured_zip_impl(ctx):
    output_file = ctx.outputs.output_file
    input_file = ctx.attr.input_file.files.to_list()[0]

    command = """
    # Extract AAB to working directory.
    WORKING_DIR=$(mktemp -d)
    unzip -d $WORKING_DIR {0}

    # Create the expected directory structure for an app bundle.
    # Reference for copying all other files to root: https://askubuntu.com/a/951768.
    mkdir -p $WORKING_DIR/assets $WORKING_DIR/dex $WORKING_DIR/manifest $WORKING_DIR/root
    mv $WORKING_DIR/*.dex $WORKING_DIR/dex/
    mv $WORKING_DIR/AndroidManifest.xml $WORKING_DIR/manifest/
    ls -d $WORKING_DIR/* | grep -v -w -E "res|assets|dex|manifest|root|resources.pb" | xargs -n 1 -I {{}} mv {{}} root/

    # Zip up the result--this will be used by bundletool to build a deployable AAB. Note that these
    # strange file path bits are needed because zip will always retain the directory structure
    # passed via arguments (necessitating changing into the working directory).
    DEST_FILE_PATH="$(pwd)/{1}"
    cd $WORKING_DIR
    zip -r $DEST_FILE_PATH .
    """.format(input_file.path, output_file.path)

    # Reference: https://docs.bazel.build/versions/main/skylark/lib/actions.html#run_shell.
    ctx.actions.run_shell(
        outputs = [output_file],
        inputs = ctx.files.input_file,
        tools = [],
        command = command,
        mnemonic = "ConvertModuleAabToStructuredZip",
        progress_message = "Generating deployable AAB",
    )
    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [output_file]),
    )

def _bundle_module_zip_into_deployable_aab_impl(ctx):
    output_file = ctx.outputs.output_file
    input_file = ctx.attr.input_file.files.to_list()[0]
    config_file = ctx.attr.config_file.files.to_list()[0]

    # Reference: https://developer.android.com/studio/build/building-cmdline#build_your_app_bundle_using_bundletool.
    arguments = [
        "build-bundle",
        "--modules=%s" % input_file.path,
        "--config=%s" % config_file.path,
        "--output=%s" % output_file.path,
    ]

    # Reference: https://docs.bazel.build/versions/master/skylark/lib/actions.html#run.
    ctx.actions.run(
        outputs = [output_file],
        inputs = ctx.files.input_file + ctx.files.config_file,
        tools = [ctx.executable._bundletool_tool],
        executable = ctx.executable._bundletool_tool.path,
        arguments = arguments,
        mnemonic = "GenerateDeployAabFromModuleZip",
        progress_message = "Generating deployable AAB",
    )
    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [output_file]),
    )

def _generate_apks_and_install_impl(ctx):
    input_file = ctx.attr.input_file.files.to_list()[0]
    apks_file = ctx.actions.declare_file("%s_processed.apks" % ctx.label.name)
    deploy_shell = ctx.actions.declare_file("%s_run.sh" % ctx.label.name)

    # Reference: https://developer.android.com/studio/command-line/bundletool#generate_apks.
    generate_apks_arguments = [
        "build-apks",
        "--bundle=%s" % input_file.path,
        "--output=%s" % apks_file.path,
    ]

    # Reference: https://docs.bazel.build/versions/master/skylark/lib/actions.html#run.
    ctx.actions.run(
        outputs = [apks_file],
        inputs = ctx.files.input_file,
        tools = [ctx.executable._bundletool_tool],
        executable = ctx.executable._bundletool_tool.path,
        arguments = generate_apks_arguments,
        mnemonic = "BuildApksFromDeployAab",
        progress_message = "Preparing AAB deploy to device",
    )

    # References: https://github.com/bazelbuild/bazel/issues/7390,
    # https://developer.android.com/studio/command-line/bundletool#deploy_with_bundletool, and
    # https://docs.bazel.build/versions/main/skylark/rules.html#executable-rules-and-test-rules.
    # Note that the bundletool can be executed directly since Bazel creates a wrapper script that
    # utilizes its own internal Java toolchain.
    ctx.actions.write(
        output = deploy_shell,
        content = """
        #!/bin/sh
        {0} install-apks --apks={1}
        echo The APK should now be installed
        """.format(ctx.executable._bundletool_tool.short_path, apks_file.short_path),
        is_executable = True,
    )

    # Reference for including necessary runfiles for Java:
    # https://github.com/bazelbuild/bazel/issues/487#issuecomment-178119424.
    runfiles = ctx.runfiles(
        files = [
            ctx.executable._bundletool_tool,
            apks_file,
        ],
    ).merge(ctx.attr._bundletool_tool.default_runfiles)
    return DefaultInfo(
        executable = deploy_shell,
        runfiles = runfiles,
    )

_convert_apk_to_module_aab = rule(
    attrs = {
        "input_file": attr.label(
            allow_files = True,
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
        "_aapt2_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "@androidsdk//:aapt2_binary",
        ),
    },
    implementation = _convert_apk_to_aab_module_impl,
)

_convert_module_aab_to_structured_zip = rule(
    attrs = {
        "input_file": attr.label(
            allow_files = True,
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
    },
    implementation = _convert_module_aab_to_structured_zip_impl,
)

_bundle_module_zip_into_deployable_aab = rule(
    attrs = {
        "input_file": attr.label(
            allow_files = True,
            mandatory = True,
        ),
        "config_file": attr.label(
            allow_files = True,
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
        "_bundletool_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//third_party:android_bundletool",
        ),
    },
    implementation = _bundle_module_zip_into_deployable_aab_impl,
)

_generate_apks_and_install = rule(
    attrs = {
        "input_file": attr.label(
            allow_files = True,
            mandatory = True,
        ),
        "_bundletool_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//third_party:android_bundletool",
        ),
    },
    executable = True,
    implementation = _generate_apks_and_install_impl,
)

def oppia_android_application(name, config_file, **kwargs):
    """
    Creates an Android App Bundle (AAB) binary with the specified name and arguments.

    Args:
        name: str. The name of the Android App Bundle to build. This will corresponding to the name of
            the generated .aab file.
        config_file: target. The path to the .pb.json bundle configuration file for this build.
        **kwargs: additional arguments. See android_binary for the exact arguments that are available.
    """
    binary_name = "%s_binary" % name
    module_aab_name = "%s_module_aab" % name
    module_zip_name = "%s_module_zip" % name
    native.android_binary(
        name = binary_name,
        **kwargs
    )
    _convert_apk_to_module_aab(
        name = module_aab_name,
        input_file = ":%s.apk" % binary_name,
        output_file = "%s.aab" % module_aab_name,
    )
    _convert_module_aab_to_structured_zip(
        name = module_zip_name,
        input_file = ":%s.aab" % module_aab_name,
        output_file = "%s.zip" % module_zip_name,
    )
    _bundle_module_zip_into_deployable_aab(
        name = name,
        input_file = ":%s.zip" % module_zip_name,
        config_file = config_file,
        output_file = "%s.aab" % name,
    )

def declare_deployable_application(name, aab_target):
    """
    Creates a new target that can be run with 'bazel run' to install an AAB file.

    Example:
        declare_deployable_application(
            name = "install_oppia_prod",
            aab_target = "//:oppia_prod",
        )

        $ bazel run //:install_oppia_prod

    This will build (if necessary) and install the correct APK derived from the Android app bundle
    on the locally attached emulator or device. Note that this command does not support targeting a
    specific device so it will not work if more than one device is available via 'adb devices'.

    Args:
        name: str. The name of the runnable target to install an AAB file on a local device.
        aab_target: target. The target (declared via oppia_android_application) that should be made
            installable.
    """
    _generate_apks_and_install(
        name = name,
        input_file = aab_target,
    )
