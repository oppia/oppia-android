"""
This file lists and imports all external dependencies needed to build Oppia Android.
"""

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")
load("//:build_vars.bzl", "BUILD_SDK_VERSION", "BUILD_TOOLS_VERSION")
load("//third_party:versions.bzl", "HTTP_DEPENDENCY_VERSIONS", "MAVEN_REPOSITORIES", "get_maven_dependencies")

http_archive(
    name = "rules_cc",
    sha256 = "65b67b81c6da378f136cc7e7e14ee08d5b9375973427eceb8c773a4f69fa7e49",
    strip_prefix = "rules_cc-0.0.10",
    url = "https://github.com/bazelbuild/rules_cc/releases/download/0.0.10/rules_cc-0.0.10.tar.gz",
)

load("@rules_cc//cc:repositories.bzl", "rules_cc_dependencies")

rules_cc_dependencies()

http_archive(
    name = "rules_java",
    sha256 = "b6c6d92ca9dbb77de31fb6c6a794d20427072663ce41c2b047902ffcc123e3ef",
    url = "https://github.com/bazelbuild/rules_java/releases/download/8.13.0/rules_java-8.13.0.tar.gz",
)

load("@rules_java//java:rules_java_deps.bzl", "rules_java_dependencies")

rules_java_dependencies()

load("@rules_java//java:repositories.bzl", "rules_java_toolchains")

rules_java_toolchains()

http_archive(
    name = "proto_bazel_features",
    sha256 = "af3d4fb1cf4f25942cb4a933b1ad93a0ea9fe9ee70c2af7f369fb72a67c266e5",
    strip_prefix = "bazel_features-1.21.0",
    url = "https://github.com/bazel-contrib/bazel_features/releases/download/v1.21.0/bazel_features-v1.21.0.tar.gz",
)

load("@proto_bazel_features//:deps.bzl", "bazel_features_deps")

bazel_features_deps()

# NOTE: compatibility_proxy_repo is gone from rules_cc 0.0.10+? Or maybe it's still needed?
# I won't load it and see if it works.

# Oppia's backend proto API definitions.
git_repository(
    name = "oppia_proto_api",
    commit = HTTP_DEPENDENCY_VERSIONS["oppia_proto_api"]["version"],
    remote = "https://github.com/oppia/oppia-proto-api",
    shallow_since = "1775968956 +0000",
)

http_archive(
    name = "com_google_protobuf",
    patch_cmds = [
        "sed -i 's/name = \"lite\",/name = \"lite\", javacopts = [\"-nowarn\", \"-Xlint:none\"],/g' java/core/BUILD.bazel",
        "sed -i 's/name = \"core\",/name = \"core\", javacopts = [\"-nowarn\", \"-Xlint:none\"],/g' java/core/BUILD.bazel",
    ],
    sha256 = "b1e70c5e933400d11100f757270e4c6c0e8fba10f3c54f593e82729a43a0e1c1",
    strip_prefix = "protobuf-27.1",
    urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v27.1/protobuf-27.1.tar.gz"],
)

load("@oppia_proto_api//repo:deps.bzl", "initializeDepsForWorkspace")

# Declare rules_kotlin before initializeDepsForWorkspace to override the version
# pulled in by protobuf_deps.

http_archive(
    name = "io_bazel_rules_kotlin",
    patch_cmds = [
        "sed -i 's/_ANDROID_SDK_JAR = \"%s\" % Label(\"\\/\\/third_party:android_sdk\")/_ANDROID_SDK_JAR = \"@\\/\\/third_party:android_sdk_jar_only\"/g' kotlin/internal/jvm/android.bzl",
        "sed -i 's/enable_data_binding = enable_data_binding,/enable_data_binding = enable_data_binding, javacopts = kwargs.pop(\"javacopts\", \\[\\]) + \\[\"-Aandroid.databinding.useAndroidX=true\"\\],/g' kotlin/internal/jvm/android.bzl",
    ],
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_kotlin"]["sha"],
    url = "https://github.com/bazelbuild/rules_kotlin/releases/download/v{0}/rules_kotlin-v{0}.tar.gz".format(HTTP_DEPENDENCY_VERSIONS["rules_kotlin"]["version"]),
)

initializeDepsForWorkspace()

load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")
load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")
load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")
load("@rules_buf//buf:repositories.bzl", "rules_buf_dependencies", "rules_buf_toolchains")

go_rules_dependencies()

go_register_toolchains(version = "1.24.12")

gazelle_dependencies()

rules_buf_dependencies()

rules_buf_toolchains(version = "v1.27.1")

