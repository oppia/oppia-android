"""
Defines the latest version of the Oppia Android app.

Note that version codes must be ordered such that dev < alpha, and kitkat < lollipop+. This will
ensure that the Play Store provides users with the correct version of the app in situations where
their device qualifies for more than one choice.
"""

MAJOR_VERSION = 0
MINOR_VERSION = 7

# TODO(#4348): Offset these version codes by '+1' for the next release.
OPPIA_DEV_KITKAT_VERSION_CODE = 22
OPPIA_DEV_VERSION_CODE = 23
OPPIA_ALPHA_KITKAT_VERSION_CODE = 24
OPPIA_ALPHA_VERSION_CODE = 25
