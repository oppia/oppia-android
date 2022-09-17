package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.player.state.itemviewmodel.ContinueInteractionViewModel

class ContinueButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = R.style.StateButtonActive
) : androidx.appcompat.widget.AppCompatButton(context, attrs, defStyleAttr) {

  private lateinit var viewModel: ContinueInteractionViewModel
  private var isAnimationTimerFinished = false

  fun setViewModel(viewModel: ContinueInteractionViewModel) {
    this.viewModel = viewModel
    viewModel.subscribeToCurrentState()
    viewModel.animateContinueButton.observeForever(continueButtonAnimationObserver)
  }

  private val continueButtonAnimationObserver = Observer<Boolean> {
    if (it) {
      startAnimating()
    } else {
      this.clearAnimation()
      isAnimationTimerFinished = false
    }
  }

  private fun startAnimating() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.expand)
    animation.interpolator = AccelerateInterpolator()
    animation.repeatCount = Int.MAX_VALUE
    this.startAnimation(animation)
    isAnimationTimerFinished = true
  }

  override fun onVisibilityAggregated(isVisible: Boolean) {
    super.onVisibilityAggregated(isVisible)
    // When the continue button is off-screen initially, the animation does not start even after the
    // timer goes off. To make sure that the animation is shown when the user finally scrolls down
    // enough that the button is visible, animate the button based on whether the timer has finished
    // or not.

    if (isVisible && isAnimationTimerFinished) {
      startAnimating()
      viewModel.animateContinueButton.removeObserver(continueButtonAnimationObserver)
    }
  }
}
