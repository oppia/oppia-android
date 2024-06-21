"""
Provides a macro for setting up support for incorporating Oppia's centralized API protos.
"""

load("@oppia_proto_api//repo:deps.bzl", "initializeDepsForWorkspace")

def set_up():
    """Adds support for incorporating Oppia's centralized API protos.
    """

    # Note that the proto API's toolchains don't need to be initialized since the ones required for
    # building are already being initialized with the same names in Oppia Android.
    initializeDepsForWorkspace()
