"""
Provides a macro for setting up support for using protobuf tools.
"""

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

def set_up():
    """Adds support for using protobuf tools.
    """

    # See https://github.com/protocolbuffers/protobuf/blob/v3.12.4/protobuf_deps.bzl for context.
    protobuf_deps()
