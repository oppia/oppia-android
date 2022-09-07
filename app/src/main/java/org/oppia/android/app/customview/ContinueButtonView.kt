package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import org.oppia.android.R
import org.oppia.android.app.player.state.itemviewmodel.ContinueInteractionViewModel

class ContinueButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = R.style.StateButtonActive
) : androidx.appcompat.widget.AppCompatButton(context, attrs, defStyleAttr) {

  private lateinit var viewModel: ContinueInteractionViewModel

//  @Inject
//  private lateinit var lifecycleSafeTimerFactory: LifecycleSafeTimerFactory

   fun setViewModel(viewModel: ContinueInteractionViewModel) {
    this.viewModel = viewModel
     startAnimating()
  }

  fun startAnimating() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.expand)
    animation.interpolator = BounceInterpolator()
    animation.repeatCount = Int.MAX_VALUE
    this.startAnimation(animation)
  }


  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
  }

}