package org.oppia.app.databinding

import android.animation.ValueAnimator
import android.view.View
import androidx.databinding.BindingAdapter

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
