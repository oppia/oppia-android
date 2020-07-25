package org.oppia.app.utility

import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
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
) {
  init {
    imageView.setOnTouchListener { view, motionEvent ->
      if (motionEvent.action == MotionEvent.ACTION_DOWN) {
        onPhotoTap(motionEvent.x, motionEvent.y)
      }
      return@setOnTouchListener false
    }
  }
  /**
   * Called when an image is clicked.
   *
   * @param view the original view on which the tap/click occurs.
   * @param x the relative x coordinate according to image
   * @param y the relative y coordinate according to image
   */
  private fun onPhotoTap(x: Float, y: Float) {
    // Show default region for non-accessibility cases and this will be only called when user taps on unspecified region.
    if (!imageView.isAccessibilityEnabled()) {
      resetRegionSelectionViews()
      val defaultRegion = parentView.findViewById<View>(R.id.default_selected_region)
      defaultRegion.setBackgroundResource(R.drawable.selected_region_background)
      defaultRegion.x = x
      defaultRegion.y = y
      listener.onClickableAreaTouched(DefaultRegionClickedEvent())
    }
  }

  /** Function to remove the background from the views.*/
  private fun resetRegionSelectionViews() {
    parentView.forEachIndexed { index: Int, childView: View ->
      // Remove any previously selected region excluding 0th index(image view)
      if (index > 0) {
        childView.setBackgroundResource(0)
      }
    }
  }

  /** Get X co-ordinate scaled according to image.*/
  private fun getXCoordinate(x: Float): Float {
    return x * getImageViewContentWidth()
  }

  /** Get Y co-ordinate scaled according to image.*/
  private fun getYCoordinate(y: Float): Float {
    return y * getImageViewContentHeight()
  }

  private fun getImageViewContentWidth(): Int {
    return imageView.width - imageView.paddingLeft - imageView.paddingRight
  }

  private fun getImageViewContentHeight(): Int {
    return imageView.height - imageView.paddingTop - imageView.paddingBottom
  }

  /** Add selectable regions to [FrameLayout].*/
  fun addRegionViews() {
    parentView.let {
      if (it.childCount > 2) {
        it.removeViews(2, it.childCount - 1) // remove all other views
      }
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
        newView.contentDescription = clickableArea.regionDescription
        newView.setOnTouchListener { _, event ->
          if (event.action == MotionEvent.ACTION_DOWN) {
            showOrHideRegion(newView, clickableArea)
          }
          return@setOnTouchListener true
        }
        if (imageView.isAccessibilityEnabled()) {
          // Make default region visibility gone when talkback enabled to avoid any accidental touch.
          val defaultRegion = parentView.findViewById<View>(R.id.default_selected_region)
          defaultRegion.isVisible = false
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
    resetRegionSelectionViews()
    listener.onClickableAreaTouched(NamedRegionClickedEvent(clickableArea.label))
    newView.setBackgroundResource(R.drawable.selected_region_background)
  }
}
