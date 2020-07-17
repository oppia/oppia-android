def process(name, src):
  """
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