protobuf_deps()

# rules_android setup

http_archive(
    name = "rules_android",
    patch_args = ["-p1"],
    patch_cmds = [
        "sed -i 's/args.add(\"-useAndroidX\", \"false\")/args.add(\"-useAndroidX\", \"true\")/g' rules/data_binding.bzl",
        "sed -i 's/PROTOBUF_VERSION = \"33.4\"/PROTOBUF_VERSION = \"27.1\"/g' prereqs.bzl",
        "sed -i 's/4\\.33\\.4/4.29.0-RC2/g' defs.bzl",
        "sed -i 's/\"org\\/mozilla\",/\"org\\/mozilla\", \"kotlin\\/\", \"kotlinx\\/\", \"com\\/beust\\/jcommander\\/\", \"com\\/google\\/gson\\/\", \"javax\\/xml\\/bind\\/\", \"javax\\/activation\\/\", \"com\\/sun\\/xml\\/\", \"org\\/glassfish\\/\", \"com\\/sun\\/istack\\/\", \"com\\/google\\/common\\/\", \"androidx\\/databinding\\/\", \"com\\/android\\/tools\\/build\\/jetifier\\/\", \"default.config\", \"default.generated.config\",/g' src/tools/java/com/google/devtools/build/android/BUILD",
        "sed -i 's/\"@androidsdk\\/\\/:aapt2\"/\"@androidsdk\\/\\/:aapt2_binary\"/g' toolchains/android/BUILD",
        "sed -i 's/android.databinding.BindingBuildInfo/androidx.databinding.BindingBuildInfo/g' rules/data_binding_annotation_template.txt",
        "sed -i 's/package android.databinding.layouts;/package androidx.databinding.layouts;/g' rules/data_binding_annotation_template.txt",
        "sed -i 's/\"\\/android\\/databinding\\/layouts\\/DataBindingInfo.java\"/\"\\/androidx\\/databinding\\/layouts\\/DataBindingInfo.java\"/g' rules/data_binding.bzl",
        "sed -i 's/\",\"\\.join(deps_pkgs)/\",\"\\.join(deps_pkgs \\+ \\[\"org.oppia.android.app.databinding.adapters-\", \"org.oppia.android.app.customview.interaction-\", \"androidx.databinding.adapters-\", \"androidx.databinding.library.baseAdapters-\"\\])/g' rules/data_binding.bzl",
        "echo '<?xml version=\"1.0\" encoding=\"utf-8\"?><api></api>' > api-versions.xml",
        "zip api-versions.jar api-versions.xml",
        "echo 'java_import(name = \"api_versions_import\", jars = [\"api-versions.jar\"], visibility = [\"//visibility:public\"])' >> BUILD",
        "sed -i '/artifacts = \\[/a \\            \"com.squareup:javapoet:1.13.0\",' defs.bzl",
        "python3 -c \"import re; content = open('tools/android/BUILD').read(); content = re.sub(r'name = \\\"compiler_annotation_processor\\\"[\\\\s\\\\S]*?deps = \\\\[[\\\\s\\\\S]*?\\\\],', 'name = \\\"compiler_annotation_processor\\\",\\n    generates_api = True,\\n    data = [\\\"@androidsdk//:sdk\\\"],\\n    processor_class = \\\"android.databinding.annotationprocessor.ProcessDataBinding\\\",\\n    visibility = [\\\"//visibility:public\\\"],\\n    deps = [\\n        \\\"//src/tools/java/com/google/devtools/build/android:databinding_exec_jar\\\",\\n        \\\"//:api_versions_import\\\",\\n    ],', content); content = re.sub(r'name = \\\"databinding_exec\\\"[\\\\s\\\\S]*?runtime_deps = \\\\[[\\\\s\\\\S]*?\\\\],', 'name = \\\"databinding_exec\\\",\\n    main_class = \\\"android.databinding.AndroidDataBinding\\\",\\n    visibility = [\\\"//visibility:public\\\"],\\n    runtime_deps = [\\n        \\\"//src/tools/java/com/google/devtools/build/android:databinding_exec_jar\\\",\\n        \\\"//:api_versions_import\\\",\\n        \\\"@rules_android_maven//:com_squareup_javapoet\\\",\\n    ],', content); open('tools/android/BUILD', 'w').write(content)\"",
        "sed -i 's/if not acls\\.use_r8(label) and _generate_proguard_outputs:/if _generate_proguard_outputs:\\n        if proguard_generate_mapping:\\n            outputs[\"proguard_map\"] = \"%{name}_proguard.map\"\\n    if not acls.use_r8(label) and _generate_proguard_outputs:/g' rules/android_binary/rule.bzl",
        "sed -i 's/ctx\\.actions\\.declare_file(ctx\\.label\\.name \\+ \"_proguard\\.map\")/getattr(ctx.outputs, \"proguard_map\", None) or ctx.actions.declare_file(ctx.label.name + \"_proguard.map\")/g' rules/android_binary/r8.bzl",
        "sed -i 's/sdkDir=\\/not\\/used/sdkDir=\\/tmp\\/androidsdk/g' rules/data_binding.bzl",
        "sed -i 's/%s-br\\.bin/%s--br.bin/g' rules/data_binding.bzl",
        "sed -i 's/%s-setter_store\\.json/%s--setter_store.json/g' rules/data_binding.bzl",
        "sed -i 's/%s-layoutinfo\\.bin/%s--layoutinfo.bin/g' rules/data_binding.bzl",
    ],
    patches = ["//third_party:rules_android_r8_min_sdk.patch"],
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_android"]["sha"],
    strip_prefix = "rules_android-%s" % HTTP_DEPENDENCY_VERSIONS["rules_android"]["version"],
    urls = ["https://github.com/bazelbuild/rules_android/releases/download/v{0}/rules_android-v{0}.tar.gz".format(HTTP_DEPENDENCY_VERSIONS["rules_android"]["version"])],
)

