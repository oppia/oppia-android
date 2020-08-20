package org.oppia.app.databinding;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

/**
 * Holds all custom binding adapters that set miscellaneous values.
 */
public final class ViewBindingAdapters {

  /**
   * BindingAdapter to set the height of a View.
   */
  @BindingAdapter("android:layout_height")
  public static void setLayoutHeight(@NonNull View view, float height) {
    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
    layoutParams.height = (int) height;
    view.setLayoutParams(layoutParams);
  }

  @BindingAdapter(
      value = {
          "app:isRotationAnimationClockwise",
          "app:rotationAnimationAngle"
      },
      requireAll = true
  )
  public static void setRotationAnimation(View view, boolean isClockwise, float angle) {
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
