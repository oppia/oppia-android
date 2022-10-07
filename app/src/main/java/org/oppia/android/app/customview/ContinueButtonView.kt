package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.R
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.android.app.utility.LifecycleSafeTimerFactory
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.EnableContinueButtonAnimation
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject
import org.oppia.android.domain.oppialogger.OppiaLogger

class ContinueButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = R.style.StateButtonActive
) : androidx.appcompat.widget.AppCompatButton(context, attrs, defStyleAttr) {

  @field:[Inject EnableContinueButtonAnimation]
  lateinit var enableContinueButtonAnimation: PlatformParameterValue<Boolean>

  @Inject
  lateinit var fragment: Fragment

  @Inject
  lateinit var oppiaClock: OppiaClock

  @Inject
  lateinit var explorationProgressController: ExplorationProgressController

  @Inject
  lateinit var lifecycleSafeTimerFactory: LifecycleSafeTimerFactory

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  private lateinit var viewModel: ContinueInteractionViewModel
  private var isAnimationTimerFinished = false

  val animateContinueButton = MutableLiveData(false)

  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralState>> by lazy {
    explorationProgressController.getCurrentState().toLiveData()
  }

  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(fragment) { result ->
      processEphemeralStateResult(result)
    }
  }

  private fun processEphemeralStateResult(result: AsyncResult<EphemeralState>) {
    when (result) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("StateFragment", "Failed to retrieve ephemeral state", result.error)
      }
      is AsyncResult.Pending -> {} // Display nothing until a valid result is available.
      is AsyncResult.Success -> processEphemeralState(result.value)
    }
  }

  private fun processEphemeralState(ephemeralState: EphemeralState) {
    if (!ephemeralState.showContinueButtonAnimation) {
      animateContinueButton.value = false
    } else {
      val timeLeftToAnimate =
        ephemeralState.continueButtonAnimationTimestamp - oppiaClock.getCurrentTimeMs()
      if (timeLeftToAnimate < 0) {
        animateContinueButton.value = true
      } else {
        lifecycleSafeTimerFactory.createTimer(timeLeftToAnimate).observe(fragment) {
          animateContinueButton.value = true
        }
      }
    }
  }

  fun setViewModel(viewModel: ContinueInteractionViewModel) {
    this.viewModel = viewModel
  }

  private fun startAnimating() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.scale_button_size)
    if (enableContinueButtonAnimation.value) {
      this.startAnimation(animation)
    }
  }

  override fun onVisibilityAggregated(isVisible: Boolean) {
    super.onVisibilityAggregated(isVisible)
    // When the continue button is off-screen initially, the animation does not start even after the
    // timer goes off. To make sure that the animation is shown when the user finally scrolls down
    // enough that the button is visible, animate the button based on whether the timer has finished
    // or not.
    if (isVisible && isAnimationTimerFinished) {
      startAnimating()
      animateContinueButton.removeObservers(fragment)
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    val viewComponentFactory =
      FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)

    subscribeToCurrentState()
    animateContinueButton.observe(fragment) {
      if (it) {
        startAnimating()
        isAnimationTimerFinished = true
      } else {
        this.clearAnimation()
        isAnimationTimerFinished = false
      }
    }
  }
}
