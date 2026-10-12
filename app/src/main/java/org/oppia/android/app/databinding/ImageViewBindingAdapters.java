package org.oppia.android.app.databinding;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.Glide;
import org.oppia.android.app.databinding.adapters.R;
import org.oppia.android.app.model.ChapterPlayState;
import org.oppia.android.app.model.ProfileAvatar;

/** Holds all custom binding adapters that bind to [ImageView]. */
public final class ImageViewBindingAdapters {
  @BindingAdapter("srcCompat")
  public static void setImageDrawableCompat(
      @NonNull ImageView imageView,
      @DrawableRes int drawableResourceId
  ) {
    imageView.setImageResource(drawableResourceId);
  }

  @BindingAdapter("srcCompat")
  public static void setImageDrawableCompat(
      @NonNull ImageView imageView,
      Drawable drawable
  ) {
    imageView.setImageDrawable(drawable);
  }

  /**
   * Binding adapter for profile images. Used to either display a local image or custom
   * colored avatar.
   *
   * @param imageView view where the profile avatar will be loaded into
   * @param profileAvatar represents either a colorId or local image uri
   */
  @BindingAdapter("profileImageSource")
  public static void setProfileImage(ImageView imageView, ProfileAvatar profileAvatar) {
    if (profileAvatar != null) {
      if (profileAvatar.getAvatarTypeCase() == ProfileAvatar.AvatarTypeCase.AVATAR_COLOR_RGB) {
        imageView.setImageResource(R.drawable.ic_default_avatar);
        imageView.setColorFilter(
                profileAvatar.getAvatarColorRgb(),
                PorterDuff.Mode.DST_OVER
        );
      } else {
        Glide.with(imageView.getContext())
            .load(profileAvatar.getAvatarImageUri())
            .placeholder(R.drawable.ic_default_avatar)
            .into(imageView);
      }
    }
  }

  /**
   * Applies the specified color tint to the ImageView.
   *
   * @param imageView the view to apply the tint on
   * @param color the resolved color int to apply as tint
   */
  @BindingAdapter("tint")
  public static void setTint(@NonNull ImageView imageView, int color) {
    imageView.setColorFilter(color, PorterDuff.Mode.SRC_IN);
  }

  /**
   * Binds a drawable indicated by {@link ChapterPlayState} to an {@link ImageView} via
   * "android:src".
   * <p/>
   * Reference: https://stackoverflow.com/a/35809319/3689782.
   */
  @BindingAdapter("playStateImage")
  public static void setPlayStateDrawable(
      @NonNull ImageView imageView,
      ChapterPlayState chapterPlayState
  ) {
    switch (chapterPlayState) {
      case COMPLETED:
        setImageDrawableCompat(imageView, R.drawable.circular_solid_color_primary_32dp);
        break;
      case NOT_STARTED:
      case STARTED_NOT_COMPLETED:
        setImageDrawableCompat(imageView, R.drawable.circular_stroke_2dp_color_primary_32dp);
        break;
      default:
        setImageDrawableCompat(imageView, R.drawable.circular_stroke_2dp_grey_32dp);
    }
  }
}
