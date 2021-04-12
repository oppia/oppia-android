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
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import org.oppia.android.R;
import org.oppia.android.app.model.LessonThumbnailGraphic;
import org.oppia.android.app.model.ProfileAvatar;

/** Holds all custom binding adapters that bind to [ImageView]. */
public final class ImageViewBindingAdapters {
  /**
   * Allows binding drawables to an [ImageView] via "android:src".
   * Reference: https://stackoverflow.com/a/35809319/3689782.
   */
  @BindingAdapter("android:src")
  public static void setImageDrawable(@NonNull ImageView imageView, String imageUrl) {
    RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.review_placeholder);
    Glide.with(imageView.getContext())
        .load(imageUrl)
        .apply(requestOptions)
        .into(imageView);
  }

  /**
   * Allows binding drawables to an [ImageView] via "android:src".
   * Reference: https://stackoverflow.com/a/35809319/3689782.
   */
  @BindingAdapter("android:src")
  public static void setImageDrawable(
      @NonNull ImageView imageView,
      @DrawableRes int drawableResourceId
  ) {
    imageView.setImageResource(drawableResourceId);
  }

  /**
   * Binds the specified [LessonThumbnailGraphic] as the source for the [ImageView].
   * <p/>
   * The view should be specified to have no width/height (when sized in a constraint layout), and
   * use centerCrop for the image to appear correctly.
   */
  @BindingAdapter("android:src")
  public static void setImageDrawable(
      ImageView imageView,
      LessonThumbnailGraphic thumbnailGraphic
  ) {
    int drawableResourceId;
    switch (thumbnailGraphic) {
      case BAKER:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_baker;
        break;
      case CHILD_WITH_BOOK:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_child_with_book;
        break;
      case CHILD_WITH_CUPCAKES:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_child_with_cupcakes;
        break;
      case CHILD_WITH_FRACTIONS_HOMEWORK:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework;
        break;
      case DUCK_AND_CHICKEN:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_duck_and_chicken;
        break;
      case PERSON_WITH_PIE_CHART:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_person_with_pie_chart;
        break;
      case IDENTIFYING_THE_PARTS_OF_A_FRACTION:
        drawableResourceId = R.drawable.topic_fractions_01;
        break;
      case WRITING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_02;
        break;
      case EQUIVALENT_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_03;
        break;
      case MIXED_NUMBERS_AND_IMPROPER_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_04;
        break;
      case COMPARING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_05;
        break;
      case ADDING_AND_SUBTRACTING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_06;
        break;
      case MULTIPLYING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_07;
        break;
      case DIVIDING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_08;
        break;
      case DERIVE_A_RATIO:
        drawableResourceId = R.drawable.topic_ratios_01;
        break;
      case WHAT_IS_A_FRACTION:
        drawableResourceId = R.drawable.topic_fractions_01;
        break;
      case FRACTION_OF_A_GROUP:
        drawableResourceId = R.drawable.topic_fractions_02;
        break;
      case ADDING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_03;
        break;
      case MIXED_NUMBERS:
        drawableResourceId = R.drawable.topic_fractions_04;
        break;
      default:
        drawableResourceId = R.drawable.topic_fractions_01;
    }
    setImageDrawable(
        imageView,
        drawableResourceId
    );
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
}
