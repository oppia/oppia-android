"""
Provides a macro for setting up support to use Google Play Services plugins & libraries.
"""

load("@tools_android//tools/googleservices:defs.bzl", "google_services_workspace_dependencies")

def setUp():
    """
    Adds support for using Google Play Services plugins & libraries to the workspace.
    """

    # Add support for Firebase Crashlytics.
    google_services_workspace_dependencies()
