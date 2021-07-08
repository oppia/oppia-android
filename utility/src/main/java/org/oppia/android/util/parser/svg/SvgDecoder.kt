package org.oppia.android.util.parser.svg

import com.bumptech.glide.load.Option
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.request.RequestOptions
import java.io.InputStream

/** Decodes an SVG internal representation from an {@link InputStream}. */
class SvgDecoder : ResourceDecoder<InputStream?, ScalableVectorGraphic?> {

  override fun handles(source: InputStream, options: Options): Boolean =
    options.get(LOAD_OPPIA_SVG) ?: false

  override fun decode(
    source: InputStream,
    width: Int,
    height: Int,
    options: Options
  ): Resource<ScalableVectorGraphic?> {
    val svgSource = source.bufferedReader().readLines().joinToString(separator = "\n")
    return SimpleResource(ScalableVectorGraphic(svgSource))
  }

  companion object {
    // Reference: https://stackoverflow.com/q/54360199.
    private val LOAD_OPPIA_SVG: Option<Boolean> =
      Option.memory(/* key= */ "load_oppia_svg", /* defaultValue= */ false)

    /**
     * Returns a [RequestOptions] that, when applied to a Glide request, ensures that the input
     * stream for that request is interpreted as an SVG. This must be used in SVG-based requests or
     * they will not be loaded correctly.
     */
    fun createLoadSvgFromPipelineOption(): RequestOptions =
      RequestOptions.option(LOAD_OPPIA_SVG, /* value= */ true)
  }
}
