"""
Defines the latest version of the Oppia Android app.

Note that version codes must be ordered such that dev > alpha, and kitkat < lollipop+. This will
ensure that the Play Store provides users with the correct version of the app in situations where
their device qualifies for more than one choice.

More unstable flavors of the app are higher version codes since they represent "newer" versions of
the app (that are potentially not broadly released yet).
"""

MAJOR_VERSION = 0
MINOR_VERSION = 11

# TODO(#4419): Remove the Kenya-specific alpha version code.
OPPIA_DEV_VERSION_CODE = 103
OPPIA_DEV_KITKAT_VERSION_CODE = 102
OPPIA_ALPHA_KENYA_VERSION_CODE = 101
OPPIA_ALPHA_VERSION_CODE = 100
OPPIA_ALPHA_KITKAT_VERSION_CODE = 99
OPPIA_BETA_VERSION_CODE = 98
OPPIA_GA_VERSION_CODE = 97
