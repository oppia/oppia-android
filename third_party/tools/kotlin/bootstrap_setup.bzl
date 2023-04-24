"""
Provides a macro for bootstrapping a custom modification of rules_kotlin.
"""

load("@rules_kotlin//src/main/starlark/release_archive:repository.bzl", "archive_repository")

# buildifier: disable=unnamed-macro
def set_up():
    """Adds support for using io_bazel_rules_kotlin as a top-level external WORKSPACE.
    """

    # See: https://github.com/bazelbuild/rules_kotlin#development-setup-guide. However, note that
    # the instructions don't seem to work for http_archive imports, so a custom approach is taken.
    archive_repository(
        name = "io_bazel_rules_kotlin",
        source_repository_name = "rules_kotlin",
    )
