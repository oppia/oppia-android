"""
Macros pertaining to building & managing Android app bundles.
"""

load("@bazel_skylib//rules:common_settings.bzl", "BuildSettingInfo")
load("@bazel_skylib//rules:copy_file.bzl", "copy_file")

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
        progress_message = "Generating app bundle AAB",
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
    unzip -q -d $WORKING_DIR {0}

    # Create the expected directory structure for an app bundle.
    # Reference for copying all other files to root: https://askubuntu.com/a/951768.
    mkdir -p $WORKING_DIR/assets $WORKING_DIR/dex $WORKING_DIR/manifest $WORKING_DIR/root
    mv $WORKING_DIR/*.dex $WORKING_DIR/dex/
    mv $WORKING_DIR/AndroidManifest.xml $WORKING_DIR/manifest/
    ls -d $WORKING_DIR/* | grep -v -w -E "res|assets|dex|manifest|root|resources.pb" | xargs -I{{}} sh -c "mv \\$0 $WORKING_DIR/root/ || exit 255" {{}} 2>&1 || exit $?

    # Zip up the result--this will be used by bundletool to build a deployable AAB. Note that these
    # strange file path bits are needed because zip will always retain the directory structure
    # passed via arguments (necessitating changing into the working directory).
    DEST_FILE_PATH="$(pwd)/{1}"
    cd $WORKING_DIR
    zip -q -r $DEST_FILE_PATH .
    """.format(input_file.path, output_file.path)

    # Reference: https://docs.bazel.build/versions/main/skylark/lib/actions.html#run_shell.
    ctx.actions.run_shell(
        outputs = [output_file],
        inputs = ctx.files.input_file,
        tools = [],
        command = command,
        mnemonic = "ConvertModuleAabToStructuredZip",
        progress_message = "Correcting app bundle structure",
    )
    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [output_file]),
    )

def _restrict_languages_in_raw_module_zip_impl(ctx):
    input_file = ctx.file.input_file
    output_file = ctx.outputs.output_file

    arguments = ctx.actions.args()
    arguments.add(input_file)
    arguments.add(output_file)
    ctx.actions.run(
        outputs = [output_file],
        inputs = [input_file],
        tools = [ctx.executable._filter_per_language_resources_tool],
        executable = ctx.executable._filter_per_language_resources_tool.path,
        arguments = [arguments],
        mnemonic = "RestrictLanguagesInAabModule",
        progress_message = "Removing unused language resources from module",
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

def _package_metadata_into_deployable_aab_impl(ctx):
    output_aab_file = ctx.outputs.output_aab_file
    input_aab_file = ctx.attr.input_aab_file.files.to_list()[0]
    proguard_map_file = ctx.attr.proguard_map_file.files.to_list()[0]

    command = """
    # Extract deployable AAB to working directory.
    WORKING_DIR=$(mktemp -d)
    echo $WORKING_DIR
    cp {0} $WORKING_DIR/temp.aab || exit 255

    # Change the permissions of the AAB copy so that it can be overwritten later.
    chmod 755 $WORKING_DIR/temp.aab || exit 255

    # Create directory needed for storing bundle metadata.
    mkdir -p $WORKING_DIR/BUNDLE-METADATA/com.android.tools.build.obfuscation

    # Copy over the Proguard map file.
    cp {1} $WORKING_DIR/BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map || exit 255

    $ Repackage the AAB file into the destination.
    DEST_FILE_PATH="$(pwd)/{2}"
    cd $WORKING_DIR
    zip -q -Dur temp.aab BUNDLE-METADATA || exit 255
    cp temp.aab $DEST_FILE_PATH || exit 255
    """.format(input_aab_file.path, proguard_map_file.path, output_aab_file.path)

    # Reference: https://docs.bazel.build/versions/main/skylark/lib/actions.html#run_shell.
    ctx.actions.run_shell(
        outputs = [output_aab_file],
        inputs = ctx.files.input_aab_file + ctx.files.proguard_map_file,
        tools = [],
        command = command,
        mnemonic = "PackageMetadataIntoDeployableAAB",
        progress_message = "Packaging symbols file into deployable AAB",
    )
    return DefaultInfo(
        files = depset([output_aab_file]),
        runfiles = ctx.runfiles(files = [output_aab_file]),
    )

def _sign_and_rename_aab_impl(ctx):
    # Extract jarsigner from the Bazel Java runtime.
    java_runtime = ctx.toolchains["@bazel_tools//tools/jdk:runtime_toolchain_type"].java_runtime
    java_bin_path = java_runtime.java_executable_exec_path
    jarsigner_path = java_bin_path[:java_bin_path.rfind("/")] + "/jarsigner"

    input_aab = ctx.file.input_aab
    key_alias = ctx.attr.key_alias[BuildSettingInfo].value
    bundletool = ctx.executable._bundletool_tool

    # Determine which keystore to use.
    keystore_filepath = ctx.attr.keystore[BuildSettingInfo].value
    keystore_password_filepath = ctx.attr.keystore_password_file[BuildSettingInfo].value
    additional_keystore_inputs = []
    if keystore_filepath and keystore_password_filepath:
        keystore_path = keystore_filepath
        keystore_password_path = keystore_password_filepath
    else:
        # Fall back to the default debug keystore (which requires a password file).
        keystore_path = ctx.file._debug_keystore.path
        additional_keystore_inputs.append(ctx.file._debug_keystore)

        debug_password_file = ctx.actions.declare_file(ctx.label.name + "_debug_password.txt")
        ctx.actions.write(
            output = debug_password_file,
            content = "android",
        )
        keystore_password_path = debug_password_file.path
        additional_keystore_inputs.append(debug_password_file)

    output_aab = ctx.actions.declare_file(ctx.label.name + ".aab")
    output_dir = ctx.actions.declare_directory(ctx.label.name + "_release")

    command = """
    # Ensure that subshells correctly bubble their failures to the outer shell.
    set -o pipefail
    mkdir -p {output_dir}
    cp {input_aab} {output_aab} || exit 255

    VERSION_NAME=$({bundletool} dump manifest --bundle={input_aab} | grep -o 'android:versionName="[^"]*"' | cut -d'"' -f2) || exit 255
    RENAMED_AAB_PATH="{output_dir}/oppia-android-$VERSION_NAME.aab"
    cp {input_aab} $RENAMED_AAB_PATH || exit 255

    JARSIGNER_LOG_FILE=$(mktemp)
    if ! {jarsigner_path} -keystore {keystore} -storepass:file {keystore_password_file} -keypass:file {keystore_password_file} $RENAMED_AAB_PATH "{key_alias}" > "$JARSIGNER_LOG_FILE" 2>&1 ; then
        cat "$JARSIGNER_LOG_FILE" >&2
        rm -f "$JARSIGNER_LOG_FILE"
        exit 255
    fi
    rm -f "$JARSIGNER_LOG_FILE"

    echo "Dev-only AAB:        bazel-bin/{name}.aab"
    echo "Renamed Release AAB: bazel-bin/{name}_release/oppia-android-$VERSION_NAME.aab"
    echo ""
    """.format(
        input_aab = input_aab.path,
        keystore = keystore_path,
        keystore_password_file = keystore_password_path,
        key_alias = key_alias,
        bundletool = bundletool.path,
        jarsigner_path = jarsigner_path,
        output_aab = output_aab.path,
        output_dir = output_dir.path,
        name = ctx.label.name,
    )

    ctx.actions.run_shell(
        outputs = [output_aab, output_dir],
        inputs = [input_aab, ctx.info_file] + additional_keystore_inputs,
        tools = depset(
            direct = [bundletool],
            transitive = [java_runtime.files],
        ),
        command = command,
        mnemonic = "SignAndRenameAab",
        progress_message = "Re-signing and renaming AAB for production deployment",
        execution_requirements = {
            "no-cache": "",
            "no-sandbox": "1",
            "local": "1",
        },
    )
    return DefaultInfo(
        files = depset([output_aab, output_dir]),
        runfiles = ctx.runfiles(files = [output_aab, output_dir]),
    )

def _generate_universal_apk_impl(ctx):
    input_aab_file = ctx.attr.input_aab_file.files.to_list()[0]
    output_apk_file = ctx.outputs.output_apk_file
    debug_keystore_file = ctx.attr.debug_keystore.files.to_list()[0]
    apks_file = ctx.actions.declare_file("%s_processed.apks" % ctx.label.name)

    # Reference: https://developer.android.com/tools/bundletool#generate_apks.
    # See also the Bazel BUILD file for the keystore for details on its password and alias.
    generate_universal_apk_arguments = [
        "build-apks",
        "--bundle=%s" % input_aab_file.path,
        "--output=%s" % apks_file.path,
        "--ks=%s" % debug_keystore_file.path,
        "--ks-pass=pass:android",
        "--ks-key-alias=androiddebugkey",
        "--key-pass=pass:android",
        "--mode=universal",
    ]

    # bundletool only generates an APKs file, so the universal APK still needs to be extracted.

    # Reference: https://docs.bazel.build/versions/master/skylark/lib/actions.html#run.
    ctx.actions.run(
        outputs = [apks_file],
        inputs = ctx.files.input_aab_file + ctx.files.debug_keystore,
        tools = [ctx.executable._bundletool_tool],
        executable = ctx.executable._bundletool_tool.path,
        arguments = generate_universal_apk_arguments,
        mnemonic = "GenerateUniversalAPK",
        progress_message = "Generating universal APK from AAB",
    )

    command = """
    # Extract APK to working directory.
    unzip -q "$(pwd)/{0}" universal.apk
    mv universal.apk "$(pwd)/{1}"
    """.format(apks_file.path, output_apk_file.path)

    # Reference: https://docs.bazel.build/versions/main/skylark/lib/actions.html#run_shell.
    ctx.actions.run_shell(
        outputs = [output_apk_file],
        inputs = [apks_file],
        tools = [],
        command = command,
        mnemonic = "ExtractUniversalAPK",
        progress_message = "Extracting universal APK from .apks file",
    )

    return DefaultInfo(
        files = depset([output_apk_file]),
        runfiles = ctx.runfiles(files = [output_apk_file]),
    )

_convert_apk_to_module_aab = rule(
    attrs = {
        "input_file": attr.label(
            allow_single_file = True,
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
            allow_single_file = True,
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
    },
    implementation = _convert_module_aab_to_structured_zip_impl,
)

_restrict_languages_in_raw_module_zip = rule(
    attrs = {
        "input_file": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
        "_filter_per_language_resources_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//scripts:filter_per_language_resources",
        ),
    },
    implementation = _restrict_languages_in_raw_module_zip_impl,
)

_bundle_module_zip_into_deployable_aab = rule(
    attrs = {
        "input_file": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "config_file": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
        "_bundletool_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//third_party:android_bundletool_binary",
        ),
    },
    implementation = _bundle_module_zip_into_deployable_aab_impl,
)

_package_metadata_into_deployable_aab = rule(
    attrs = {
        "input_aab_file": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "proguard_map_file": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "output_aab_file": attr.output(
            mandatory = True,
        ),
    },
    implementation = _package_metadata_into_deployable_aab_impl,
)

_sign_and_rename_aab = rule(
    attrs = {
        "input_aab": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "keystore": attr.label(
            mandatory = True,
        ),
        "keystore_password_file": attr.label(
            mandatory = True,
        ),
        "key_alias": attr.label(mandatory = True),
        "_bundletool_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//third_party:android_bundletool_binary",
        ),
        "_debug_keystore": attr.label(
            default = Label("@bazel_tools//tools/android:debug_keystore"),
            allow_single_file = True,
        ),
    },
    toolchains = ["@bazel_tools//tools/jdk:runtime_toolchain_type"],
    implementation = _sign_and_rename_aab_impl,
)

_generate_universal_apk = rule(
    attrs = {
        "input_aab_file": attr.label(
            allow_single_file = [".aab"],
            mandatory = True,
        ),
        "output_apk_file": attr.output(
            mandatory = True,
        ),
        "debug_keystore": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "_bundletool_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//third_party:android_bundletool_binary",
        ),
    },
    implementation = _generate_universal_apk_impl,
)

def oppia_android_application(name, config_file, proguard_generate_mapping, production_release, **kwargs):
    """
    Creates an Android App Bundle (AAB) binary with the specified name and arguments.

    This generates a mobile-installable target that ends in '_binary'. For example, if there's an
    Oppia Android application defined with the name 'oppia_dev' then its APK binary can be
    mobile-installed using:

      bazel mobile-install //:oppia_dev_binary

    Args:
        name: str. The name of the Android App Bundle to build. This will corresponding to the name
            of the generated .aab file.
        config_file: target. The path to the .pb.json bundle configuration file for this build.
        proguard_generate_mapping: boolean. Whether to perform a Proguard optimization step &
            generate Proguard mapping corresponding to the obfuscation step.
        production_release: boolean. Whether this is a production-facing release build which will
            undergo additional renaming and, if configured, signing.
        **kwargs: additional arguments. See android_binary for the exact arguments that are
            available.
    """

    binary_name = "%s_binary" % name
    binary_file_name = "%s.apk" % binary_name
    proguard_map_file_name = ":%s_proguard.map" % binary_name

    main_module_name = "%s_main_module" % name
    main_module_file_name = "%s.aab" % main_module_name

    corrected_structure_app_module_name = "%s_corrected_structure_app_module" % name
    corrected_structure_app_module_file_name = "%s.zip" % corrected_structure_app_module_name

    language_restricted_module_name = "%s_language_restricted_module" % name
    language_restricted_module_file_name = "%s.zip" % language_restricted_module_name

    deployable_name = "%s_deployable" % name
    deployable_file_name = "%s.aab" % deployable_name

    deployable_with_symbols_aab_name = "%s_deployable_with_symbols" % name
    deployable_with_symbols_aab_file_name = "%s.aab" % deployable_with_symbols_aab_name

    native.android_binary(
        name = binary_name,
        tags = ["manual"],
        proguard_generate_mapping = proguard_generate_mapping,
        **kwargs
    )
    _convert_apk_to_module_aab(
        name = main_module_name,
        input_file = binary_file_name,
        output_file = main_module_file_name,
        tags = ["manual"],
    )
    _convert_module_aab_to_structured_zip(
        name = corrected_structure_app_module_name,
        input_file = main_module_file_name,
        output_file = corrected_structure_app_module_file_name,
        tags = ["manual"],
    )
    _restrict_languages_in_raw_module_zip(
        name = language_restricted_module_name,
        input_file = corrected_structure_app_module_file_name,
        output_file = language_restricted_module_file_name,
        tags = ["manual"],
    )
    _bundle_module_zip_into_deployable_aab(
        name = deployable_name,
        input_file = language_restricted_module_file_name,
        config_file = config_file,
        output_file = deployable_file_name,
        tags = ["manual"],
    )
    if proguard_generate_mapping:
        _package_metadata_into_deployable_aab(
            name = deployable_with_symbols_aab_name,
            input_aab_file = deployable_file_name,
            proguard_map_file = proguard_map_file_name,
            output_aab_file = deployable_with_symbols_aab_file_name,
            tags = ["manual"],
        )
        deployable_and_maybe_symbols_added_aab_file_name = deployable_with_symbols_aab_file_name
    else:
        deployable_and_maybe_symbols_added_aab_file_name = deployable_file_name
    if production_release:
        _sign_and_rename_aab(
            name = name,
            input_aab = ":%s" % deployable_and_maybe_symbols_added_aab_file_name,
            keystore = "//config:keystore_file",
            keystore_password_file = "//config:keystore_password_file",
            key_alias = "//config:key_alias",
            tags = ["manual"],
        )
    else:
        # Copy over the file to its expected location.
        copy_file(
            name = name,
            src = deployable_and_maybe_symbols_added_aab_file_name,
            out = "%s.aab" % name,
        )

def generate_universal_apk(name, aab_target):
    """
    Creates a new 'bazel mobile-install'-able universal APK target for the provided AAB target.

    Example usage in a top-level BUILD.bazel file and CLI:
        generate_universal_apk(
            name = "oppia_prod_universal_apk",
            aab_target = "//:oppia_prod",
        )

        $ bazel mobile-install //:oppia_prod_universal_apk

    Note that, sometimes, you may not want to use mobile-install such as for production builds that
    may have functional disparity from incremental installations of the app. In those cases, it's
    best to uninstall the app from the target device and install the APK directly using
    'adb install' as so (per the above example):

        $ adb install bazel-bin/oppia_prod_universal_apk.apk

    Args:
        name: str. The name of the runnable target to install an AAB file on a local device.
        aab_target: target. The target (declared via oppia_android_application) that should be made
            installable.
    """
    _generate_universal_apk(
        name = name,
        input_aab_file = aab_target,
        output_apk_file = "%s.apk" % name,
        debug_keystore = "@bazel_tools//tools/android:debug_keystore",
        tags = ["manual"],
    )
