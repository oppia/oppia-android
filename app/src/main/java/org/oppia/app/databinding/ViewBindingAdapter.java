package org.oppia.app.databinding;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.databinding.BindingAdapter;

public class ViewBindingAdapter {

  private static ValueAnimator appearAnimator = ValueAnimator.ofFloat(0f, 1f);
  private static ValueAnimator disappearAnimator = ValueAnimator.ofFloat(1f, 2f);
  private static AnimatorSet animatorSet = new AnimatorSet();

  @BindingAdapter("app:flashingAnimation")
  public static void setFlashingAnimation(View view, Boolean isFlashing) {
    ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener(); {
        view.scaleX = (Float) it.animatedValue;
    view.scaleY = (Float) it.animatedValue;
    view.alpha = (Float) it.animatedValue;
    }
    appearAnimator.addUpdateListener(animatorUpdateListener);
    appearAnimator.duration = 1500

    disappearAnimator.addUpdateListener {
      view.scaleX = it.animatedValue as Float;
      view.scaleY = it.animatedValue as Float;
      view.alpha = 2f - it.animatedValue as Float;
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
  public static void setLayoutHeight(View view, Float height) {
    val layoutParams = view.layoutParams
    layoutParams.height = height.toInt()
    view.layoutParams = layoutParams
  }

  @BindingAdapter(
      "app:isRotationAnimationClockwise",
      "app:rotationAnimationAngle",
      requireAll = true
  )
  public static void setRotationAnimation(View view, Boolean isClockwise, Float angle) {
    if (isClockwise) {
      val valueAnimator = ValueAnimator.ofFloat(0f, angle);
      valueAnimator.setDuration(300);
      valueAnimator.addUpdateListener {
        view.rotation = it.animatedValue as Float;
      }
      valueAnimator.start()
    } else {
      val valueAnimator = ValueAnimator.ofFloat(angle, 0f);
      valueAnimator.duration = 300
      valueAnimator.addUpdateListener {
        view.rotation = (Float) it.animatedValue;
      }
      valueAnimator.start();
    }
  }
}
