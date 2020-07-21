
def process(name, src):
  """
  Formats the import statement of the src file to contain a full path to the file
  The genrule() rule generates a copy of the src file and alters the copy
  The proto_library() rule takes this altered copy and builds it
  """
  native.genrule(
    name = name,
    srcs = [src],
    outs = ["processed_" + src],
    cmd = ''' cp $< $@ &&\
    sed 's/import \"/import \"model\/src\/main\/proto\//g' $< > $@
    '''
    ,
  )

  native.proto_library(
      name = name + "_lib",
      srcs = ["processed_" + src]
  )

  native.java_proto_library(
      name = name + "_java_lib",
      deps = [name + "_lib"]
  )
