"""
Instrumentation macros to define up end-to-end tests.
"""

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")
load("@bazel_skylib//rules:common_settings.bzl", "bool_flag")

def oppia_instrumentation_test(
        name,
        srcs,
        deps):
    """
    Creates library, binary, and instrumentation test for each test suite.

    Args:
        name: str. The class name of the test suite.
        srcs: list of str. List of test files corresponding to this test suite.
        deps: list of str. The list of dependencies needed to build and run the test.
    """
    kt_android_library(
        name = "%s_lib" % name,
        testonly = True,
        srcs = srcs,
        deps = deps + [
            "//app:databinding_resources",
            "//third_party:androidx_databinding_databinding-common",
            "//third_party:androidx_databinding_databinding-runtime",
            "//third_party:javax_annotation_javax_annotation-api",
        ],
        custom_package = "org.oppia.android",
        enable_data_binding = True,
        manifest = "//instrumentation:src/javatests/AndroidManifest.xml",
    )

    native.android_binary(
        name = "%sBinary" % name,
        testonly = True,
        custom_package = "org.oppia.android",
        instruments = "//instrumentation:oppia_test",
        manifest = "//instrumentation:src/javatests/AndroidManifest.xml",
        deps = [":%s_lib" % name],
    )

    # TODO(#3617): Target isn't supported yet. Remove the manual tag once fixed.
    native.android_instrumentation_test(
        name = name,
        target_device = "@android_test_support//tools/android/emulated_devices/generic_phone:android_23_x86_qemu2",
        test_app = ":%sBinary" % name,
        tags = ["manual"],
    )

_AvdInfo = provider(
    "Info related to created Android Virtual Devices.",
    fields = {
        "target_system_image_files": "System image files that must be included for emulation.",
    },
)

def _define_android_virtual_device_impl(ctx):
    avd_bundle = ctx.outputs.avd_bundle
    target_system_image_files = ctx.files.target_system_image_files

    arguments = ctx.actions.args()
    arguments.add("--avd-bundle-path", avd_bundle.path)
    arguments.add_joined(
        "--system-image-paths",
        [file.path for file in target_system_image_files],
        join_with = ",",
    )
    arguments.add("--hardware-profiles-proto", ctx.file.hardware_profiles_proto)
    arguments.add("--hardware-profile", ctx.attr.hardware_profile)
    arguments.add("--emulator-binary-path", ctx.file._emulator_tool)
    arguments.add("--adb-binary-path", ctx.file._adb_tool)
    if ctx.attr.disable_acceleration:
        arguments.add("--disable-acceleration")
    ctx.actions.run(
        outputs = [avd_bundle],
        inputs = [ctx.file.hardware_profiles_proto] + target_system_image_files,
        tools = [ctx.executable._create_avd_tool, ctx.file._emulator_tool, ctx.file._adb_tool],
        executable = ctx.executable._create_avd_tool.path,
        arguments = [arguments],
        mnemonic = "GenerateAvd",
        progress_message = "Generating Android Virtual Device",
    )
    return [
        DefaultInfo(
            files = depset([avd_bundle]),
            runfiles = ctx.runfiles(files = [avd_bundle]),
        ),
        _AvdInfo(target_system_image_files = depset([ctx.attr.target_system_image_files])),
    ]

