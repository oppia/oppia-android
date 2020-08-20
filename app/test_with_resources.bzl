def test_with_resources(file):
  '''
  Genrule for test files.

  Because each databinding library must have a unique package name and manifest, resources must be
  imported using the proper package name when building with Bazel. This genrule alters those imports
  in order to keep Gradle building.
  '''
  native.genrule(
    name = "update_" + file[0:-3],
    srcs = [file[0:-3] + ".kt"],
    outs = [file[0:-3] + "_updated.kt"],
    cmd = '''
        cat $(SRCS) |
        sed 's/import org.oppia.app.R/import org.oppia.app.test.R/g' |
        sed 's/import org.oppia.app.databinding./import org.oppia.app.databinding.databinding./g' > $(OUTS)
    ''',
  )

  return "update_" + file[0:-3]
