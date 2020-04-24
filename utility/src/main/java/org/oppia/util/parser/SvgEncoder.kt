package org.oppia.util.parser

import com.bumptech.glide.load.EncodeStrategy
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceEncoder
import com.bumptech.glide.load.engine.Resource
import com.caverock.androidsvg.SVG
import java.io.File
import java.io.FileOutputStream

class SvgEncoder : ResourceEncoder<SVG?> {

  override fun getEncodeStrategy(options: Options): EncodeStrategy {
    return EncodeStrategy.SOURCE
  }

  override fun encode(data: Resource<SVG?>, file: File, options: Options): Boolean {
    return try {
      val svg: SVG = data.get()
      val picture = svg.renderToPicture()
      picture.writeToStream(FileOutputStream(file))
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }
}
