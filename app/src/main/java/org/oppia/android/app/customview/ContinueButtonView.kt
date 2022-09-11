package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.player.state.itemviewmodel.ContinueInteractionViewModel

class ContinueButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = R.style.StateButtonActive
) : androidx.appcompat.widget.AppCompatButton(context, attrs, defStyleAttr) {

  private lateinit var viewModel: ContinueInteractionViewModel

  fun setViewModel(viewModel: ContinueInteractionViewModel) {
    this.viewModel = viewModel
    viewModel.subscribeToCurrentState()
    viewModel.animateContinueButton.observeForever(myObserver)
  }

  private val myObserver = Observer<Boolean> {
    if (it) {
      val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
//      animation.interpolator = BounceInterpolator()
      animation.repeatCount = Int.MAX_VALUE
      this.startAnimation(animation)
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    viewModel.animateContinueButton.removeObserver(myObserver)
  }

}
