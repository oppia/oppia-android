"""
Contains all of the HTTP archive/jar & Git repository dependencies that are directly required for
production & test builds of Oppia Android scripts. These are exposed via DIRECT_REMOTE_DEPENDENCIES.
"""

load(
    "//third_party/macros:direct_dep_defs.bzl",
    "EXPORT_TOOLCHAIN",
    "create_export_binary_details",
    "create_export_library_details",
    "create_git_repository_reference",
    "create_http_archive_reference",
    "create_http_file_reference",
    "create_http_jar_reference",
)

ANDROID_BUNDLE_TOOL_VERSION = "1.8.0"
ARCHIVE_PATCHER_VERSION_COMMIT = "50ca40a3de7983392a383ed3cc7b48e25f1b69b3"
RULES_BUF_VERSION = "0.1.1"
RULES_GO_VERSION = "0.39.1"
BUILD_TOOLS_VERSION = "4.2.2"
GO_VERSION = "1.20.2"
BUF_VERSION = "1.17.0"
KTLINT_VERSION = "0.37.1"
CHECKSTYLE_VERSION = "10.10.0"

DIRECT_REMOTE_DEPENDENCIES = [
    create_http_jar_reference(
        name = "android_bundletool",
        sha = "1e8430002c76f36ce2ddbac8aadfaf2a252a5ffbd534dab64bb255cda63db7ba",
        version = ANDROID_BUNDLE_TOOL_VERSION,
        url = "https://github.com/google/bundletool/releases/download/{0}/bundletool-all-{0}.jar",
        test_only = False,
        exports_details = [
            create_export_library_details(
                exposed_artifact_name = "android_bundletool",
                exportable_target = "jar",
                export_toolchain = EXPORT_TOOLCHAIN.ANDROID,
            ),
            create_export_binary_details(
                exposed_artifact_name = "android_bundletool_binary",
                main_class = "com.android.tools.build.bundletool.BundleToolMain",
                exportable_runtime_target = "jar",
            ),
        ],
    ),
    create_git_repository_reference(
        name = "archive_patcher",
        commit = ARCHIVE_PATCHER_VERSION_COMMIT,
        remote = "https://github.com/google/archive-patcher",
        test_only = False,
        build_file = "//scripts/third_party/versions/mods:BUILD.archive-patcher",
        export_details = create_export_library_details(
            exposed_artifact_name = "com_google_archivepatcher",
            exportable_target = ":tools",
            export_toolchain = EXPORT_TOOLCHAIN.ANDROID,
        ),
    ),
    create_http_archive_reference(
        name = "io_bazel_rules_go",
        sha = "6dc2da7ab4cf5d7bfc7c949776b1b7c733f05e56edc4bcd9022bb249d2e2a996",
        version = RULES_GO_VERSION,
        test_only = False,
        url = "https://github.com/bazelbuild/rules_go/releases/download/v{0}/rules_go-v{0}.zip",
    ),
    create_http_archive_reference(
        name = "com_github_bazelbuild_buildtools",
        sha = "ae34c344514e08c23e90da0e2d6cb700fcd28e80c02e23e4d5715dddcb42f7b3",
        version = BUILD_TOOLS_VERSION,
        test_only = False,
        url = "https://github.com/bazelbuild/buildtools/archive/refs/tags/{0}.tar.gz",
        strip_prefix_template = "buildtools-{0}",
    ),
    create_http_file_reference(
        name = "buf-darwin-arm64",
        sha = "69432401a4e1676755f8a246f82435c68b194255ede9a3497cf797ee6d96e18e",
        version = BUF_VERSION,
        test_only = False,
        url = "https://github.com/bufbuild/buf/releases/download/v{0}/buf-Darwin-arm64",
        executable = True,
    ),
    create_http_file_reference(
        name = "buf-darwin-x86_64",
        sha = "63d7cdf641a2c21705c065d6c9407225ef846a03be45cdb668ee5eb43990538b",
        version = BUF_VERSION,
        test_only = False,
        url = "https://github.com/bufbuild/buf/releases/download/v{0}/buf-Darwin-x86_64",
        executable = True,
    ),
    create_http_file_reference(
        name = "buf-linux-aarch64",
        sha = "6ae166bfcb3eae7642e98e439901b1b94295fc3c7f816d964645087f521fef5f",
        version = BUF_VERSION,
        test_only = False,
        url = "https://github.com/bufbuild/buf/releases/download/v{0}/buf-Linux-aarch64",
        executable = True,
    ),
    create_http_file_reference(
        name = "buf-linux-x86_64",
        sha = "a4b18f4e44fd918847e310b93ad94ea66913f2040956f856520b92f731e52d7f",
        version = BUF_VERSION,
        test_only = False,
        url = "https://github.com/bufbuild/buf/releases/download/v{0}/buf-Linux-x86_64",
        executable = True,
    ),
    create_http_file_reference(
        name = "buf-windows-arm64",
        sha = "bae3ae842ba4c3e38695b82020833f9997a3a0ea18730c6ff6adead3af53e6c6",
        version = BUF_VERSION,
        test_only = False,
        url = "https://github.com/bufbuild/buf/releases/download/v{0}/buf-Windows-arm64.exe",
        executable = True,
    ),
    create_http_file_reference(
        name = "buf-windows-x86_64",
        sha = "9175bbcae32e45a5e7cda0340d835f918070c8fd03b0e0c3e065c98779195308",
        version = BUF_VERSION,
        test_only = False,
        url = "https://github.com/bufbuild/buf/releases/download/v{0}/buf-Windows-x86_64.exe",
        executable = True,
    ),
    create_http_file_reference(
        name = "ktlint",
        sha = "115d4c5cb3421eae732c42c137f5db8881ff9cc1ef180a01e638283f3ccbae44",
        version = KTLINT_VERSION,
        test_only = False,
        url = "https://github.com/pinterest/ktlint/releases/download/{0}/ktlint",
    ),
    create_http_jar_reference(
        name = "checkstyle",
        sha = "2fdc30f2f55291541cff6bada4c6223c46db4bd9765b347d1c42a6a24f51ed42",
        version = CHECKSTYLE_VERSION,
        url = "https://github.com/checkstyle/checkstyle/releases/download/checkstyle-{0}/checkstyle-{0}-all.jar",
        test_only = False,
        exports_details = [
            create_export_binary_details(
                exposed_artifact_name = "checkstyle_binary",
                main_class = "com.puppycrawl.tools.checkstyle.Main",
                exportable_runtime_target = "jar",
            ),
        ],
    ),
]
