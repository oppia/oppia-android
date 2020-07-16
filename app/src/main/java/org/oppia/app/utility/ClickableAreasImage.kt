package org.oppia.app.utility

import android.graphics.RectF
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
import com.github.chrisbanes.photoview.OnPhotoTapListener
import com.github.chrisbanes.photoview.PhotoViewAttacher
import org.oppia.app.R
import org.oppia.app.player.state.ImageRegionSelectionInteractionView
import kotlin.math.roundToInt

/**
 * Helper class to handle clicks on an image along with highlighting the selected region .
 */
class ClickableAreasImage(
  private val imageView: ImageRegionSelectionInteractionView,
  private val parentView: FrameLayout,
  private val listener: OnClickableAreaClickedListener
) : OnPhotoTapListener {
  private val attacher: PhotoViewAttacher = PhotoViewAttacher(imageView)

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
    val clickableAreaIndex = getClickableAreaOrDefault(x, y)
    parentView.forEachIndexed { index: Int, tappedView: View ->
      if (index > 0) {
        tappedView.isVisible = index == clickableAreaIndex + 1
        listener.onClickableAreaTouched(
          imageView.getClickableAreas()[clickableAreaIndex].label
        )
      }
    }
  }

  private fun getClickableAreaOrDefault(x: Float, y: Float): Int {
    return imageView.getClickableAreas().indexOfFirst {
      isBetween(it.region.area.upperLeft.x, it.region.area.lowerRight.x, x) &&
        isBetween(it.region.area.upperLeft.y, it.region.area.lowerRight.y, y)
    }
  }

  /** Return whether a point lies between two points.*/
  private fun isBetween(start: Float, end: Float, actual: Float): Boolean {
    return actual in start..end
  }

  /** Get X co-ordinate scaled according to image.*/
  private fun getXCoordinate(x: Float): Float {
    val rect = attacher.displayRect
    return (x * rect.width()) + rect.left
  }

  /** Get Y co-ordinate scaled according to image.*/
  private fun getYCoordinate(y: Float): Float {
    val rect = attacher.displayRect
    return (y * rect.height()) + rect.top
  }

  /** Add selectable regions to [FrameLayout].*/
  fun addViews(useSeparateRegionViews: Boolean) {
    parentView.let {
      it.removeViews(1, it.childCount - 1) // remove all other views
      imageView.getClickableAreas().forEach { clickableArea ->
        val imageRect = RectF(
          getXCoordinate(clickableArea.region.area.upperLeft.x),
          getYCoordinate(clickableArea.region.area.upperLeft.y),
          getXCoordinate(clickableArea.region.area.lowerRight.x),
          getYCoordinate(clickableArea.region.area.lowerRight.y)
        )
        val layoutParams = FrameLayout.LayoutParams(
          imageRect.width().roundToInt(),
          imageRect.height().roundToInt()
        )
        val newView = View(it.context)

        newView.x = imageRect.left
        newView.y = imageRect.top
        newView.isClickable = true
        newView.isFocusableInTouchMode = true
        newView.isFocusable = true
        newView.layoutParams = layoutParams
        newView.contentDescription = clickableArea.label
        if (useSeparateRegionViews) {
          newView.setOnClickListener {
            parentView.forEachIndexed { index: Int, tappedView: View ->
              // Remove any previously selected region excluding 0th index(image view)
              if (index > 0) {
                tappedView.setBackgroundResource(0)
              }
            }
            listener.onClickableAreaTouched(clickableArea.label)
            newView.setBackgroundResource(R.drawable.selected_region_background)
          }
        } else {
          newView.isVisible = false
          newView.setBackgroundResource(R.drawable.selected_region_background)
        }
        it.addView(newView)
      }
    }
  }
}
