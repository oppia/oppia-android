package org.oppia.android.scripts.assets

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class ImageRepairerTest {
  private val imageRepairer = ImageRepairer()

  @Test
  fun testHasTransparentPixels_opaqueImage_returnsFalse() {
    val imageData = createPngImageData(BufferedImage.TYPE_INT_ARGB) { graphics ->
      graphics.color = Color(255, 0, 0, 255)
      graphics.fillRect(0, 0, 2, 2)
    }

    assertThat(imageRepairer.hasTransparentPixels(imageData, "png")).isFalse()
  }

  @Test
  fun testHasTransparentPixels_fullyTransparentImage_returnsTrue() {
    val imageData = createPngImageData(BufferedImage.TYPE_INT_ARGB) { graphics ->
      graphics.color = Color(0, 0, 0, 0)
      graphics.fillRect(0, 0, 2, 2)
    }

    assertThat(imageRepairer.hasTransparentPixels(imageData, "png")).isTrue()
  }

  @Test
  fun testHasTransparentPixels_partiallyTransparentPixel_returnsTrue() {
    val imageData = createPngImageData(BufferedImage.TYPE_INT_ARGB) { graphics ->
      graphics.color = Color(255, 0, 0, 128)
      graphics.fillRect(0, 0, 2, 2)
    }

    assertThat(imageRepairer.hasTransparentPixels(imageData, "png")).isTrue()
  }

  @Test
  fun testHasTransparentPixels_imageWithoutAlphaChannel_returnsFalse() {
    val imageData = createPngImageData(BufferedImage.TYPE_INT_RGB) { graphics ->
      graphics.color = Color.RED
      graphics.fillRect(0, 0, 2, 2)
    }

    assertThat(imageRepairer.hasTransparentPixels(imageData, "png")).isFalse()
  }

  @Test
  fun testHasTransparentPixels_svgExtension_returnsFalse() {
    val imageData = "<svg></svg>".toByteArray()

    assertThat(imageRepairer.hasTransparentPixels(imageData, "svg")).isFalse()
  }

  @Test
  fun testHasTransparentPixels_svgExtensionUpperCase_returnsFalse() {
    val imageData = "<svg></svg>".toByteArray()

    assertThat(imageRepairer.hasTransparentPixels(imageData, "SVG")).isFalse()
  }

  @Test
  fun testHasTransparentPixels_mixedOpaqueAndTransparent_returnsTrue() {
    val image = BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB)
    image.setRGB(0, 0, Color(255, 0, 0, 255).rgb)
    image.setRGB(1, 0, Color(0, 255, 0, 255).rgb)
    image.setRGB(0, 1, Color(0, 0, 255, 100).rgb)
    image.setRGB(1, 1, Color(255, 255, 0, 255).rgb)
    val imageData = ByteArrayOutputStream().also {
      ImageIO.write(image, "png", it)
    }.toByteArray()

    assertThat(imageRepairer.hasTransparentPixels(imageData, "png")).isTrue()
  }

  private fun createPngImageData(
    imageType: Int,
    draw: (java.awt.Graphics2D) -> Unit
  ): ByteArray {
    val image = BufferedImage(2, 2, imageType)
    val graphics = image.createGraphics()
    draw(graphics)
    graphics.dispose()
    return ByteArrayOutputStream().also {
      ImageIO.write(image, "png", it)
    }.toByteArray()
  }
}
