"""
Macros & definitions corresponding to Oppia binary build flavors.
"""

load("//:oppia_android_application.bzl", "generate_universal_apk", "oppia_android_application")
load("//:version.bzl", "MAJOR_VERSION", "MINOR_VERSION")

# Defines the list of flavors available to build the Oppia app in. Note to developers: this list
# should be ordered by the development pipeline (i.e. features go through dev first, then other
# flavors as they mature).
AVAILABLE_FLAVORS = [
    "dev",
    "alpha",
    "beta",
    "ga",
]

# keep sorted
_PRODUCTION_PROGUARD_SPECS = [
    "//config:proguard/android-proguard-rules.pro",
    "//config:proguard/androidx-proguard-rules.pro",
    "//config:proguard/firebase-components-proguard-rules.pro",
    "//config:proguard/glide-proguard-rules.pro",
    "//config:proguard/google-play-services-proguard-rules.pro",
    "//config:proguard/guava-proguard-rules.pro",
    "//config:proguard/kotlin-proguard-rules.pro",
    "//config:proguard/kotlinpoet-javapoet-proguard-rules.pro",
    "//config:proguard/material-proguard-rules.pro",
    "//config:proguard/moshi-proguard-rules.pro",
    "//config:proguard/okhttp-proguard-rules.pro",
    "//config:proguard/oppia-prod-proguard-rules.pro",
    "//config:proguard/protobuf-proguard-rules.pro",
]

# Note to developers: keys of this dict should follow the order of AVAILABLE_FLAVORS.
_FLAVOR_METADATA = {
    "dev": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 23,
        "target_sdk_version": 35,
        "multidex": "native",
        "proguard_specs": [],  # Developer builds are not optimized.
        "production_release": False,
        "enable_app_expiration": False,
        "deps": [
            "//app/src/main/java/org/oppia/android/app/application/dev:developer_application",
            "//config/src/java/org/oppia/android/config:all_languages_config",
        ],
        "application_class": ".app.application.dev.DeveloperOppiaApplication",
    },
    "alpha": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 23,
        "target_sdk_version": 35,
        "multidex": "native",
        "proguard_specs": _PRODUCTION_PROGUARD_SPECS,
        "production_release": True,
        "enable_app_expiration": True,
        "deps": [
            "//app/src/main/java/org/oppia/android/app/application/alpha:alpha_application",
            "//config/src/java/org/oppia/android/config:all_languages_config",
            "//config/src/java/org/oppia/android/config:alpha_feature_flags_override_config",
        ],
        "application_class": ".app.application.alpha.AlphaOppiaApplication",
    },
    "beta": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 23,
        "target_sdk_version": 35,
        "multidex": "native",
        "proguard_specs": _PRODUCTION_PROGUARD_SPECS,
        "production_release": True,
        "enable_app_expiration": True,
        "deps": [
            "//app/src/main/java/org/oppia/android/app/application/beta:beta_application",
            "//config/src/java/org/oppia/android/config:beta_feature_flags_override_config",
            "//config/src/java/org/oppia/android/config:production_languages_config",
        ],
        "application_class": ".app.application.beta.BetaOppiaApplication",
    },
    "ga": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 23,
        "target_sdk_version": 35,
        "multidex": "native",
        "proguard_specs": _PRODUCTION_PROGUARD_SPECS,
        "production_release": True,
        "enable_app_expiration": False,
        "deps": [
            "//app/src/main/java/org/oppia/android/app/application/ga:general_availability_application",
            "//config/src/java/org/oppia/android/config:ga_feature_flags_override_config",
            "//config/src/java/org/oppia/android/config:production_languages_config",
        ],
        "application_class": ".app.application.ga.GaOppiaApplication",
    },
}

