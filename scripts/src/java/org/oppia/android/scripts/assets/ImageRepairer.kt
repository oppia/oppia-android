package org.oppia.android.scripts.assets

import com.github.weisj.jsvg.parser.SVGLoader
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.roundToInt
import org.oppia.android.scripts.assets.ImageRepairer.Companion.resizeTo

class ImageRepairer {
  fun convertToPng(filename: String, svgImageContents: String): RepairedImage {
    if ("data:image/png;base64" !in svgImageContents) return RepairedImage.NoRepairNeeded
    val loader = SVGLoader()
    val svgDocument =
      svgImageContents.byteInputStream().use { loader.load(it) }
        ?: error("Failed to load: $filename.")
    val size = svgDocument.size()
    val chosenWidth =
      filename.extractWidthFromFilename().toFloat().convertOppiaPxToStandardAndroidPx().roundToInt()
    val chosenHeight =
      filename.extractHeightFromFilename().toFloat().convertOppiaPxToStandardAndroidPx().roundToInt()
    // Render at a larger size to reduce aliasing from the underlying rendering library (but render
    // at no larger than 5x the .
    val renderWidth = chosenWidth * 5
    val renderHeight = chosenHeight * 5
    val scaleFactorX = renderWidth.toDouble() / size.getWidth()
    val scaleFactorY = renderHeight.toDouble() / size.getHeight()
    val renderImage = BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB)
    val graphics = renderImage.createGraphics()
    graphics.scale(scaleFactorX, scaleFactorY)
    svgDocument.render(/* component = */ null, graphics)
    graphics.dispose()
    val image = renderImage.resizeTo(chosenWidth, chosenHeight)
    return ByteArrayOutputStream().use {
      ImageIO.write(image, /* formatName = */ "png", it)
      return@use RepairedImage.RenderedSvg(it.toByteArray().toList(), chosenWidth, chosenHeight)
    }
  }

  fun areEqualImages(extension: String, imageData1: ByteArray, imageData2: ByteArray): Boolean {
    if (extension == "svg") return imageData1.decodeToString() == imageData2.decodeToString()
    val image1 = imageData1.inputStream().use { ImageIO.read(it) } ?: error("Cannot read file of type $extension (data size: ${imageData1.size} bytes).")
    val image2 = imageData2.inputStream().use { ImageIO.read(it) } ?: error("Cannot read file of type $extension (data size: ${imageData2.size} bytes).")
    return areImagesEqual(image1, image2)
  }

  sealed class RepairedImage {
    data class RenderedSvg(
      val pngContents: List<Byte>, val width: Int, val height: Int
    ): RepairedImage()

    object NoRepairNeeded: RepairedImage()
  }

  private fun areImagesEqual(image1: BufferedImage, image2: BufferedImage): Boolean {
    if (image1.width != image2.width) return false
    if (image1.height != image2.height) return false
    for (y in 0 until image1.height) {
      for (x in 0 until image1.width) {
        if (image1.getRGB(x, y) != image2.getRGB(x, y)) return false
      }
    }
    return true
  }

  private companion object {
    private val WIDTH_REGEX by lazy { "width_(\\d+)".toRegex() }
    private val HEIGHT_REGEX by lazy { "height_(\\d+)".toRegex() }
    private val TRANSPARENT = Color(/* r = */ 0, /* g = */ 0, /* b = */ 0, /* a = */ 0)

    private const val REFERENCE_MONITOR_PPI = 81.589f
    private const val RELATIVE_SIZE_ADJUSTMENT_FACTOR = 0.15f
    private const val MOST_COMMON_DEVICE_PPI = 270f // Based on observed usage.
    private const val PRESUMED_MOST_COMMON_DEVICE_DENSITY = 320f
    // This is computed per Android density documentation.
    private const val COMMON_DEVICE_DENSITY_SCALAR = PRESUMED_MOST_COMMON_DEVICE_DENSITY / 160f
    private const val OPPIA_LOCAL_IMAGE_SPACE_CONVERSION_FACTOR =
      // The conversion here is from Oppia pixel to MDPI pixel (which can be treated as dp since
      // 1px=1dp in MDPI) for later scaling according to the user's set display density.
      (MOST_COMMON_DEVICE_PPI / REFERENCE_MONITOR_PPI) * RELATIVE_SIZE_ADJUSTMENT_FACTOR

    private fun String.extractWidthFromFilename(): Int {
      return WIDTH_REGEX.find(this)?.destructured?.component1()?.toIntOrNull()
        ?: error("Invalid filename: $this.")
    }

    private fun String.extractHeightFromFilename(): Int {
      return HEIGHT_REGEX.find(this)?.destructured?.component1()?.toIntOrNull()
        ?: error("Invalid filename: $this.")
    }

    private fun BufferedImage.resizeTo(newWidth: Int, newHeight: Int): BufferedImage =
      getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH).buffered(type)

    private fun Image.buffered(imageType: Int): BufferedImage {
      return BufferedImage(
        getWidth(/* observer = */ null), getHeight(/* observer = */ null), imageType
      ).also {
        val graphics = it.createGraphics()
        graphics.drawImage(
          /* img = */ this,
          /* x = */ 0,
          /* y = */ 0,
          /* bgColor = */ TRANSPARENT,
          /* observer = */ null
        )
        graphics.dispose()
      }
    }

    private fun Float.convertOppiaPxToStandardAndroidPx() =
      this * COMMON_DEVICE_DENSITY_SCALAR * OPPIA_LOCAL_IMAGE_SPACE_CONVERSION_FACTOR
  }
}
