def format_import_proto_library(name, src, deps):
  """
  This macro exists as a way to build proto files that contain import statements in both Gradle
  and Bazel.
  This macro formats the src file's import statements to contain a full path to the file in order
  for Bazel to properly locate file.
  """

  native.genrule(
    name = name,
    srcs = [src],
    outs = ["processed_" + src],
    cmd = '''
    cat $< |
    sed 's/import \"/import \"model\/src\/main\/proto\//g' |
    sed 's/\"model\/src\/main\/proto\/exploration/\"model\/processed_src\/main\/proto\/exploration/g' |
    sed 's/\"model\/src\/main\/proto\/topic/\"model\/processed_src\/main\/proto\/topic/g' |
    sed 's/\"model\/src\/main\/proto\/question/\"model\/processed_src\/main\/proto\/question/g' > $@
    ''',
  )

  native.proto_library(
      name = name + "_proto",
      srcs = ["processed_" + src],
      deps = deps,
  )
