package org.oppia.app.databinding

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.databinding.BindingAdapter

private val appearAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
private val disappearAnimator: ValueAnimator = ValueAnimator.ofFloat(1f, 2f)
private val animatorSet = AnimatorSet()

@BindingAdapter("app:flashingAnimation")
fun setFlashingAnimation(view: View, isFlashing: Boolean) {
  appearAnimator.addUpdateListener {
    view.scaleX = it.animatedValue as Float
    view.scaleY = it.animatedValue as Float
    view.alpha = it.animatedValue as Float
  }
  appearAnimator.duration = 1500

  disappearAnimator.addUpdateListener {
    view.scaleX = it.animatedValue as Float
    view.scaleY = it.animatedValue as Float
    view.alpha = 2f - it.animatedValue as Float
  }
  disappearAnimator.duration = 500

  if (isFlashing) {
    animatorSet.playSequentially(appearAnimator, disappearAnimator)
    animatorSet.start()
    animatorSet.doOnEnd {
      animatorSet.start()
    }
  } else {
    animatorSet.cancel()
    view.scaleX = 0f
    view.scaleY = 0f
    view.alpha = 0f
  }
}

/** BindingAdapter to set the height of a View.*/
@BindingAdapter("android:layout_height")
fun setLayoutHeight(view: View, height: Float) {
  val layoutParams = view.layoutParams
  layoutParams.height = height.toInt()
  view.layoutParams = layoutParams
}

@BindingAdapter(
  "app:isRotationAnimationClockwise",
  "app:rotationAnimationAngle",
  requireAll = true
)
fun setRotationAnimation(view: View, isClockwise: Boolean, angle: Float) {
  if (isClockwise) {
    val valueAnimator = ValueAnimator.ofFloat(0f, angle)
    valueAnimator.duration = 300
    valueAnimator.addUpdateListener {
      view.rotation = it.animatedValue as Float
    }
    valueAnimator.start()
  } else {
    val valueAnimator = ValueAnimator.ofFloat(angle, 0f)
    valueAnimator.duration = 300
    valueAnimator.addUpdateListener {
      view.rotation = it.animatedValue as Float
    }
    valueAnimator.start()
  }
}

