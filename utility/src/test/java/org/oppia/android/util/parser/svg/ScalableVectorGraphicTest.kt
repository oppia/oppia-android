package org.oppia.android.util.parser.svg

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.parser.image.ImageTransformation
import org.robolectric.annotation.LooperMode

/** Tests for [ScalableVectorGraphic]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ScalableVectorGraphicTest {
  @Test
  fun testComputeSizeSpecs_withDocumentSize_noFilenameSize_usesDocumentSizeForBoth() {
    val svg = ScalableVectorGraphic(SVG_250_BY_245)

    val sizeSpecs = svg.computeSizeSpecs()

    assertThat(sizeSpecs.intrinsicWidth).isEqualTo(250f)
    assertThat(sizeSpecs.intrinsicHeight).isEqualTo(245f)
    assertThat(sizeSpecs.renderedWidth).isEqualTo(250f)
    assertThat(sizeSpecs.renderedHeight).isEqualTo(245f)
  }

  @Test
  fun testComputeSizeSpecs_withDocumentSize_andFilenameSize_rendersAtFilenameSize() {
    val svg = ScalableVectorGraphic(SVG_250_BY_245).withFilenameSize(width = 150, height = 160)

    val sizeSpecs = svg.computeSizeSpecs()

    assertThat(sizeSpecs.intrinsicWidth).isEqualTo(250f)
    assertThat(sizeSpecs.intrinsicHeight).isEqualTo(245f)
    assertThat(sizeSpecs.renderedWidth).isEqualTo(150f)
    assertThat(sizeSpecs.renderedHeight).isEqualTo(160f)
  }

  @Test
  fun testComputeSizeSpecs_withOnlyViewBox_usesViewBoxAsIntrinsicSize() {
    val svg = ScalableVectorGraphic(SVG_VIEW_BOX_144_BY_108)
      .withFilenameSize(width = 600, height = 300)

    val sizeSpecs = svg.computeSizeSpecs()

    assertThat(sizeSpecs.intrinsicWidth).isEqualTo(144f)
    assertThat(sizeSpecs.intrinsicHeight).isEqualTo(108f)
    assertThat(sizeSpecs.renderedWidth).isEqualTo(600f)
    assertThat(sizeSpecs.renderedHeight).isEqualTo(300f)
  }

  @Test
  fun testComputeSizeSpecs_withNoSize_andFilenameSize_usesFilenameSizeForBoth() {
    val svg = ScalableVectorGraphic(SVG_WITHOUT_SIZE).withFilenameSize(width = 300, height = 150)

    val sizeSpecs = svg.computeSizeSpecs()

    assertThat(sizeSpecs.intrinsicWidth).isEqualTo(300f)
    assertThat(sizeSpecs.intrinsicHeight).isEqualTo(150f)
    assertThat(sizeSpecs.renderedWidth).isEqualTo(300f)
    assertThat(sizeSpecs.renderedHeight).isEqualTo(150f)
  }

  @Test
  fun testComputeSizeSpecs_withNoSizeAtAll_usesDefaultSize() {
    val svg = ScalableVectorGraphic(SVG_WITHOUT_SIZE)

    val sizeSpecs = svg.computeSizeSpecs()

    assertThat(sizeSpecs.intrinsicWidth).isEqualTo(1f)
    assertThat(sizeSpecs.intrinsicHeight).isEqualTo(1f)
  }

  @Test
  fun testComputeLetterboxedPictureSize_noFilenameSize_returnsNull() {
    val svg = ScalableVectorGraphic(SVG_250_BY_245)

    assertThat(svg.computeLetterboxedPictureSize()).isNull()
  }

  @Test
  fun testComputeLetterboxedPictureSize_filenameSizeWithSameAspectRatio_returnsNull() {
    // 500x490 is exactly twice the size of the SVG, so no letterboxing is needed.
    val svg = ScalableVectorGraphic(SVG_250_BY_245).withFilenameSize(width = 500, height = 490)

    assertThat(svg.computeLetterboxedPictureSize()).isNull()
  }

  @Test
  fun testComputeLetterboxedPictureSize_filenameSizeOffBySubPixel_returnsNull() {
    // 600x450 would be exact; 600x451 only needs half a rendered pixel of padding on each side.
    val svg =
      ScalableVectorGraphic(SVG_VIEW_BOX_144_BY_108).withFilenameSize(width = 600, height = 451)

    assertThat(svg.computeLetterboxedPictureSize()).isNull()
  }

  @Test
  fun testComputeLetterboxedPictureSize_smallSvg_offBySubPixelOfSvgOnly_padsTopAndBottom() {
    // The SVG is tiny compared to its rendered size, so a quarter of an SVG pixel of padding is
    // actually 15 rendered pixels (which would visibly stretch the image if it were ignored).
    val svg =
      ScalableVectorGraphic(SVG_VIEW_BOX_10_BY_10).withFilenameSize(width = 600, height = 630)

    val letterboxSize = svg.computeLetterboxedPictureSize()

    assertThat(letterboxSize).isNotNull()
    assertThat(letterboxSize?.pictureWidth).isEqualTo(10f)
    assertThat(letterboxSize?.pictureHeight).isWithin(TOLERANCE).of(10.5f)
    assertThat(letterboxSize?.documentViewPort?.top).isWithin(TOLERANCE).of(0.25f)
  }

  @Test
  fun testComputeLetterboxedPictureSize_wideFilenameSize_padsLeftAndRight() {
    // The SVG is 4:3 but the filename says 2:1, so the picture needs to be wider than the SVG.
    val svg = ScalableVectorGraphic(SVG_VIEW_BOX_144_BY_108)
      .withFilenameSize(width = 600, height = 300)

    val letterboxSize = svg.computeLetterboxedPictureSize()

    assertThat(letterboxSize).isNotNull()
    assertThat(letterboxSize?.pictureWidth).isEqualTo(216f)
    assertThat(letterboxSize?.pictureHeight).isEqualTo(108f)
    assertThat(letterboxSize?.documentViewPort).isEqualTo(RectF(36f, 0f, 180f, 108f))
  }

  @Test
  fun testComputeLetterboxedPictureSize_tallFilenameSize_padsTopAndBottom() {
    // The SVG is a bit wider than tall, but the filename says it's a bit taller than wide.
    val svg = ScalableVectorGraphic(SVG_250_BY_245).withFilenameSize(width = 150, height = 160)

    val letterboxSize = svg.computeLetterboxedPictureSize()

    assertThat(letterboxSize).isNotNull()
    assertThat(letterboxSize?.pictureWidth).isWithin(TOLERANCE).of(250f)
    assertThat(letterboxSize?.pictureHeight).isWithin(TOLERANCE).of(266.6667f)
    val documentViewPort = checkNotNull(letterboxSize?.documentViewPort)
    assertThat(documentViewPort.left).isWithin(TOLERANCE).of(0f)
    assertThat(documentViewPort.top).isWithin(TOLERANCE).of(10.83333f)
    assertThat(documentViewPort.width()).isWithin(TOLERANCE).of(250f)
    assertThat(documentViewPort.height()).isWithin(TOLERANCE).of(245f)
  }

  @Test
  fun testComputeLetterboxedPictureSize_noViewBox_mismatchedFilenameSize_padsLeftAndRight() {
    val svg =
      ScalableVectorGraphic(SVG_100_BY_100_WITHOUT_VIEW_BOX)
        .withFilenameSize(width = 200, height = 100)

    val letterboxSize = svg.computeLetterboxedPictureSize()

    assertThat(letterboxSize).isNotNull()
    assertThat(letterboxSize?.pictureWidth).isEqualTo(200f)
    assertThat(letterboxSize?.pictureHeight).isEqualTo(100f)
    assertThat(letterboxSize?.documentViewPort).isEqualTo(RectF(50f, 0f, 150f, 100f))
  }

  @Test
  fun testComputeLetterboxedPictureSize_noIntrinsicSize_returnsNull() {
    val svg = ScalableVectorGraphic(SVG_WITHOUT_SIZE).withFilenameSize(width = 300, height = 150)

    assertThat(svg.computeLetterboxedPictureSize()).isNull()
  }

  @Test
  fun testRenderToBlockPicture_noFilenameSize_pictureHasDocumentSize() {
    val svg = ScalableVectorGraphic(SVG_250_BY_245)

    val picture = svg.renderToBlockPicture()

    assertThat(picture.width).isEqualTo(250)
    assertThat(picture.height).isEqualTo(245)
  }

  @Test
  fun testRenderToBlockPicture_preserving_sameAspectRatio_pictureHasDocumentSize() {
    val svg = ScalableVectorGraphic(SVG_250_BY_245)
      .withFilenameSizePreservingAspectRatio(width = 500, height = 490)

    val picture = svg.renderToBlockPicture()

    assertThat(picture.width).isEqualTo(250)
    assertThat(picture.height).isEqualTo(245)
  }

  @Test
  fun testRenderToBlockPicture_preserving_wideFilenameSize_pictureMatchesFilenameAspectRatio() {
    val svg = ScalableVectorGraphic(SVG_VIEW_BOX_144_BY_108)
      .withFilenameSizePreservingAspectRatio(width = 600, height = 300)

    val picture = svg.renderToBlockPicture()

    // The picture is 2:1 (like the filename) so that drawing it at 600x300 doesn't stretch it.
    assertThat(picture.width).isEqualTo(216)
    assertThat(picture.height).isEqualTo(108)
  }

  @Test
  fun testRenderToBlockPicture_preserving_tallFilenameSize_pictureMatchesFilenameAspectRatio() {
    val svg = ScalableVectorGraphic(SVG_250_BY_245)
      .withFilenameSizePreservingAspectRatio(width = 150, height = 160)

    val picture = svg.renderToBlockPicture()

    assertThat(picture.width).isEqualTo(250)
    assertThat(picture.height).isEqualTo(267)
  }

  @Test
  fun testRenderToBlockPicture_preserving_svgAllowsStretching_pictureHasDocumentSize() {
    // SVGs that opt into stretching are still stretched, like they are on web.
    val svg = ScalableVectorGraphic(SVG_VIEW_BOX_144_BY_108_STRETCHABLE)
      .withFilenameSizePreservingAspectRatio(width = 600, height = 300)

    val picture = svg.renderToBlockPicture()

    assertThat(picture.width).isEqualTo(144)
    assertThat(picture.height).isEqualTo(108)
  }

  @Test
  fun testRenderToBlockPicture_notPreserving_wideFilenameSize_pictureHasDocumentSize() {
    // Without opting in, the SVG keeps being stretched to its rendered size (for example, for image
    // region selection where the clickable regions are relative to the whole image view).
    val svg = ScalableVectorGraphic(SVG_VIEW_BOX_144_BY_108)
      .withFilenameSize(width = 600, height = 300)

    val picture = svg.renderToBlockPicture()

    assertThat(picture.width).isEqualTo(144)
    assertThat(picture.height).isEqualTo(108)
  }

  @Test
  fun testComputeSizeSpecsForTextPicture_noTextPaint_noFilenameSize_usesDocumentSize() {
    val svg = ScalableVectorGraphic(SVG_250_BY_245)

    val sizeSpecs = svg.computeSizeSpecsForTextPicture(textPaint = null)

    assertThat(sizeSpecs.intrinsicWidth).isEqualTo(250f)
    assertThat(sizeSpecs.intrinsicHeight).isEqualTo(245f)
    assertThat(sizeSpecs.verticalAlignment).isEqualTo(0f)
  }

  @Test
  fun testTransform_addsTransformations_andKeepsFilenameSizeAndAspectRatioSetting() {
    val svg = ScalableVectorGraphic(SVG_VIEW_BOX_144_BY_108)
      .withFilenameSizePreservingAspectRatio(width = 600, height = 300)

    val transformedSvg = svg.transform(listOf(ImageTransformation.BLUR, ImageTransformation.BLUR))

    assertThat(svg.transformations).isEmpty()
    assertThat(transformedSvg.transformations).containsExactly(ImageTransformation.BLUR)
    assertThat(transformedSvg.computeSizeSpecs().renderedWidth).isEqualTo(600f)
    // The transformed graphic should still keep its aspect ratio when rendered.
    assertThat(transformedSvg.renderToBlockPicture().width).isEqualTo(216)
  }

  private fun ScalableVectorGraphic.withFilenameSize(width: Int, height: Int) =
    also { it.initializeWithExtractedDimensions(width, height) }

  private fun ScalableVectorGraphic.withFilenameSizePreservingAspectRatio(width: Int, height: Int) =
    also { it.initializeWithExtractedDimensions(width, height, preserveAspectRatio = true) }

  private companion object {
    private const val TOLERANCE = 1e-3f

    private const val SVG_250_BY_245 =
      "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"250\" height=\"245\"" +
        " viewBox=\"0 0 250 245\"><rect width=\"250\" height=\"245\"/></svg>"

    private const val SVG_VIEW_BOX_144_BY_108 =
      "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 144 108\">" +
        "<rect width=\"144\" height=\"108\"/></svg>"

    private const val SVG_VIEW_BOX_144_BY_108_STRETCHABLE =
      "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 144 108\"" +
        " preserveAspectRatio=\"none\"><rect width=\"144\" height=\"108\"/></svg>"

    private const val SVG_VIEW_BOX_10_BY_10 =
      "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 10 10\">" +
        "<rect width=\"10\" height=\"10\"/></svg>"

    private const val SVG_100_BY_100_WITHOUT_VIEW_BOX =
      "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">" +
        "<rect width=\"100\" height=\"100\"/></svg>"

    private const val SVG_WITHOUT_SIZE =
      "<svg xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"10\" height=\"10\"/></svg>"
  }
}
