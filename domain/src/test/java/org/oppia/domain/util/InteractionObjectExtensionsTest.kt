package org.oppia.domain.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.ClickOnImage
import org.oppia.app.model.ImageWithRegions
import org.oppia.app.model.ImageWithRegions.LabeledRegion
import org.oppia.app.model.ImageWithRegions.LabeledRegion.Region
import org.oppia.app.model.ImageWithRegions.LabeledRegion.Region.NormalizedRectangle2d
import org.oppia.app.model.ImageWithRegions.LabeledRegion.Region.RegionType.RECTANGLE
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.Point2d
import org.oppia.app.model.StringList
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [InteractionObjectExtensions]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class InteractionObjectExtensionsTest {

  @Test
  fun testToAnswerStr_listOfSetsOfHtmlStrings_multipleLists_correctlyFormatsElements() {
    val listOfSetsOfHtmlStrings = ListOfSetsOfHtmlStrings.newBuilder()
      .addAllSetOfHtmlStrings(
        listOf<StringList>(
          createHtmlStringList("a", "b", "c"),
          createHtmlStringList("1", "2")
        )
      )
      .build()

    val interactionObject =
      InteractionObject.newBuilder().setListOfSetsOfHtmlString(listOfSetsOfHtmlStrings).build()

    assertThat(interactionObject.toAnswerString()).isEqualTo("[a, b, c], [1, 2]")
  }

  @Test
  fun testToAnswerStr_imageWithRegions_multipleRegions_correctlyFormatsElements() {
    val imageWithRegions = ImageWithRegions.newBuilder()
      .addLabelRegions(
        createLabelRegion(
          "Region 1",
          createPoint2d(0.1f, 0.0f) to createPoint2d(0.4f, 0.5f)
        )
      ).build()

    val interactionObject =
      InteractionObject.newBuilder().setImageWithRegions(imageWithRegions).build()

    assertThat(interactionObject.toAnswerString())
      .isEqualTo("[$RECTANGLE Region 1 (0.1, 0.0), (0.4, 0.5)]")
  }

  @Test
  fun testToAnswerStr_clickOnImage_multipleRegions_correctlyFormatsElements() {
    val clickOnImage = ClickOnImage.newBuilder()
      .setClickPosition(createPoint2d(0.3f, 1.0f))
      .addAllClickedRegions(listOf("a", "b", "c"))
      .build()

    val interactionObject =
      InteractionObject.newBuilder().setClickOnImage(clickOnImage).build()

    assertThat(interactionObject.toAnswerString())
      .isEqualTo("[(a, b, c), (0.3, 1.0)]")
  }

  private fun createPoint2d(x: Float, y: Float): Point2d {
    return Point2d.newBuilder().setX(x).setY(y).build()
  }

  private fun createLabelRegion(
    label: String,
    points: Pair<Point2d, Point2d>
  ): LabeledRegion {
    return LabeledRegion.newBuilder().setLabel(label)
      .setRegion(
        Region.newBuilder()
          .setRegionType(RECTANGLE)
          .setArea(
            NormalizedRectangle2d.newBuilder()
              .setUpperLeft(points.first)
              .setLowerRight(points.second)
          )
      )
      .build()
  }

  private fun createHtmlStringList(vararg items: String): StringList {
    return StringList.newBuilder().addAllHtml(items.toList()).build()
  }
}
