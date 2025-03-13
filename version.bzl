"""
Defines the latest version of the Oppia Android app.

Note that version codes must be ordered such that dev > alpha. This will ensure that the Play Store
provides users with the correct version of the app in situations where their device qualifies for
more than one choice.

More unstable flavors of the app are higher version codes since they represent "newer" versions of
the app (that potentially contain changes or features that are not yet ready for broad release).
"""

MAJOR_VERSION = 0
MINOR_VERSION = 15

OPPIA_DEV_VERSION_CODE = 186
OPPIA_ALPHA_VERSION_CODE = 185
OPPIA_BETA_VERSION_CODE = 184
OPPIA_GA_VERSION_CODE = 183
