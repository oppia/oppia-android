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

        Glide.with(imageView.context)
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
        setImageDrawable(
            imageView,
            switch (thumbnailGraphic) {
                case LessonThumbnailGraphic.BAKER:
                    R.drawable.lesson_thumbnail_graphic_baker;
                case LessonThumbnailGraphic.CHILD_WITH_BOOK:
                    R.drawable.lesson_thumbnail_graphic_child_with_book;
                case LessonThumbnailGraphic.CHILD_WITH_CUPCAKES:
                    R.drawable.lesson_thumbnail_graphic_child_with_cupcakes;
                case LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK:
                    R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework;
                case LessonThumbnailGraphic.DUCK_AND_CHICKEN:
                    R.drawable.lesson_thumbnail_graphic_duck_and_chicken;
                case LessonThumbnailGraphic.PERSON_WITH_PIE_CHART:
                    R.drawable.lesson_thumbnail_graphic_person_with_pie_chart;
                case LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION:
                    R.drawable.topic_fractions_01;
                case LessonThumbnailGraphic.WRITING_FRACTIONS:
                    R.drawable.topic_fractions_02;
                case LessonThumbnailGraphic.EQUIVALENT_FRACTIONS:
                    R.drawable.topic_fractions_03;
                case LessonThumbnailGraphic.MIXED_NUMBERS_AND_IMPROPER_FRACTIONS:
                    R.drawable.topic_fractions_04;
                case LessonThumbnailGraphic.COMPARING_FRACTIONS:
                    R.drawable.topic_fractions_05;
                case LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS:
                    R.drawable.topic_fractions_06;
                case LessonThumbnailGraphic.MULTIPLYING_FRACTIONS:
                    R.drawable.topic_fractions_07;
                case LessonThumbnailGraphic.DIVIDING_FRACTIONS:
                    R.drawable.topic_fractions_08;
                case LessonThumbnailGraphic.DERIVE_A_RATIO:
                    R.drawable.topic_ratios_01;
                case LessonThumbnailGraphic.WHAT_IS_A_FRACTION:
                    R.drawable.topic_fractions_01;
                case LessonThumbnailGraphic.FRACTION_OF_A_GROUP:
                    R.drawable.topic_fractions_02;
                case LessonThumbnailGraphic.ADDING_FRACTIONS:
                    R.drawable.topic_fractions_03;
                case LessonThumbnailGraphic.MIXED_NUMBERS:
                    R.drawable.topic_fractions_04;
                default:
                    R.drawable.topic_fractions_01;
            }
        );
    }

    //TODO: Figure out translation
    /**
     * Binding adapter for profile images. Used to either display a local image or custom colored avatar.
     *
     * @param imageView View where the profile avatar will be loaded into.
     * @param profileAvatar Represents either a colorId or local image uri.
     */
    @BindingAdapter("profile:src")
    public static Boolean setProfileImage(ImageView imageView, ProfileAvatar profileAvatar) {
        if (profileAvatar != null) {
            if (profileAvatar.avatarTypeCase == ProfileAvatar.AvatarTypeCase.AVATAR_COLOR_RGB) {
                Glide.with(imageView.context).load(R.drawable.ic_default_avatar).listener(object : RequestListener<Drawable> {

                    public Boolean onLoadFailed(
                        GlideException e,
                        Object model,
                        Target<Drawable> target,
                        Boolean isFirstResource) {
                        return false;
                    }

                    public Boolean onResourceReady(
                        Drawable resource,
                        Object model,
                        Target<Drawable> target,
                        DataSource dataSource,
                        Boolean isFirstResource) {
                            imageView.setColorFilter(
                                profileAvatar.avatarColorRgb,
                                PorterDuff.Mode.DST_OVER
                            );
                            return false;
                        }
                }).into(imageView);
            } else {
                Glide.with(imageView.context)
                    .load(profileAvatar.avatarImageUri)
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(imageView);
            }
        }
    }
}