package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.image_region_selection_test_activity.*
import org.oppia.app.R
import org.oppia.app.model.ImageWithRegions.LabeledRegion
import org.oppia.app.model.Point2d
import org.oppia.app.utility.ClickableAreasImage
import org.oppia.app.utility.OnClickableAreaClickedListener
import javax.inject.Inject

/** The presenter for [ImageRegionSelectionTestActivity] */
class ImageRegionSelectionTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.image_region_selection_test_activity)
    with(activity) {
      val clickableAreas: List<LabeledRegion> = getClickableAreas()
      clickable_image_view.setClickableAreas(clickableAreas)

      val clickableAreasImage = ClickableAreasImage(
        clickable_image_view,
        image_parent_view,
        this as OnClickableAreaClickedListener
      )
      clickable_image_view.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> // ktlint-disable max-line-length
        // Update the regions, as the bounds have changed
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom)
          clickableAreasImage.addViews(
            useSeparateRegionViews = clickable_image_view.isAccessibilityEnabled()
          )
      }
    }
  }

  private fun getClickableAreas(): List<LabeledRegion> {
    return listOf(
      createLabelRegion(
        "Region 1",
        createPoint2d(0.553030303030303f, 0.5470132743362832f) to
          createPoint2d(0.7613636363636364f, 0.7638274336283186f)
      ),
      createLabelRegion(
        "Region 2",
        createPoint2d(0.5454545454545454f, 0.22842920353982302f) to
          createPoint2d(0.7537878787878788f, 0.4540929203539823f)
      ),
      createLabelRegion(
        "Region 3",
        createPoint2d(0.24242424242424243f, 0.22400442477876106f) to
          createPoint2d(0.49242424242424243f, 0.7638274336283186f)
      )
    )
  }

  private fun createLabelRegion(
    label: String,
    points: Pair<Point2d, Point2d>
  ): LabeledRegion {
    return LabeledRegion.newBuilder().setLabel(label)
      .setRegion(
        LabeledRegion.Region.newBuilder()
          .setRegionType(LabeledRegion.Region.RegionType.RECTANGLE)
          .setArea(
            LabeledRegion.Region.NormalizedRectangle2d.newBuilder()
              .setUpperLeft(points.first)
              .setLowerRight(points.second)
          )
      )
      .build()
  }

  private fun createPoint2d(x: Float, y: Float): Point2d {
    return Point2d.newBuilder().setX(x).setY(y).build()
  }
}
