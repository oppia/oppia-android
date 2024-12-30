"""
Macros & definitions corresponding to Oppia binary build flavors.
"""

load("//:oppia_android_application.bzl", "declare_deployable_application", "oppia_android_application")
load("//:version.bzl", "MAJOR_VERSION", "MINOR_VERSION", "OPPIA_ALPHA_KITKAT_VERSION_CODE", "OPPIA_ALPHA_VERSION_CODE", "OPPIA_BETA_VERSION_CODE", "OPPIA_DEV_KITKAT_VERSION_CODE", "OPPIA_DEV_VERSION_CODE", "OPPIA_GA_VERSION_CODE")

# Defines the list of flavors available to build the Oppia app in. Note to developers: this list
# should be ordered by the development pipeline (i.e. features go through dev first, then other
# flavors as they mature).
AVAILABLE_FLAVORS = [
    "dev",
    "dev_kitkat",
    "alpha",
    "alpha_kitkat",
    "beta",
    "ga",
]

# This file contains the list of classes that must be in the main dex list for the legacy multidex
# build used on KitKat devices. Generally, this is the main application class is needed so that it
# can load multidex support, plus any dependencies needed by that pipeline.
_MAIN_DEX_LIST_TARGET_KITKAT = "//:config/kitkat_main_dex_class_list.txt"

# keep sorted
_PRODUCTION_PROGUARD_SPECS = [
    "config/proguard/android-proguard-rules.pro",
    "config/proguard/androidx-proguard-rules.pro",
    "config/proguard/firebase-components-proguard-rules.pro",
    "config/proguard/glide-proguard-rules.pro",
    "config/proguard/google-play-services-proguard-rules.pro",
    "config/proguard/guava-proguard-rules.pro",
    "config/proguard/kotlin-proguard-rules.pro",
    "config/proguard/kotlinpoet-javapoet-proguard-rules.pro",
    "config/proguard/material-proguard-rules.pro",
    "config/proguard/moshi-proguard-rules.pro",
    "config/proguard/okhttp-proguard-rules.pro",
    "config/proguard/oppia-prod-proguard-rules.pro",
    "config/proguard/protobuf-proguard-rules.pro",
]

# Note to developers: keys of this dict should follow the order of AVAILABLE_FLAVORS.
_FLAVOR_METADATA = {
    "dev": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 21,
        "target_sdk_version": 34,
        "multidex": "native",
        "proguard_specs": [],  # Developer builds are not optimized.
        "production_release": False,
        "deps": [
            "//app/src/main/java/org/oppia/android/app/application/dev:developer_application",
            "//config/src/java/org/oppia/android/config:all_languages_config",
        ],
        "version_code": OPPIA_DEV_VERSION_CODE,
        "application_class": ".app.application.dev.DeveloperOppiaApplication",
    },
    "dev_kitkat": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 21,
        "target_sdk_version": 34,
        "multidex": "manual_main_dex",
        "main_dex_list": _MAIN_DEX_LIST_TARGET_KITKAT,
        "proguard_specs": [],  # Developer builds are not optimized.
        "production_release": False,
        "deps": [
            "//app/src/main/java/org/oppia/android/app/application/dev:developer_application",
            "//config/src/java/org/oppia/android/config:all_languages_config",
        ],
        "version_code": OPPIA_DEV_KITKAT_VERSION_CODE,
        "application_class": ".app.application.dev.DeveloperOppiaApplication",
    },
    "alpha": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 21,
        "target_sdk_version": 34,
        "multidex": "native",
        "proguard_specs": _PRODUCTION_PROGUARD_SPECS,
        "production_release": True,
        "deps": [
            "//app/src/main/java/org/oppia/android/app/application/alpha:alpha_application",
            "//config/src/java/org/oppia/android/config:all_languages_config",
        ],
        "version_code": OPPIA_ALPHA_VERSION_CODE,
        "application_class": ".app.application.alpha.AlphaOppiaApplication",
    },
    "alpha_kitkat": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 21,
        "target_sdk_version": 34,
        "multidex": "manual_main_dex",
        "main_dex_list": _MAIN_DEX_LIST_TARGET_KITKAT,
        "proguard_specs": [],
        "production_release": True,
        "deps": [
            "//app/src/main/java/org/oppia/android/app/application/alpha:alpha_application",
            "//config/src/java/org/oppia/android/config:all_languages_config",
        ],
        "version_code": OPPIA_ALPHA_KITKAT_VERSION_CODE,
        "application_class": ".app.application.alpha.AlphaOppiaApplication",
    },
    "beta": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 21,
        "target_sdk_version": 34,
        "multidex": "native",
        "proguard_specs": _PRODUCTION_PROGUARD_SPECS,
        "production_release": True,
        "deps": [
            "//app/src/main/java/org/oppia/android/app/application/beta:beta_application",
            "//config/src/java/org/oppia/android/config:production_languages_config",
        ],
        "version_code": OPPIA_BETA_VERSION_CODE,
        "application_class": ".app.application.beta.BetaOppiaApplication",
    },
    "ga": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 21,
        "target_sdk_version": 34,
        "multidex": "native",
        "proguard_specs": _PRODUCTION_PROGUARD_SPECS,
        "production_release": True,
        "deps": [
            "//app/src/main/java/org/oppia/android/app/application/ga:general_availability_application",
            "//config/src/java/org/oppia/android/config:production_languages_config",
        ],
        "version_code": OPPIA_GA_VERSION_CODE,
        "application_class": ".app.application.ga.GaOppiaApplication",
    },
}

