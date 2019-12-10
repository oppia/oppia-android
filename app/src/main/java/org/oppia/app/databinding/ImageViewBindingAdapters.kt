package org.oppia.app.databinding

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.oppia.app.R
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.SkillThumbnailGraphic

/**
 * Allows binding drawables to an [ImageView] via "android:src". Source: https://stackoverflow.com/a/35809319/3689782.
 */
@BindingAdapter("android:src")
fun setImageDrawable(imageView: ImageView, imageUrl: String) {
  val requestOptions = RequestOptions().placeholder(R.drawable.review_placeholder)

  Glide.with(imageView.context)
    .load(imageUrl)
    .apply(requestOptions)
    .into(imageView)
}

/**
 * Allows binding drawables to an [ImageView] via "android:src". Source: https://stackoverflow.com/a/35809319/3689782.
 */
@BindingAdapter("android:src")
fun setImageDrawable(imageView: ImageView, @DrawableRes drawableResourceId: Int) {
  imageView.setImageResource(drawableResourceId)
}

/**
 * Binds the specified [LessonThumbnailGraphic] as the source for the [ImageView]. The view should be specified to have
 * no width/height (when sized in a constraint layout), and use centerCrop for the image to appear correctly.
 */
@BindingAdapter("android:src")
fun setImageDrawable(imageView: ImageView, thumbnailGraphic: LessonThumbnailGraphic) {
  setImageDrawable(
    imageView, when (thumbnailGraphic) {
      LessonThumbnailGraphic.BAKER -> R.drawable.lesson_thumbnail_graphic_baker
      LessonThumbnailGraphic.CHILD_WITH_BOOK -> R.drawable.lesson_thumbnail_graphic_child_with_book
      LessonThumbnailGraphic.CHILD_WITH_CUPCAKES -> R.drawable.lesson_thumbnail_graphic_child_with_cupcakes
      LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK ->
        R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework
      LessonThumbnailGraphic.DUCK_AND_CHICKEN -> R.drawable.lesson_thumbnail_graphic_duck_and_chicken
      LessonThumbnailGraphic.PERSON_WITH_PIE_CHART -> R.drawable.lesson_thumbnail_graphic_person_with_pie_chart
      else -> R.drawable.lesson_thumbnail_default
    }
  )
}

/**
 * Binds the specified [SkillThumbnailGraphic] as the source for the [ImageView]. The view should be specified to have
 * no width/height (when sized in a constraint layout), and use centerCrop for the image to appear correctly.
 */
@BindingAdapter("android:src")
fun setImageDrawable(imageView: ImageView, thumbnailGraphic: SkillThumbnailGraphic) {
  setImageDrawable(
    imageView, when (thumbnailGraphic) {
      SkillThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION -> R.drawable.topic_fractions_01
      SkillThumbnailGraphic.WRITING_FRACTIONS -> R.drawable.topic_fractions_02
      SkillThumbnailGraphic.EQUIVALENT_FRACTIONS -> R.drawable.topic_fractions_03
      SkillThumbnailGraphic.MIXED_NUMBERS_AND_IMPROPER_FRACTIONS -> R.drawable.topic_fractions_04
      SkillThumbnailGraphic.COMPARING_FRACTIONS -> R.drawable.topic_fractions_05
      SkillThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS -> R.drawable.topic_fractions_06
      SkillThumbnailGraphic.MULTIPLYING_FRACTIONS -> R.drawable.topic_fractions_07
      SkillThumbnailGraphic.DIVIDING_FRACTIONS -> R.drawable.topic_fractions_08
      SkillThumbnailGraphic.DERIVE_A_RATIO -> R.drawable.topic_ratios_01
      else -> R.drawable.topic_fractions_01
    }
  )
}

@BindingAdapter("profile:src")
fun setProfileImage(imageView: ImageView, imageUrl: String) {
  Glide.with(imageView.context)
    .load(imageUrl)
    .placeholder(R.drawable.ic_default_avatar)
    .into(imageView)
}
