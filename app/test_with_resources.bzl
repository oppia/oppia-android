"""
Macros for app module tests that depend on resources.
"""

def test_with_resources(name):
    # Genrule for test files.
    # Because each databinding library must have a unique package name and manifest, resources must be
    # imported using the proper package name when building with Bazel. This genrule alters those imports
    # in order to keep Gradle building.

    native.genrule(
        name = "update_" + name[0:-3],
        srcs = [name],
        outs = [name[0:-3] + "_updated.kt"],
        cmd = """
        cat $(SRCS) |
        sed 's/import org.oppia.android.R/import org.oppia.android.app.test.R/g' |
        sed 's/import org.oppia.android.databinding./import org.oppia.android.app.databinding.databinding./g' > $(OUTS)
    """,
    )

    return "update_" + name[0:-3]
