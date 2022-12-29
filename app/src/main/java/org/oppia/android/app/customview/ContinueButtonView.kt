package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import org.oppia.android.R
import org.oppia.android.app.utility.lifecycle.LifecycleSafeTimerFactory
import org.oppia.android.app.view.ViewComponentFactory
import org.oppia.android.app.view.ViewComponentImpl
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.platformparameter.EnableContinueButtonAnimation
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** A custom [AppCompatButton] used to show continue button animations. */
class ContinueButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = R.style.StateButtonActive
) : androidx.appcompat.widget.AppCompatButton(context, attrs, defStyleAttr) {

  @field:[Inject EnableContinueButtonAnimation]
  lateinit var enableContinueButtonAnimation: PlatformParameterValue<Boolean>
  @Inject lateinit var fragment: Fragment
  @Inject lateinit var oppiaClock: OppiaClock
  @Inject lateinit var lifecycleSafeTimerFactory: LifecycleSafeTimerFactory
  @Inject lateinit var oppiaLogger: OppiaLogger

  private var shouldAnimateContinueButtonLateinit: Boolean? = null
  private val shouldAnimateContinueButton: Boolean
    get() = checkNotNull(shouldAnimateContinueButtonLateinit) {
      "Expected shouldAnimateContinueButtonLateinit to be initialized by this point."
    }

  private var continueButtonAnimationTimestampMsLateinit: Long? = null
  private val continueButtonAnimationTimestampMs: Long
    get() = checkNotNull(continueButtonAnimationTimestampMsLateinit) {
      "Expected continueButtonAnimationTimestampMsLateinit to be initialized by this point."
    }

  private val hasAnimationTimerFinished: Boolean
    get() = continueButtonAnimationTimestampMs < oppiaClock.getCurrentTimeMs()

  private var animationStartTimer: LiveData<Any>? = null
  private var currentAnimationReuseCount = 0

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    val viewComponentFactory =
      FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory
    val viewComponent = viewComponentFactory.createViewComponent(this) as ViewComponentImpl
    viewComponent.inject(this)
    maybeInitializeAnimation()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    // Make sure state can't leak across rebinding boundaries (since this view may be reused).
    cancelOngoingTimer()
  }

  /** Sets whether the view should animate to catch a user's attention. */
  fun setShouldAnimateContinueButton(shouldAnimateContinueButton: Boolean) {
    shouldAnimateContinueButtonLateinit = shouldAnimateContinueButton
    maybeInitializeAnimation()
  }

  /**
   * Sets when, in clock time, the animation controlled by [setShouldAnimateContinueButton] should
   * play.
   */
  fun setContinueButtonAnimationTimestampMs(continueButtonAnimationTimestampMs: Long) {
    continueButtonAnimationTimestampMsLateinit = continueButtonAnimationTimestampMs
    maybeInitializeAnimation()
  }

  private fun maybeInitializeAnimation() {
    if (::oppiaClock.isInitialized &&
      shouldAnimateContinueButtonLateinit != null &&
      continueButtonAnimationTimestampMsLateinit != null
    ) {
      when {
        !shouldAnimateContinueButton -> clearAnimation()
        hasAnimationTimerFinished -> startAnimating()
        else -> {
          val timeLeftToAnimate = continueButtonAnimationTimestampMs - oppiaClock.getCurrentTimeMs()
          startAnimatingWithDelay(delayMs = timeLeftToAnimate)
        }
      }
    }
  }

  private fun startAnimatingWithDelay(delayMs: Long) {
    cancelOngoingTimer()
    val sequenceNumber = currentAnimationReuseCount
    lifecycleSafeTimerFactory.createTimer(delayMs).observe(fragment) {
      // Only play the animation if it's still valid to do so (since the view may have been recycled
      // for a new context that may not want the animation to play).
      if (sequenceNumber == currentAnimationReuseCount) {
        startAnimating()
      }
    }
  }

  private fun cancelOngoingTimer() {
    currentAnimationReuseCount++
    animationStartTimer?.let {
      it.removeObservers(fragment)
      animationStartTimer = null
    }
  }

  private fun startAnimating() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.scale_button_size)
    if (enableContinueButtonAnimation.value) {
      startAnimation(animation)
       lifecycleSafeTimerFactory.createTimer(8000).observe(fragment) {
          startAnimating()
      }
    }
  }
}
