"""
Bazel macros for defining proto libraries.
"""

load("@rules_proto//proto:defs.bzl", "proto_library")

# TODO(#4096): Remove this once it's no longer needed.
def oppia_proto_library(name, **kwargs):
    """
    Defines a new proto library.

    Note that the library is defined with a stripped import prefix which ensures that protos have a
    common import directory (which was needed for now-obsolete legacy reasons). This common import
    directory is needed for cross-proto textprotos to work correctly.

    Args:
        name: str. The name of the proto library.
        **kwargs: additional parameters to pass into proto_library.
    """
    proto_library(
        name = name,
        strip_import_prefix = "",
        **kwargs
    )
