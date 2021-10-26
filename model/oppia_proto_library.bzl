"""
TODO: add docs
"""

load("@rules_proto//proto:defs.bzl", "proto_library")

# TODO: add regex check
# TODO: add TODO to remove
# TODO: maybe close format proto issue with this PR?

def oppia_proto_library(name, strip_import_prefix = "", **kwargs):
    """
    TODO: add docs
    """
    proto_library(
        name = name,
        strip_import_prefix = strip_import_prefix,
        **kwargs
    )
