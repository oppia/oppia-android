"""
Defines Starlark macros that are used to set up dependency toolchains needed to build the Oppia
Android project.
"""

load("//third_party/tools/android:toolchain_setup.bzl", setUpAndroid = "setUp")
load("//third_party/tools/java:toolchain_setup.bzl", setUpJava = "setUp")
load("//third_party/tools/kotlin:toolchain_setup.bzl", setUpKotlin = "setUp")
load("//third_party/tools/proto:toolchain_setup.bzl", setUpProto = "setUp")
load("//third_party/tools/robolectric:toolchain_setup.bzl", setUpRobolectric = "setUp")
load("//third_party/tools/skylib:toolchain_setup.bzl", setUpSkylib = "setUp")
load("//third_party/tools/tools_android:toolchain_setup.bzl", setUpToolsAndroid = "setUp")

def initializeToolchainsForWorkspace():
    """
    Initializes the toolchains needed to be able to build the Oppia Android app & tests.

    Note that this must be called after loading in this toolchains file, for example:
        load("//third_party/tools:toolchains.bzl", "initializeToolchainsForWorkspace")
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