load("@rules_android//:prereqs.bzl", "rules_android_prereqs")

rules_android_prereqs()

load("@rules_android//:defs.bzl", "rules_android_workspace")

rules_android_workspace()

load("@rules_android//android:rules.bzl", "android_sdk_repository")

android_sdk_repository(
    name = "androidsdk",
    api_level = BUILD_SDK_VERSION,
    build_tools_version = BUILD_TOOLS_VERSION,
)

http_archive(
    name = "rules_license",
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_license"]["sha"],
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_license/releases/download/{0}/rules_license-{0}.tar.gz".format(HTTP_DEPENDENCY_VERSIONS["rules_license"]["version"]),
        "https://github.com/bazelbuild/rules_license/releases/download/{0}/rules_license-{0}.tar.gz".format(HTTP_DEPENDENCY_VERSIONS["rules_license"]["version"]),
    ],
)

# The rules_java contains the java_lite_proto_library rule used for Java generated protos.

http_archive(
    name = "zlib",
    build_file = "@com_google_protobuf//:third_party/zlib.BUILD",
    sha256 = HTTP_DEPENDENCY_VERSIONS["zlib"]["sha"],
    strip_prefix = "zlib-" + HTTP_DEPENDENCY_VERSIONS["zlib"]["version"],
    urls = [
        "https://github.com/madler/zlib/releases/download/v{0}/zlib-{0}.tar.gz".format(HTTP_DEPENDENCY_VERSIONS["zlib"]["version"]),
        "http://zlib.net/fossils/zlib-%s.tar.gz" % HTTP_DEPENDENCY_VERSIONS["zlib"]["version"],
    ],
)

# Add support for JVM rules: https://github.com/bazelbuild/rules_jvm_external
http_archive(
    name = "rules_jvm_external",
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_jvm"]["sha"],
    strip_prefix = "rules_jvm_external-%s" % HTTP_DEPENDENCY_VERSIONS["rules_jvm"]["version"],
    url = "https://github.com/bazelbuild/rules_jvm_external/releases/download/{0}/rules_jvm_external-{0}.tar.gz".format(HTTP_DEPENDENCY_VERSIONS["rules_jvm"]["version"]),
)

http_archive(
    name = "bazel_features",
    sha256 = HTTP_DEPENDENCY_VERSIONS["bazel_features"]["sha"],
    strip_prefix = "bazel_features-%s" % HTTP_DEPENDENCY_VERSIONS["bazel_features"]["version"],
    url = "https://github.com/bazel-contrib/bazel_features/releases/download/v{0}/bazel_features-v{0}.tar.gz".format(HTTP_DEPENDENCY_VERSIONS["bazel_features"]["version"]),
)

load("@bazel_features//:deps.bzl", "bazel_features_deps")

bazel_features_deps()

# Add support for Kotlin: https://github.com/bazelbuild/rules_kotlin.
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories", "kotlinc_version")

kotlin_repositories()

register_toolchains("//tools/kotlin:kotlin_16_jdk9_toolchain")

