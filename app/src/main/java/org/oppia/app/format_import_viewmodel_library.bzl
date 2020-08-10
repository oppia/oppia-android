def format_import_viewmodel_library(name, file_list):
  '''
  This macro exists as a way to build proto files that contain import statements in both Gradle
  and Bazel.
  This macro formats the src file's import statements to contain a full path to the file in order
  for Bazel to properly locate file.

  Args:
      name: str. The name of the .proto file without the '.proto' suffix. This will be the root for
          the name of the proto library created. Ex: If name = 'topic', then the src file is
          'topic.proto' and the proto library created will be named 'topic_proto'.
      src: str. The name of the .proto file to be built into a proto_library.
      deps: list of str. The list of dependencies needed to build the src file. This list will contain
          all of the proto_library targets for the files imported into src.
  '''

  # TODO(#1543): Ensure this function works on Windows systems.

#  [
#      native.genrule(
#          name = "update_" + file,
#          srcs = [file + ".kt"],
#          outs = [file + "_updated.kt"],
#          cmd = '''
#           cat $(SRCS) |
#           sed 's/import org.oppia.app.R/import org.oppia.app.vm.R/g' > $(OUTS)
#          ''',
#      )
#  for file in file_list]
