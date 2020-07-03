package org.oppia.util.image

import android.widget.ImageView
import com.github.chrisbanes.photoview.OnPhotoTapListener
import com.github.chrisbanes.photoview.PhotoViewAttacher
import org.oppia.app.model.ImageWithRegions.LabeledRegion
import javax.inject.Singleton

@Singleton
class ClickableAreasImage(
  attacher: PhotoViewAttacher,
  private val listener: OnClickableAreaClickedListener
) : OnPhotoTapListener {
  private var clickableAreas: List<LabeledRegion> = emptyList()

  init {
    attacher.setOnPhotoTapListener(this)
  }

  override fun onPhotoTap(view: ImageView, x: Float, y: Float) {
    val clickableAreas = getClickAbleAreas(x, y)
    listener.onClickableAreaTouched(clickableAreas.map { it.label })
  }

  private fun getClickAbleAreas(x: Float, y: Float): List<LabeledRegion> {
    val myClickArea: MutableList<LabeledRegion> = ArrayList()
    for (ca in clickableAreas) {
      if (isBetween(ca.region.area.lowerRight.x, ca.region.area.upperLeft.x, x)) {
        if (isBetween(ca.region.area.lowerRight.y, ca.region.area.upperLeft.y, y)) {
          myClickArea.add(ca)
        }
      }
    }
    return myClickArea
  }

  private fun isBetween(start: Float, end: Float, actual: Float): Boolean {
    return actual in start..end
  }

  fun setClickableAreas(clickableAreas: List<LabeledRegion>) {
    this.clickableAreas = clickableAreas
  }

}