# The proto_compiler and proto_java_toolchain bindings load the protos rules needed for generating
# protos while helping us avoid the unnecessary compilation of protoc. Referecences:
# - https://github.com/google/startup-os/blob/5f30a62/WORKSPACE#L179-L187
# - https://github.com/bazelbuild/bazel/issues/7095

bind(
    name = "proto_compiler",
    actual = "//tools:protoc",
)

bind(
    name = "proto_java_toolchain",
    actual = "//tools:java_toolchain",
)

# The rules_proto contains the proto_library rule used for proto generation.

http_archive(
    name = "rules_proto",
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_proto"]["sha"],
    strip_prefix = "rules_proto-%s" % HTTP_DEPENDENCY_VERSIONS["rules_proto"]["version"],
    url = "https://github.com/bazelbuild/rules_proto/releases/download/{0}/rules_proto-{0}.tar.gz".format(HTTP_DEPENDENCY_VERSIONS["rules_proto"]["version"]),
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies")

rules_proto_dependencies()

load("@rules_proto//proto:toolchains.bzl", "rules_proto_toolchains")

rules_proto_toolchains()

# Add support for Dagger
http_archive(
    name = "dagger",
    patch_cmds = [
        "sed -i 's/\"%s\\/\\/:com_google_dagger_dagger_compiler\" % repo_name,/\"%s\\/\\/:com_google_dagger_dagger_compiler\" % repo_name, \"@\\/\\/third_party:com_google_guava_guava_jre\",/g' workspace_defs.bzl",
    ],
    sha256 = HTTP_DEPENDENCY_VERSIONS["dagger"]["sha"],
    strip_prefix = "dagger-dagger-%s" % HTTP_DEPENDENCY_VERSIONS["dagger"]["version"],
    urls = ["https://github.com/google/dagger/archive/dagger-%s.zip" % HTTP_DEPENDENCY_VERSIONS["dagger"]["version"]],
)

load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")

# Add support for Robolectric: https://github.com/robolectric/robolectric-bazel
http_archive(
    name = "robolectric",
    patch_cmds = ["exit 1"],
    sha256 = HTTP_DEPENDENCY_VERSIONS["robolectric"]["sha"],
    strip_prefix = "robolectric-bazel-%s" % HTTP_DEPENDENCY_VERSIONS["robolectric"]["version"],
    urls = ["https://github.com/robolectric/robolectric-bazel/archive/%s.tar.gz" % HTTP_DEPENDENCY_VERSIONS["robolectric"]["version"]],
)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")

robolectric_repositories()

# Add support for Firebase Crashlytics
git_repository(
    name = "tools_android",
    commit = "00e6f4b7bdd75911e33c618a9bc57bab7a6e8930",
    patch_cmds = [
        "find . -name 'BUILD*' -exec sed -i '1s|^|load(\"@rules_android//rules:rules.bzl\", \"android_library\", \"aar_import\", \"android_binary\")\\n|' {} +",
        "find . -name '*.bzl' -exec sed -i 's/native\\.android_library/android_library/g' {} +",
        "find . -name '*.bzl' -exec sed -i '1s|^|load(\"@rules_android//rules:rules.bzl\", \"android_library\")\\n|' {} +",
    ],
    remote = "https://github.com/bazelbuild/tools_android",
    shallow_since = "1594238320 -0400",
)

load("@tools_android//tools/googleservices:defs.bzl", "google_services_workspace_dependencies")

google_services_workspace_dependencies()

# A custom version of Android SVG is needed since custom changes needed to be added to the library
# to correctly size in-line SVGs (such as those needed for LaTeX-based math expressions).
git_repository(
    name = "androidsvg",
    commit = "5bc9c7553e94c3476e8ea32baea3c77567228fcd",
    patch_cmds = [
        "find . -name 'BUILD*' -exec sed -i '1s|^|load(\"@rules_android//rules:rules.bzl\", \"android_library\", \"aar_import\", \"android_binary\")\\n|' {} +",
        "sed -i 's/fontVariantSmallCaps == Boolean.TRUE/Boolean.TRUE.equals(fontVariantSmallCaps)/g' androidsvg/src/main/java/com/caverock/androidsvg/parser/ParserHelper.java",
    ],
    remote = "https://github.com/oppia/androidsvg",
    shallow_since = "1686304726 -0700",
)

git_repository(
    name = "android-spotlight",
    commit = "cc23499d37dc8533a2876e45b5063e981a4583f4",
    patch_cmds = [
        "find . -name 'BUILD*' -exec sed -i '1s|^|load(\"@rules_android//rules:rules.bzl\", \"android_library\", \"aar_import\", \"android_binary\")\\n|' {} +",
    ],
    remote = "https://github.com/oppia/android-spotlight",
    shallow_since = "1680147372 -0700",
)

# A custom fork of KotliTeX that removes resources artifacts that break the build, and updates the
# min target SDK version to be compatible with Oppia.
git_repository(
    name = "kotlitex",
    commit = "ccdf4170817fa3b48b8e1e452772dd58ecb71cf2",
    patch_cmds = [
        "find . -name 'BUILD*' -exec sed -i '1s|^|load(\"@rules_android//rules:rules.bzl\", \"android_library\", \"aar_import\", \"android_binary\")\\n|' {} +",
        "find . -name 'BUILD*' -exec sed -i 's/exports_manifest = True/exports_manifest = 1/g' {} +",
    ],
    remote = "https://github.com/oppia/kotlitex",
    shallow_since = "1679426649 -0700",
)

git_repository(
    name = "archive_patcher",
    commit = "d1c18b0035d5f669ddaefadade49cae0748f9df2",
    patch_cmds = [
        "find . -name 'BUILD*' -exec sed -i '1s|^|load(\"@rules_android//rules:rules.bzl\", \"android_library\", \"aar_import\", \"android_binary\")\\n|' {} +",
    ],
    remote = "https://github.com/oppia/archive-patcher",
    shallow_since = "1642022460 -0800",
)

bind(
    name = "databinding_annotation_processor",
    actual = "//tools/android:compiler_annotation_processor",
)

http_archive(
    name = "protobuf_tools",
    sha256 = HTTP_DEPENDENCY_VERSIONS["protobuf_tools"]["sha"],
    strip_prefix = "protobuf-%s" % HTTP_DEPENDENCY_VERSIONS["protobuf_tools"]["version"],
    urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v{0}/protobuf-all-{0}.zip".format(HTTP_DEPENDENCY_VERSIONS["protobuf_tools"]["version"])],
)

# Bind python headers to satisfy a transitive dependency in order to enable pre-fetching support.
# This is done such that it should satisfiy the requirement for pre-fetching but cause an actual
# build failure for any real dependencies on the target.
bind(
    name = "python_headers",
    actual = "@bazel_tools//tools/cpp:malloc",
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

ATS_TAG = "1edfdab3134a7f01b37afabd3eebfd2c5bb05151"

ATS_SHA256 = "dcd1ff76aef1a26329d77863972780c8fe1fc8ff625747342239f0489c2837ec"

http_archive(
    name = "android_test_support",
    sha256 = ATS_SHA256,
    strip_prefix = "android-test-%s" % ATS_TAG,
    urls = ["https://github.com/android/android-test/archive/%s.tar.gz" % ATS_TAG],
)

load("@android_test_support//:repo.bzl", "android_test_repositories")

android_test_repositories()

# Android bundle tool.
http_jar(
    name = "android_bundletool",
    sha256 = HTTP_DEPENDENCY_VERSIONS["android_bundletool"]["sha"],
    url = "https://github.com/google/bundletool/releases/download/{0}/bundletool-all-{0}.jar".format(HTTP_DEPENDENCY_VERSIONS["android_bundletool"]["version"]),
)

# Note to developers: new dependencies should be added to //third_party:versions.bzl, not here.
maven_install(
    aar_import_bzl_label = "@rules_android//android:rules.bzl",
    artifacts = DAGGER_ARTIFACTS + get_maven_dependencies(),
    duplicate_version_warning = "error",
    fail_if_repin_required = True,
    maven_install_json = "//third_party:maven_install.json",
    override_targets = {
        "com.google.guava:guava": "@//third_party:com_google_guava_guava",
    },
    repositories = DAGGER_REPOSITORIES + MAVEN_REPOSITORIES,
    strict_visibility = True,
    use_starlark_android_rules = True,
)

load("@maven//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

[
    http_jar(
        name = "guava_%s" % guava_type,
        sha256 = HTTP_DEPENDENCY_VERSIONS["guava_%s" % guava_type]["sha"],
        urls = [
            "{0}/com/google/guava/guava/{1}-{2}/guava-{1}-{2}.jar".format(
                url_base,
                HTTP_DEPENDENCY_VERSIONS["guava_%s" % guava_type]["version"],
                guava_type,
            )
            for url_base in DAGGER_REPOSITORIES + MAVEN_REPOSITORIES
        ],
    )
    for guava_type in [
        "android",
        "jre",
    ]
]

register_toolchains("@rules_android//toolchains/android:all")

register_toolchains("@rules_android//toolchains/android_sdk:all")