def _transform_android_manifest_impl(ctx):
    input_file = ctx.attr.input_file.files.to_list()[0]
    output_file = ctx.outputs.output_file
    build_flavor = ctx.attr.build_flavor
    major_version = ctx.attr.major_version
    minor_version = ctx.attr.minor_version
    application_relative_qualified_class = ctx.attr.application_relative_qualified_class
    enable_firebase_analytics = ctx.attr.enable_firebase_analytics
    enable_app_expiration = ctx.attr.enable_app_expiration

    # See corresponding transformation script for details on the passed arguments.
    arguments = [
        ".",  # Working directory of the Bazel repository.
        input_file.path,  # Path to the source manifest.
        output_file.path,  # Path to the output manifest.
        build_flavor,
        "%s" % major_version,
        "%s" % minor_version,
        "%s" % application_relative_qualified_class,
        "true" if enable_firebase_analytics else "false",
        "true" if enable_app_expiration else "false",
    ]

    # Reference: https://docs.bazel.build/versions/master/skylark/lib/actions.html#run.
    ctx.actions.run(
        outputs = [output_file],
        inputs = [input_file],
        tools = [ctx.executable._transform_android_manifest_tool],
        executable = ctx.executable._transform_android_manifest_tool.path,
        arguments = arguments,
        mnemonic = "TransformAndroidManifest",
        progress_message = "Transforming Android manifest",
        execution_requirements = {
            "local": "1",  # Ensure that .git can be accessed locally by the script.
        },
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
        "build_flavor": attr.string(mandatory = True),
        "major_version": attr.int(mandatory = True),
        "minor_version": attr.int(mandatory = True),
        "application_relative_qualified_class": attr.string(mandatory = True),
        "enable_firebase_analytics": attr.bool(mandatory = True),
        "enable_app_expiration": attr.bool(mandatory = True),
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
        application_relative_qualified_class,
        enable_firebase_analytics,
        enable_app_expiration):
    """
    Generates a new transformation of the specified AndroidManifest.xml.

    The transformed version of the manifest include computed version code and
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
        application_relative_qualified_class: String. The relatively qualified main application
            class of the app for this build flavor.
        enable_firebase_analytics: bool. Whether to enable Firebase Analytics.
        enable_app_expiration: bool. Whether to enable app expiration.
    """
    _transform_android_manifest(
        name = name,
        input_file = input_file,
        output_file = output_file,
        build_flavor = build_flavor,
        major_version = major_version,
        minor_version = minor_version,
        application_relative_qualified_class = application_relative_qualified_class,
        enable_firebase_analytics = enable_firebase_analytics,
        enable_app_expiration = enable_app_expiration,
    )

def define_oppia_aab_binary_flavor(flavor):
    """
    Defines a new flavor of the Oppia Android app.

    Flavors are defined through properties defined within _FLAVOR_METADATA.

    This will define two targets:
    - //:oppia_<flavor> (the AAB)
    - //:oppia_<flavor>_universal_apk (the installable binary target--see generate_universal_apk
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
        enable_app_expiration = _FLAVOR_METADATA[flavor]["enable_app_expiration"],
        enable_firebase_analytics = select({
            "//config:firebase_analytics_enabled": True,
            "//conditions:default": False,
        }),
    )
    app_name = "oppia_%s" % flavor

    oppia_android_application(
        name = app_name,
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
        proguard_generate_mapping = True if len(_FLAVOR_METADATA[flavor]["proguard_specs"]) != 0 else False,
        proguard_specs = _FLAVOR_METADATA[flavor]["proguard_specs"],
        shrink_resources = True if len(_FLAVOR_METADATA[flavor]["proguard_specs"]) != 0 else False,
        production_release = _FLAVOR_METADATA[flavor]["production_release"],
        deps = _FLAVOR_METADATA[flavor]["deps"] + select({
            "//config:assets_type_alpha": ["//domain:domain_alpha_assets"],
            "//config:assets_type_prod": ["//domain:domain_prod_assets"],
            "//conditions:default": ["//domain:domain_dev_assets"],
        }),
    )

    generate_universal_apk(
        name = "oppia_%s_universal_apk" % flavor,
        aab_target = ":oppia_%s" % flavor,
    )
