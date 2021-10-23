package org.oppia.android.app.databinding;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import org.oppia.android.R;
import org.oppia.android.app.model.ChapterPlayState;
import org.oppia.android.app.model.ProfileAvatar;

/** Holds all custom binding adapters that bind to [ImageView]. */
public final class ImageViewBindingAdapters {
  @BindingAdapter("app:srcCompat")
  public static void setImageDrawableCompat(
      @NonNull ImageView imageView,
      @DrawableRes int drawableResourceId
  ) {
    imageView.setImageResource(drawableResourceId);
  }

  @BindingAdapter("app:srcCompat")
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
  @BindingAdapter("profile:src")
  public static void setProfileImage(ImageView imageView, ProfileAvatar profileAvatar) {
    if (profileAvatar != null) {
      if (profileAvatar.getAvatarTypeCase() == ProfileAvatar.AvatarTypeCase.AVATAR_COLOR_RGB) {
        Glide.with(imageView.getContext())
            .load(R.drawable.ic_default_avatar)
            .listener(new RequestListener<Drawable>() {
              @Override
              public boolean onLoadFailed(
                  GlideException e,
                  Object model,
                  Target<Drawable> target,
                  boolean isFirstResource) {
                return false;
              }

              @Override
              public boolean onResourceReady(
                  Drawable resource,
                  Object model,
                  Target<Drawable> target,
                  DataSource dataSource,
                  boolean isFirstResource
              ) {
                imageView.setColorFilter(
                    profileAvatar.getAvatarColorRgb(),
                    PorterDuff.Mode.DST_OVER
                );
                return false;
              }
            }).into(imageView);
      } else {
        Glide.with(imageView.getContext())
            .load(profileAvatar.getAvatarImageUri())
            .placeholder(R.drawable.ic_default_avatar)
            .into(imageView);
      }
    }
  }

  /**
   * Binds a drawable indicated by {@link ChapterPlayState} to an {@link ImageView} via
   * "android:src".
   * <p/>
   * Reference: https://stackoverflow.com/a/35809319/3689782.
   */
  @BindingAdapter("playState:image")
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