def _create_runnable_emulator_impl(ctx):
    avd_bundle = ctx.file.avd_bundle
    target_system_image_files = [
        file
        for target in ctx.attr.avd_bundle[_AvdInfo].target_system_image_files.to_list()
        for file in target.files.to_list()
    ]
    emulator_runtime_directory = ctx.actions.declare_directory(
        "emulator_runtime_%s" % ctx.label.name,
    )
    run_emulator_shell = ctx.actions.declare_file("run_emulator.sh")

    ctx.actions.run_shell(
        outputs = [emulator_runtime_directory],
        command = "mkdir %s" % emulator_runtime_directory.path,
    )

    target_system_image_file_line = ",".join([file.path for file in target_system_image_files])
    arguments = [ctx.executable._run_emulator_tool.short_path]
    arguments.extend(["--avd-bundle-path", avd_bundle.short_path])
    arguments.extend(["--working-path", emulator_runtime_directory.short_path])
    arguments.extend(["--system-image-paths", target_system_image_file_line])
    arguments.extend(["--emulator-binary-path", ctx.file._emulator_tool.short_path])
    arguments.extend(["--adb-binary-path", ctx.file._adb_tool.short_path])
    if ctx.attr.disable_rendering:
        arguments.append("--disable-rendering")
    if ctx.attr.disable_acceleration:
        arguments.append("--disable-acceleration")
    if ctx.attr.disable_fastboot:
        arguments.append("--disable-fastboot")
    ctx.actions.write(
        output = run_emulator_shell,
        content = """
        #!/bin/sh
        {0}
        """.format(" ".join(arguments)),
        is_executable = True,
    )

    # Reference for including necessary runfiles for Java:
    # https://github.com/bazelbuild/bazel/issues/487#issuecomment-178119424.
    runfiles = ctx.runfiles(
        files = target_system_image_files + [
            avd_bundle,
            ctx.executable._run_emulator_tool,
            ctx.file._emulator_tool,
            ctx.file._adb_tool,
        ],
    ).merge(ctx.attr._run_emulator_tool.default_runfiles)
    return DefaultInfo(
        executable = run_emulator_shell,
        runfiles = runfiles,
    )

_define_android_virtual_device = rule(
    attrs = {
        "avd_bundle": attr.output(
            mandatory = True,
        ),
        "target_system_image_files": attr.label(
            mandatory = True,
        ),
        "hardware_profiles_proto": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "hardware_profile": attr.string(
            mandatory = True,
        ),
        "disable_acceleration": attr.bool(),
        "_adb_tool": attr.label(
            allow_single_file = True,
            default = "@androidsdk//:adb",
        ),
        "_emulator_tool": attr.label(
            allow_single_file = True,
            default = "@androidsdk//:emulator",
        ),
        "_create_avd_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//scripts:create_avd",
        ),
    },
    implementation = _define_android_virtual_device_impl,
)

_create_runnable_emulator = rule(
    attrs = {
        "avd_bundle": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "disable_rendering": attr.bool(),
        "disable_acceleration": attr.bool(),
        "disable_fastboot": attr.bool(),
        "_adb_tool": attr.label(
            allow_single_file = True,
            default = "@androidsdk//:adb",
        ),
        "_emulator_tool": attr.label(
            allow_single_file = True,
            default = "@androidsdk//:emulator",
        ),
        "_run_emulator_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//scripts:run_emulator",
        ),
    },
    executable = True,
    implementation = _create_runnable_emulator_impl,
)

def define_android_virtual_device(
        name,
        target_system_image_files,
        hardware_profiles_proto,
        hardware_profile):
    _define_android_virtual_device(
        name = name,
        avd_bundle = "%s_avd.tgz" % name,
        target_system_image_files = target_system_image_files,
        hardware_profiles_proto = hardware_profiles_proto,
        hardware_profile = hardware_profile,
        testonly = True,
        tags = ["manual"],
        disable_acceleration = select({
            "//instrumentation:disable_emulator_acceleration_config": True,
            "//conditions:default": False,
        }),
    )

def create_runnable_emulator(name, avd_bundle):
    _create_runnable_emulator(
        name = name,
        avd_bundle = avd_bundle,
        testonly = True,
        tags = ["manual"],
        disable_rendering = select({
            "//instrumentation:disable_emulator_rendering_config": True,
            "//conditions:default": False,
        }),
        disable_acceleration = select({
            "//instrumentation:disable_emulator_acceleration_config": True,
            "//conditions:default": False,
        }),
        disable_fastboot = select({
            "//instrumentation:disable_emulator_fastboot_config": True,
            "//conditions:default": False,
        }),
    )
