"""
Defines the latest version of the Oppia Android app.

Note that version codes must be ordered such that dev < alpha, and kitkat < lollipop+. This will
ensure that the Play Store provides users with the correct version of the app in situations where
their device qualifies for more than one choice.
"""

MAJOR_VERSION = 0
MINOR_VERSION = 6

OPPIA_DEV_KITKAT_VERSION_CODE = 14
OPPIA_DEV_VERSION_CODE = 15
OPPIA_ALPHA_KITKAT_VERSION_CODE = 16
OPPIA_ALPHA_VERSION_CODE = 17

TOTAL_VERSIONS = 4

def increase_minor_version():
    """Increases the release version by one."""
    MINOR_VERSION += 1

def increase_major_version():
    """Increases the major version by one."""
    MAJOR_VERSION += 1

def increase_version_code():
    """Each version code will be increaed by the number of versions that have been released."""
    OPPIA_ALPHA_KITKAT_VERSION_CODE += TOTAL_VERSIONS
    OPPIA_ALPHA_VERSION_CODE += TOTAL_VERSIONS
    OPPIA_DEV_KITKAT_VERSION_CODE += TOTAL_VERSIONS
    OPPIA_DEV_VERSION_CODE += TOTAL_VERSIONS
