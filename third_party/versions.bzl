"""
Contains all versions of third-party dependencies for the Oppia Android app.
"""

DEPENDENCY_VERSIONS = {
    "androidx.constraintlayout:constraintlayout": "1.1.3",
}

def get_maven_dependencies():
    """
    Returns a list of maven dependencies to install to fulfill third-party dependencies.
    """
    return ["%s:%s" % (name, version) for name, version in DEPENDENCY_VERSIONS.items()]
