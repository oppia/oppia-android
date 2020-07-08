package org.oppia.app.utility

import android.graphics.RectF
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.github.chrisbanes.photoview.OnPhotoTapListener
import com.github.chrisbanes.photoview.PhotoViewAttacher
import org.oppia.app.model.ImageWithRegions.LabeledRegion
import kotlin.math.roundToInt

class ClickableAreasImage(
  imageView: ImageView,
  private val overlayView: View,
  private val listener: OnClickableAreaClickedListener
) : OnPhotoTapListener {
  private val attacher: PhotoViewAttacher = PhotoViewAttacher(imageView)
  private var clickableAreas: List<LabeledRegion> = emptyList()

  init {
    attacher.setOnPhotoTapListener(this)
  }

  /**
   * Called when an image is clicked.
   *
   * @param view the original view on which the tap/click occurs.
   * @param x the relative x coordinate according to image
   * @param y the relative y coordinate according to image
   */
  override fun onPhotoTap(view: ImageView, x: Float, y: Float) {
    val clickableArea = getClickableAreaOrDefault(x, y)
    if (clickableArea.hasRegion()) {
      val imageRect = RectF(
        getXCoordinate(clickableArea.region.area.upperLeft.x),
        getYCoordinate(clickableArea.region.area.upperLeft.y),
        getXCoordinate(clickableArea.region.area.lowerRight.x),
        getYCoordinate(clickableArea.region.area.lowerRight.y)
      )
      val overlayViewParams = FrameLayout.LayoutParams(
        imageRect.width().roundToInt(),
        imageRect.height().roundToInt()
      )
      overlayView.layoutParams = overlayViewParams
      overlayView.x = imageRect.left
      overlayView.y = imageRect.top
      overlayView.visibility = View.VISIBLE
      listener.onClickableAreaTouched(clickableArea.label)
    } else {
      overlayView.visibility = View.GONE
    }
  }

  private fun getClickableAreaOrDefault(x: Float, y: Float): LabeledRegion {
    val area = clickableAreas.filter {
      isBetween(it.region.area.upperLeft.x, it.region.area.lowerRight.x, x) &&
        isBetween(it.region.area.upperLeft.y, it.region.area.lowerRight.y, y)
    }.firstOrNull()
    return area ?: LabeledRegion.getDefaultInstance()
  }

  /* Return whether a point lies between two points.*/
  private fun isBetween(start: Float, end: Float, actual: Float): Boolean {
    return actual in start..end
  }

  /* Get X co-ordinate scaled according to image.*/
  private fun getXCoordinate(x: Float): Float {
    val rect = attacher.displayRect
    return (x * rect.width()) + rect.left
  }

  /* Get Y co-ordinate scaled according to image.*/
  private fun getYCoordinate(x: Float): Float {
    val rect = attacher.displayRect
    return (x * rect.height()) + rect.top
  }

  fun setClickableAreas(clickableAreas: List<LabeledRegion>) {
    this.clickableAreas = clickableAreas
  }
}
