package org.oppia.android.util.parser

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import java.io.InputStream

/** Decodes an SVG internal representation from an {@link InputStream}. */
class SvgDecoder : ResourceDecoder<InputStream?, OppiaSvg?> {

  override fun handles(source: InputStream, options: Options): Boolean {
    return true
  }

  override fun decode(
    source: InputStream,
    width: Int,
    height: Int,
    options: Options
  ): Resource<OppiaSvg?> {
    val svgSource = source.bufferedReader().readLines().joinToString(separator = "\n")
    return SimpleResource(OppiaSvg(svgSource))
  }
}
