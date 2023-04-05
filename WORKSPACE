"""
This file initializes all external dependencies & toolchains needed to build Oppia Android.
"""

# Note to developer: The order of loads & calls below MUST NOT BE CHANGED--it will guarantee a
# breakage as subsequent loads depend on both previous loads & calls. There is no other order
# possible for the configuration below, and it ought to never need to be changed.

workspace(name = "oppia_android")

load("//third_party:direct_deps.bzl", "downloadDirectWorkspaceDependencies")

downloadDirectWorkspaceDependencies()

load("//tools:toolchains.bzl", "initializeToolchainsForWorkspace")

initializeToolchainsForWorkspace()

load("//third_party:maven_deps.bzl", "downloadMavenDependencies")

downloadMavenDependencies()

load("//third_party:maven_deps_pinner.bzl", "pinMavenDependencies")

pinMavenDependencies()
