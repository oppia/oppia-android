package org.oppia.app.databinding

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import org.oppia.app.R
import org.oppia.app.model.LessonThumbnailGraphic

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
  setImageDrawable(imageView, when (thumbnailGraphic) {
    LessonThumbnailGraphic.BAKER -> R.drawable.lesson_thumbnail_graphic_baker
    LessonThumbnailGraphic.CHILD_WITH_BOOK -> R.drawable.lesson_thumbnail_graphic_child_with_book
    LessonThumbnailGraphic.CHILD_WITH_CUPCAKES -> R.drawable.lesson_thumbnail_graphic_child_with_cupcakes
    LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK ->
      R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework
    LessonThumbnailGraphic.DUCK_AND_CHICKEN -> R.drawable.lesson_thumbnail_graphic_duck_and_chicken
    LessonThumbnailGraphic.PERSON_WITH_PIE_CHART -> R.drawable.lesson_thumbnail_graphic_person_with_pie_chart
    else -> R.drawable.lesson_thumbnail_default
  })
}
