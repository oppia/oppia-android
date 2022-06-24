package org.oppia.android.app.spotlight

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import org.oppia.android.databinding.OverlayBinding

class SpotlightOverlayPositionAutomator(private val anchor: View) {

  //  these will determine what arrow resource we will use (top, bottom, left, right facing)
  private lateinit var overlayHintVerticalPosition: OverlayHintVerticalPosition
  private lateinit var overlayHintHorizontalPosition: OverlayHintHorizontalPosition

  private fun getAnchorTopMargin(): Int {
    return anchor.top
  }

  private fun getAnchorBottomMargin(): Int {
    return anchor.bottom
  }

  private fun getAnchorLeftMargin(): Int {
    return anchor.left
  }

  private fun getAnchorRightMargin(): Int {
    return anchor.right
  }

  private fun getAnchorHeight(): Int {
    return anchor.height
  }

  private fun getAnchorWidth(): Int {
    return anchor.width
  }

//  private fun getArrowTopMargin(): Int {
//      return 0
//  TODO: this is only possible after reources can be accessed from this class. Not a necessity currently
//  }

//  private fun getArrowHeight(): Int{
//    resources.getDimensionPixelSize(R.dimen.arrow_height)
//  }

  private fun calculateOverlayVerticalHintPosition() {
    overlayHintVerticalPosition = if (getAnchorBottomMargin() > getAnchorTopMargin()) {
      OverlayHintVerticalPosition.UNDER
    } else OverlayHintVerticalPosition.OVER
  }

  private fun calculateOverlayHorizontalHintPosition() {
    overlayHintHorizontalPosition = if (getAnchorLeftMargin() > getAnchorRightMargin()) {
      OverlayHintHorizontalPosition.RIGHT
    } else OverlayHintHorizontalPosition.LEFT
  }

  fun calculateArrowTopMargin(): Int {
    calculateOverlayVerticalHintPosition()
    return if (overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER) {
      getAnchorTopMargin() + getAnchorHeight()
    } else {
      getAnchorBottomMargin() + getAnchorHeight()
    }
  }

  fun setConstraints(binding: OverlayBinding) {
    val set = ConstraintSet()

    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT && overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER){
      set.connect(
        binding.customText.id,
        ConstraintSet.RIGHT,
        binding.arrow.id,
        ConstraintSet.RIGHT,
        0
      )
      set.connect(
        binding.customText.id,
        ConstraintSet.BOTTOM,
        binding.arrow.id,
        ConstraintSet.TOP,
        0
      )
    }
    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT && overlayHintVerticalPosition == OverlayHintVerticalPosition.OVER){
      set.connect(
        binding.customText.id,
        ConstraintSet.RIGHT,
        binding.arrow.id,
        ConstraintSet.RIGHT,
        0
      )
      set.connect(
        binding.customText.id,
        ConstraintSet.TOP,
        binding.arrow.id,
        ConstraintSet.BOTTOM,
        0
      )
    }
    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.LEFT || overlayHintVerticalPosition == OverlayHintVerticalPosition.OVER){
      set.connect(
        binding.customText.id,
        ConstraintSet.LEFT,
        binding.arrow.id,
        ConstraintSet.LEFT,
        0
      )
      set.connect(
        binding.customText.id,
        ConstraintSet.BOTTOM,
        binding.arrow.id,
        ConstraintSet.BOTTOM,
        0
      )
    }
    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.LEFT || overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER){
      set.connect(
        binding.customText.id,
        ConstraintSet.LEFT,
        binding.arrow.id,
        ConstraintSet.LEFT,
        0
      )
      set.connect(
        binding.customText.id,
        ConstraintSet.BOTTOM,
        binding.arrow.id,
        ConstraintSet.TOP,
        0
      )
    }
  }

  fun calculateArrowLeftMargin(): Int {
    calculateOverlayHorizontalHintPosition()
    return if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT) {
      getAnchorLeftMargin() + getAnchorWidth()
    } else {
      getAnchorLeftMargin() + 10
    }
  }

  sealed class OverlayHintVerticalPosition {
    object OVER : OverlayHintVerticalPosition()
    object UNDER : OverlayHintVerticalPosition()
  }

  sealed class OverlayHintHorizontalPosition {
    object RIGHT : OverlayHintHorizontalPosition()
    object LEFT : OverlayHintHorizontalPosition()
  }
}