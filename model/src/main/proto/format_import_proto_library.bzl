
def format_import_proto_library(name, src, deps):
  """
  Formats the import statement of the src file to contain a full path to the file
  The genrule() rule generates a copy of the src file and alters the copy
  The proto_library() rule takes this altered copy and builds it
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
      deps = deps
  )
