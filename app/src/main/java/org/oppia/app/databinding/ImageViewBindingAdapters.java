package org.oppia.app.databinding;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.jetbrains.annotations.NotNull;
import org.oppia.app.R;
import org.oppia.app.model.LessonThumbnailGraphic;
import org.oppia.app.model.ProfileAvatar;

public final class ImageViewBindingAdapters {
  /**
   * Allows binding drawables to an [ImageView] via "android:src". Source: https://stackoverflow.com/a/35809319/3689782.
   */
  @BindingAdapter("android:src")
  public static void setImageDrawable(@NotNull ImageView imageView, String imageUrl) {
    RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.review_placeholder);

    Glide.with(imageView.getContext())
        .load(imageUrl)
        .apply(requestOptions)
        .into(imageView);
  }

  /**
   * Allows binding drawables to an [ImageView] via "android:src". Source: https://stackoverflow.com/a/35809319/3689782.
   */
  @BindingAdapter("android:src")
  public static void setImageDrawable(@NotNull ImageView imageView, @DrawableRes int drawableResourceId) {
    imageView.setImageResource(drawableResourceId);
  }

  /**
   * Binds the specified [LessonThumbnailGraphic] as the source for the [ImageView]. The view should be specified to have
   * no width/height (when sized in a constraint layout), and use centerCrop for the image to appear correctly.
   */
  @BindingAdapter("android:src")
  public static void setImageDrawable(ImageView imageView, LessonThumbnailGraphic thumbnailGraphic) {
    int drawableResourceId;
    switch (thumbnailGraphic) {
      case BAKER:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_baker;
      case CHILD_WITH_BOOK:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_child_with_book;
      case CHILD_WITH_CUPCAKES:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_child_with_cupcakes;
      case CHILD_WITH_FRACTIONS_HOMEWORK:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework;
      case DUCK_AND_CHICKEN:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_duck_and_chicken;
      case PERSON_WITH_PIE_CHART:
        drawableResourceId = R.drawable.lesson_thumbnail_graphic_person_with_pie_chart;
      case IDENTIFYING_THE_PARTS_OF_A_FRACTION:
        drawableResourceId = R.drawable.topic_fractions_01;
      case WRITING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_02;
      case EQUIVALENT_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_03;
      case MIXED_NUMBERS_AND_IMPROPER_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_04;
      case COMPARING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_05;
      case ADDING_AND_SUBTRACTING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_06;
      case MULTIPLYING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_07;
      case DIVIDING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_08;
      case DERIVE_A_RATIO:
        drawableResourceId = R.drawable.topic_ratios_01;
      case WHAT_IS_A_FRACTION:
        drawableResourceId = R.drawable.topic_fractions_01;
      case FRACTION_OF_A_GROUP:
        drawableResourceId = R.drawable.topic_fractions_02;
      case ADDING_FRACTIONS:
        drawableResourceId = R.drawable.topic_fractions_03;
      case MIXED_NUMBERS:
        drawableResourceId = R.drawable.topic_fractions_04;
      default:
        drawableResourceId = R.drawable.topic_fractions_01;
    }
    setImageDrawable(
        imageView,
        drawableResourceId
    );
  }

  //TODO: Figure out translation
  /**
   * Binding adapter for profile images. Used to either display a local image or custom colored avatar.
   *  @param imageView View where the profile avatar will be loaded into.
   * @param profileAvatar Represents either a colorId or local image uri.
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
              public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
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
