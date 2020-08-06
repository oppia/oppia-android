package org.oppia.app.databinding;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.BindingAdapter;

public class ViewBindingAdapter {

  private static ValueAnimator appearAnimator = ValueAnimator.ofFloat(0f, 1f);
  private static ValueAnimator disappearAnimator = ValueAnimator.ofFloat(1f, 2f);
  private static AnimatorSet animatorSet = new AnimatorSet();

  @BindingAdapter("app:flashingAnimation")
  public static void setFlashingAnimation(View view, Boolean isFlashing) {
    appearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      public void onAnimationUpdate(ValueAnimator animation) {
        view.setScaleX((float) animation.getAnimatedValue());
        view.setScaleY((float) animation.getAnimatedValue());
        view.setAlpha((float) animation.getAnimatedValue());
      }
    });

    appearAnimator.setDuration(1500);

    disappearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      public void onAnimationUpdate(ValueAnimator animation) {
        view.setScaleX((float) animation.getAnimatedValue());
        view.setScaleY((float) animation.getAnimatedValue());
        view.setAlpha(2f -  (float) animation.getAnimatedValue());
      }
    });

    disappearAnimator.setDuration(500);

    if (isFlashing) {
      animatorSet.playSequentially(appearAnimator, disappearAnimator);
      animatorSet.start();

      animatorSet.addListener(new AnimatorListenerAdapter() {
        public void onAnimationEnd(ValueAnimator animatorSet)
        {
          animatorSet.start();
        }
      });
    } else {
      animatorSet.cancel();
      view.setScaleX(0f);
      view.setScaleY(0f);
      view.setAlpha(0f);
    }
  }

  /** BindingAdapter to set the height of a View.*/
  @BindingAdapter("android:layout_height")
  public static void setLayoutHeight(View view, Float height) {
    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
    layoutParams.height = height.intValue();
    view.setLayoutParams(layoutParams);
  }

  @BindingAdapter( value = {
      "app:isRotationAnimationClockwise",
      "app:rotationAnimationAngle"},
      requireAll = true
  )
  public static void setRotationAnimation(View view, Boolean isClockwise, Float angle) {
    if (isClockwise) {
      ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, angle);
      valueAnimator.setDuration(300);

      valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
          view.setRotation((float) animation.getAnimatedValue());
        }
      });

      valueAnimator.start();
    } else {
      ValueAnimator valueAnimator = ValueAnimator.ofFloat(angle, 0f);
      valueAnimator.setDuration(300);

      valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
          view.setRotation((float) animation.getAnimatedValue());
        }
      });

      valueAnimator.start();
    }
  }
}
