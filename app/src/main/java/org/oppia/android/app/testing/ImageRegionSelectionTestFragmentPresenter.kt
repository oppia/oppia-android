package org.oppia.android.app.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.R
import org.oppia.android.app.model.ImageWithRegions.LabeledRegion
import org.oppia.android.app.model.Point2d
import org.oppia.android.app.player.state.ImageRegionSelectionInteractionView
import javax.inject.Inject

/** The presenter for [ImageRegionSelectionTestActivity] */
class ImageRegionSelectionTestFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val view = inflater.inflate(R.layout.image_region_selection_test_fragment, container, false)
    with(view) {
      val clickableAreas: List<LabeledRegion> = getClickableAreas()
      view.findViewById<ImageRegionSelectionInteractionView>(R.id.clickable_image_view)
        .setClickableAreas(clickableAreas)
    }
    return view
  }

  private fun getClickableAreas(): List<LabeledRegion> {
    return listOf(
      createLabeledRegion(
        "Region 3",
        createPoint2d(0.24242424242424243f, 0.22400442477876106f) to
          createPoint2d(0.49242424242424243f, 0.7638274336283186f)
      ),
      createLabeledRegion(
        "Region 1",
        createPoint2d(0.553030303030303f, 0.5470132743362832f) to
          createPoint2d(0.7613636363636364f, 0.7638274336283186f)
      ),
      createLabeledRegion(
        "Region 2",
        createPoint2d(0.5454545454545454f, 0.22842920353982302f) to
          createPoint2d(0.7537878787878788f, 0.4540929203539823f)
      )
    )
  }

  private fun createLabeledRegion(
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
