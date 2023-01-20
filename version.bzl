"""
Defines the latest version of the Oppia Android app.

Note that version codes must be ordered such that dev < alpha, and kitkat < lollipop+. This will
ensure that the Play Store provides users with the correct version of the app in situations where
their device qualifies for more than one choice.
"""

MAJOR_VERSION = 0
MINOR_VERSION = 10

# TODO(#4419): Remove the Kenya-specific alpha version code.
OPPIA_DEV_KITKAT_VERSION_CODE = 61
OPPIA_DEV_VERSION_CODE = 60
OPPIA_ALPHA_KITKAT_VERSION_CODE = 59
OPPIA_ALPHA_VERSION_CODE = 58
OPPIA_ALPHA_KENYA_VERSION_CODE = 57
OPPIA_BETA_VERSION_CODE = 56
OPPIA_GA_VERSION_CODE = 55
