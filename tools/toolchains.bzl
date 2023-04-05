"""
Defines Starlark macros that are used to set up dependency toolchains needed to build the Oppia
Android project.
"""

load("//tools/android:setup.bzl", setUpAndroid = "setUp")
load("//tools/java:setup.bzl", setUpJava = "setUp")
load("//tools/kotlin:setup.bzl", setUpKotlin = "setUp")
load("//tools/proto:setup.bzl", setUpProto = "setUp")
load("//tools/robolectric:setup.bzl", setUpRobolectric = "setUp")
load("//tools/skylib:setup.bzl", setUpSkylib = "setUp")
load("//tools/tools_android:setup.bzl", setUpToolsAndroid = "setUp")

def initializeToolchainsForWorkspace():
    """
    Initializes the toolchains needed to be able to build the Oppia Android app & tests.

    Note that this must be called after loading in this toolchains file, for example:
        load("//tools:toolchains.bzl", "initializeToolchainsForWorkspace")
        initializeToolchainsForWorkspace()

    Note also that this can't be called until the dependencies themselves are loaded (see
    third_party/deps.bzl).
    """

    # Note that the order matters here since toolchains & libraries may have cross-dependencies.
    setUpSkylib()
    setUpAndroid()
    setUpKotlin()
    setUpJava()
    setUpProto()
    setUpRobolectric()
    setUpToolsAndroid()
