package org.oppia.util.parser

import com.bumptech.glide.load.EncodeStrategy
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceEncoder
import com.bumptech.glide.load.engine.Resource
import com.caverock.androidsvg.SVG
import org.oppia.util.logging.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/** Encodes an SVG internal representation to {@link FileOutputStream}. */
class SvgEncoder: ResourceEncoder<SVG?> {

  private lateinit var logger: Logger
  override fun getEncodeStrategy(options: Options): EncodeStrategy {
    return EncodeStrategy.SOURCE
  }

  override fun encode(data: Resource<SVG?>, file: File, options: Options): Boolean {
    return try {
      val svg: SVG = data.get()
      val picture = svg.renderToPicture()
      // Write the contents of the Picture object to disk
      val os: OutputStream = FileOutputStream(file)
      picture.writeToStream(os)
      os.close()
      true
    } catch (e: Exception) {
      logger.e("Failed to encode svg",""+e.message)
      false
    }
  }
}
