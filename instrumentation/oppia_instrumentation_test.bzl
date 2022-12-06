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
    arguments.extend(["--reuse-policy", "create_new"])
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

# TODO: Introduce a coordinator of some kind to share test loads across a prefixed number of emulators. Though, it might actually make more sense just to start one emulator per test, instead.
def _create_runnable_instrumentation_test_impl(ctx):
    test_binary_file = ctx.file.test_binary
    instrumented_binary_file = ctx.file.instrumented_binary
    run_instrumentation_test_shell = ctx.actions.declare_file("run_instrumentation_test.sh")
    avd_bundle = ctx.file.avd_bundle
    avd_bundle_file_name = avd_bundle.basename
    avd_name = avd_bundle_file_name[:avd_bundle_file_name.index(".")]

    target_system_image_files = [
        file
        for target in ctx.attr.avd_bundle[_AvdInfo].target_system_image_files.to_list()
        for file in target.files.to_list()
    ]

    emulator_runtime_directory = ctx.actions.declare_directory(
        "%s_emulator_runtime" % ctx.label.name,
    )
    ctx.actions.run_shell(
        outputs = [emulator_runtime_directory],
        command = "mkdir -p %s" % emulator_runtime_directory.path,
    )

    run_emulator_args = [ctx.executable._run_emulator_tool.short_path]
    run_emulator_args.extend(["--avd-bundle-path", avd_bundle.short_path])
    run_emulator_args.extend(["--working-path", emulator_runtime_directory.short_path])
    run_emulator_args.extend([
        "--system-image-paths",
        ",".join([file.path for file in target_system_image_files]),
    ])
    run_emulator_args.extend(["--emulator-binary-path", ctx.file._emulator_tool.short_path])
    run_emulator_args.extend(["--adb-binary-path", ctx.file._adb_tool.short_path])
    run_emulator_args.extend(["--reuse-policy", "join_existing"])
    run_emulator_args.append("--disable-rendering")  # Can't open windows within builds.
    if ctx.attr.disable_acceleration:
        run_emulator_args.append("--disable-acceleration")
    if ctx.attr.disable_fastboot:
        run_emulator_args.append("--disable-fastboot")

    run_test_args = [ctx.executable._run_instrumentation_test_tool.short_path]
    run_test_args.extend(["--adb-binary-path", ctx.file._adb_tool.short_path])
    run_test_args.extend(["--test-apk-path", test_binary_file.short_path])
    run_test_args.extend(["--instrumented-binary-apk-path", instrumented_binary_file.short_path])
    run_test_args.extend(["--test-class", ctx.attr.test_class])
    run_test_args.extend(["--avd-name", avd_name])
    run_test_args.extend([
        "--test-services-apk-path",
        ctx.file._androidx_test_services_test_services_apk.short_path,
    ])
    run_test_args.extend([
        "--test-orchestrator-apk-path",
        ctx.file._androidx_test_orchestrator_apk.short_path,
    ])

    ctx.actions.write(
        output = run_instrumentation_test_shell,
        content = """
        #!/bin/sh
        # Start emulator (if one isn't already available).
        {0}

        # Start the test.
        {1}
        """.format(" ".join(run_emulator_args), " ".join(run_test_args)),
        is_executable = True,
    )

    # Reference for including necessary runfiles for Java:
    # https://github.com/bazelbuild/bazel/issues/487#issuecomment-178119424.
    runfiles = ctx.runfiles(
        files = [
            test_binary_file,
            instrumented_binary_file,
            avd_bundle,
            emulator_runtime_directory,
            ctx.executable._run_instrumentation_test_tool,
            ctx.file._adb_tool,
            ctx.file._androidx_test_services_test_services_apk,
            ctx.file._androidx_test_orchestrator_apk,
            ctx.executable._run_emulator_tool,
            ctx.file._emulator_tool,
        ] + target_system_image_files,
    ).merge(
        ctx.attr._run_instrumentation_test_tool.default_runfiles,
    ).merge(ctx.attr._run_emulator_tool.default_runfiles)
    return DefaultInfo(
        executable = run_instrumentation_test_shell,
        runfiles = runfiles,
    )

