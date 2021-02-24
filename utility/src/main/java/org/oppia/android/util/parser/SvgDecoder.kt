package org.oppia.android.util.parser

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.IOException
import java.io.InputStream

/** Decodes an SVG internal representation from an {@link InputStream}. */
class SvgDecoder : ResourceDecoder<InputStream?, SVG?> {

  override fun handles(source: InputStream, options: Options): Boolean {
    return true
  }

  override fun decode(
    source: InputStream,
    width: Int,
    height: Int,
    options: Options
  ): Resource<SVG?>? {
    return try {
      SimpleResource(source.use { SVG.getFromInputStream(it) })
    } catch (ex: SVGParseException) {
      throw IOException("Cannot load SVG from stream", ex)
    }
  }
}