def _transform_android_manifest_impl(ctx):
    input_file = ctx.attr.input_file.files.to_list()[0]
    output_file = ctx.outputs.output_file
    git_meta_dir = ctx.attr.git_meta_dir.files.to_list()[0]
    build_flavor = ctx.attr.build_flavor
    major_version = ctx.attr.major_version
    minor_version = ctx.attr.minor_version
    version_code = ctx.attr.version_code
    application_relative_qualified_class = ctx.attr.application_relative_qualified_class

    # See corresponding transformation script for details on the passed arguments.
    arguments = [
        ".",  # Working directory of the Bazel repository.
        input_file.path,  # Path to the source manifest.
        output_file.path,  # Path to the output manifest.
        build_flavor,
        "%s" % major_version,
        "%s" % minor_version,
        "%s" % version_code,
        "%s" % application_relative_qualified_class,
        "origin/develop",  # The base branch for computing the version name.
    ]

    # Reference: https://docs.bazel.build/versions/master/skylark/lib/actions.html#run.
    ctx.actions.run(
        outputs = [output_file],
        inputs = ctx.files.input_file + [git_meta_dir],
        tools = [ctx.executable._transform_android_manifest_tool],
        executable = ctx.executable._transform_android_manifest_tool.path,
        arguments = arguments,
        mnemonic = "TransformAndroidManifest",
        progress_message = "Transforming Android manifest",
    )
    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [output_file]),
    )

_transform_android_manifest = rule(
    attrs = {
        "input_file": attr.label(
            allow_files = True,
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
        "git_meta_dir": attr.label(
            allow_files = True,
            mandatory = True,
        ),
        "build_flavor": attr.string(mandatory = True),
        "major_version": attr.int(mandatory = True),
        "minor_version": attr.int(mandatory = True),
        "version_code": attr.int(mandatory = True),
        "application_relative_qualified_class": attr.string(mandatory = True),
        "_transform_android_manifest_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//scripts:transform_android_manifest",
        ),
    },
    implementation = _transform_android_manifest_impl,
)

def transform_android_manifest(
        name,
        input_file,
        output_file,
        build_flavor,
        major_version,
        minor_version,
        version_code,
        application_relative_qualified_class):
    """
    Generates a new transformation of the specified AndroidManifest.xml.

    The transformed version of the manifest include an explicitly specified version code and
    computed version name based on the specified major/minor version, flavor, and the most recent
    develop branch hash.

    Args:
        name: str. The name of this transformation target.
        input_file: target. The file target corresponding to the AndroidManifest.xml file to
            transform.
        output_file: str. The filename that should be generated as the transformed manifest.
        build_flavor: str. The specific release flavor of this build of the app.
        major_version: int. The major version of the app.
        minor_version: int. The minor version of the app.
        version_code: int. The version code of this flavor of the app.
        application_relative_qualified_class: String. The relatively qualified main application
            class of the app for this build flavor.
    """
    _transform_android_manifest(
        name = name,
        input_file = input_file,
        output_file = output_file,
        git_meta_dir = "//:.git",
        build_flavor = build_flavor,
        major_version = major_version,
        minor_version = minor_version,
        version_code = version_code,
        application_relative_qualified_class = application_relative_qualified_class,
    )

def define_oppia_aab_binary_flavor(flavor):
    """
    Defines a new flavor of the Oppia Android app.

    Flavors are defined through properties defined within _FLAVOR_METADATA.

    This will define two targets:
    - //:oppia_<flavor> (the AAB)
    - //:install_oppia_<flavor> (the installable binary target--see declare_deployable_application
      for details)

    Args:
        flavor: str. The name of the flavor of the app. Must correspond to an entry in
            AVAILABLE_FLAVORS.
    """
    transform_android_manifest(
        name = "oppia_%s_transformed_manifest" % flavor,
        application_relative_qualified_class = _FLAVOR_METADATA[flavor]["application_class"],
        input_file = _FLAVOR_METADATA[flavor]["manifest"],
        output_file = "AndroidManifest_transformed_%s.xml" % flavor,
        build_flavor = flavor,
        major_version = MAJOR_VERSION,
        minor_version = MINOR_VERSION,
        version_code = _FLAVOR_METADATA[flavor]["version_code"],
    )
    oppia_android_application(
        name = "oppia_%s" % flavor,
        custom_package = "org.oppia.android",
        testonly = not _FLAVOR_METADATA[flavor]["production_release"],
        enable_data_binding = True,
        config_file = "//:bundle_config.pb.json",
        manifest = ":AndroidManifest_transformed_%s.xml" % flavor,
        manifest_values = {
            "applicationId": "org.oppia.android",
            "minSdkVersion": "%d" % _FLAVOR_METADATA[flavor]["min_sdk_version"],
            "targetSdkVersion": "%d" % _FLAVOR_METADATA[flavor]["target_sdk_version"],
        },
        multidex = _FLAVOR_METADATA[flavor]["multidex"],
        main_dex_list = _FLAVOR_METADATA[flavor].get("main_dex_list"),
        proguard_generate_mapping = True if len(_FLAVOR_METADATA[flavor]["proguard_specs"]) != 0 else False,
        proguard_specs = _FLAVOR_METADATA[flavor]["proguard_specs"],
        shrink_resources = True if len(_FLAVOR_METADATA[flavor]["proguard_specs"]) != 0 else False,
        deps = _FLAVOR_METADATA[flavor]["deps"],
    )
    declare_deployable_application(
        name = "install_oppia_%s" % flavor,
        aab_target = ":oppia_%s" % flavor,
    )