def _generate_instrumentation_manifest_impl(ctx):
    instrumented_binary_file = ctx.file.instrumented_binary

    output_file = ctx.actions.declare_file(
        "%sInstrumentationTestBinaryManifest.xml" % ctx.attr.name,
    )

    arguments = ctx.actions.args()
    arguments.add(instrumented_binary_file)
    arguments.add(output_file)

    ctx.actions.run(
        outputs = [output_file],
        inputs = [instrumented_binary_file],
        tools = [ctx.executable._run_instrumentation_test_tool],
        executable = ctx.executable._run_instrumentation_test_tool.path,
        arguments = [arguments],
        mnemonic = "GenerateInstrumentationManifest",
        progress_message = "Generating instrumentation test binary manifest",
    )

    # Reference for including necessary runfiles for Java:
    # https://github.com/bazelbuild/bazel/issues/487#issuecomment-178119424.
    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(
            files = [output_file, instrumented_binary_file],
        ).merge(ctx.attr._run_instrumentation_test_tool.default_runfiles),
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

_create_runnable_instrumentation_test = rule(
    attrs = {
        "test_binary": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "instrumented_binary": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "avd_bundle": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "test_class": attr.string(
            mandatory = True,
        ),
        "disable_acceleration": attr.bool(),
        "disable_fastboot": attr.bool(),
        "_androidx_test_orchestrator_apk": attr.label(
            allow_single_file = True,
            default = "//third_party:androidx_test_orchestrator_apk",
        ),
        "_androidx_test_services_test_services_apk": attr.label(
            allow_single_file = True,
            default = "//third_party:androidx_test_services_test-services_apk",
        ),
        "_run_instrumentation_test_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//scripts:run_instrumentation_test",
        ),
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
    test = True,
    implementation = _create_runnable_instrumentation_test_impl,
)

_generate_instrumentation_manifest = rule(
    attrs = {
        "instrumented_binary": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "_run_instrumentation_test_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//scripts:generate_instrumentation_manifest",
        ),
    },
    implementation = _generate_instrumentation_manifest_impl,
)

def define_android_virtual_device(
        name,
        target_system_image_files,
        hardware_profiles_proto,
        hardware_profile,
        **kwargs):
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
        **kwargs
    )

def create_runnable_emulator(name, avd_bundle, **kwargs):
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
        **kwargs
    )

# TODO: Mention in PR the approach for reusing existing emulators. This makes the implementation
# easier since Bazel-managed AVD files means we can't reuse AVD dirs across multiple emulators, and
# unpacking is expensive (so we don't want to run multiple emulators for the same AVD) and there are
# a limited number of available ADB ports. However, this makes test coordination tricky as tests
# will need to block on an open emulator if an existing test is running. Long-term, we may want to
# consider introducing some sort of coordinator for better resource utilization & load-balancing,
# though this will probably be quite difficult to do as it'd require a server separate from Bazel to
# be kicked off to manage emulator sessions & AVD resources, and requests coming into to run tests.
def create_runnable_instrumentation_test(
        name,
        test_binary_target,
        instrumented_binary_target,
        test_class,
        avd_bundle,
        size = "enormous",
        **kwargs):
    _create_runnable_instrumentation_test(
        name = name,
        test_binary = "%s.apk" % test_binary_target,
        instrumented_binary = "%s.apk" % instrumented_binary_target,
        test_class = test_class,
        avd_bundle = avd_bundle,
        disable_acceleration = select({
            "//instrumentation:disable_emulator_acceleration_config": True,
            "//conditions:default": False,
        }),
        disable_fastboot = select({
            "//instrumentation:disable_emulator_fastboot_config": True,
            "//conditions:default": False,
        }),
        size = size,
        **kwargs
    )

def generate_instrumentation_manifest(name, instrumented_binary_target, **kwargs):
    _generate_instrumentation_manifest(
        name = name,
        instrumented_binary = "%s.apk" % instrumented_binary_target,
        **kwargs
    )
    return name
