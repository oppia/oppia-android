package org.oppia.util.parser

import android.graphics.Bitmap
import android.graphics.Canvas
import com.bumptech.glide.load.EncodeStrategy
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceEncoder
import com.bumptech.glide.load.engine.Resource
import com.caverock.androidsvg.SVG
import org.oppia.util.logging.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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
      val bitmap: Bitmap =
        Bitmap.createBitmap(picture.width, picture.height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      canvas.drawPicture(picture)

      val os = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.PNG, /* quality= */100 , os)
      bitmap.recycle()
      os.writeTo(FileOutputStream(file))
      os.close()
      true
    } catch (e: Exception) {
      logger.e("Failed to encode svg",""+e.message)
      false
    }
  }
}
