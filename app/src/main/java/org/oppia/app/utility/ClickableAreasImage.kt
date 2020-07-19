package org.oppia.app.utility

import android.graphics.RectF
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import com.github.chrisbanes.photoview.OnPhotoTapListener
import com.github.chrisbanes.photoview.PhotoViewAttacher
import org.oppia.app.R
import org.oppia.app.model.ImageWithRegions
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
    // show default region for non-accessibility case
    if (!imageView.isAccessibilityEnabled()) {
      parentView.forEachIndexed { index: Int, childView: View ->
        // Remove any previously selected region excluding 0th index(image view)
        if (index > 0) {
          childView.setBackgroundResource(0)
        }
      }
      val defaultRegion = parentView.findViewById<View>(R.id.default_selected_region)
      defaultRegion.setBackgroundResource(R.drawable.selected_region_background)
      defaultRegion.x = getXCoordinate(x)
      defaultRegion.y = getYCoordinate(y)
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
  fun addViews() {
    parentView.let {
      if (it.childCount > 2)
        it.removeViews(2, it.childCount - 1) // remove all other views
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
        newView.layoutParams = layoutParams
        newView.x = imageRect.left
        newView.y = imageRect.top
        newView.isClickable = true
        newView.isFocusable = true
        newView.isFocusableInTouchMode = true
        newView.tag = clickableArea.label
        newView.contentDescription = clickableArea.label
        newView.setOnTouchListener { _, _ ->
          showOrHideRegion(newView, clickableArea)
          return@setOnTouchListener true
        }
        if (imageView.isAccessibilityEnabled()) {
          newView.setOnClickListener {
            showOrHideRegion(newView, clickableArea)
          }
        }
        it.addView(newView)
        newView.requestLayout()
      }
    }
  }

  private fun showOrHideRegion(newView: View, clickableArea: ImageWithRegions.LabeledRegion) {
    parentView.forEachIndexed { index: Int, tappedView: View ->
      // Remove any previously selected region excluding 0th index(image view)
      if (index > 0) {
        tappedView.setBackgroundResource(0)
      }
    }
    listener.onClickableAreaTouched(clickableArea.label)
    newView.setBackgroundResource(R.drawable.selected_region_background)
  }
}
