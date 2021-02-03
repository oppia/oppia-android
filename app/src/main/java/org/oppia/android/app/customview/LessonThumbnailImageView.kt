package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.oppia.android.R
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.shim.ViewComponentFactory
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.DefaultGcsPrefix
import org.oppia.android.util.parser.ImageLoader
import org.oppia.android.util.parser.ImageTransformation
import org.oppia.android.util.parser.ImageViewTarget
import org.oppia.android.util.parser.ThumbnailDownloadUrlTemplate
import javax.inject.Inject

/** A custom [AppCompatImageView] used to show lesson thumbnails. */
class LessonThumbnailImageView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

  private val imageView = this
  private var isBlurred: Boolean = false
  private lateinit var lessonThumbnail: LessonThumbnail
  private lateinit var entityId: String
  private lateinit var entityType: String

  @Inject
  lateinit var imageLoader: ImageLoader

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  // TODO(#1571): Investigate this issue to fix the initialization error.
  @Inject
  @field:ThumbnailDownloadUrlTemplate
  lateinit var thumbnailDownloadUrlTemplate: String

  @Inject
  @field:DefaultGcsPrefix
  lateinit var gcsPrefix: String

  @Inject
  lateinit var logger: ConsoleLogger

  fun setEntityId(entityId: String) {
    this.entityId = entityId
    checkIfLoadingIsPossible()
  }

  fun setEntityType(entityType: String) {
    this.entityType = entityType
    checkIfLoadingIsPossible()
  }

  fun setLessonThumbnail(lessonThumbnail: LessonThumbnail) {
    this.lessonThumbnail = lessonThumbnail
    checkIfLoadingIsPossible()
  }

  fun setIsBlurred(isBlurred: Boolean) {
    this.isBlurred = isBlurred
  }

  private fun checkIfLoadingIsPossible() {
    if (::entityId.isInitialized &&
      ::entityType.isInitialized &&
      ::lessonThumbnail.isInitialized &&
      ::thumbnailDownloadUrlTemplate.isInitialized &&
      ::resourceBucketName.isInitialized &&
      ::gcsPrefix.isInitialized &&
      ::imageLoader.isInitialized &&
      ::logger.isInitialized
    ) {
      loadLessonThumbnail()
    }
  }

  private fun loadLessonThumbnail() {
    var transformations = if (isBlurred) {
      listOf(ImageTransformation.BLUR)
    } else {
      listOf()
    }
    if (lessonThumbnail.thumbnailFilename.isNotEmpty()) {
      loadImage(lessonThumbnail.thumbnailFilename, transformations)
    } else {
      imageLoader.loadDrawable(
        getLessonDrawableResource(lessonThumbnail),
        ImageViewTarget(this),
        transformations
      )
    }
    imageView.setBackgroundColor(lessonThumbnail.backgroundColorRgb)
  }

  /** Loads an image using Glide from [filename]. */
  private fun loadImage(filename: String, transformations: List<ImageTransformation>) {
    val imageName = String.format(
      thumbnailDownloadUrlTemplate,
      entityType,
      entityId,
      filename
    )
    val imageUrl = "$gcsPrefix/$resourceBucketName/$imageName"
    if (imageUrl.endsWith("svg", ignoreCase = true)) {
      imageLoader.loadSvg(imageUrl, ImageViewTarget(this), transformations)
    } else {
      imageLoader.loadBitmap(imageUrl, ImageViewTarget(this), transformations)
    }
  }

  override fun onAttachedToWindow() {
    try {
      super.onAttachedToWindow()
      (FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory)
        .createViewComponent(this).inject(this)
    } catch (e: IllegalStateException) {
      if (::logger.isInitialized)
        logger.e(
          "LessonThumbnailImageView",
          "Throws exception on attach to window",
          e
        )
    }
  }

  private fun getLessonDrawableResource(lessonThumbnail: LessonThumbnail): Int {
    return when (lessonThumbnail.thumbnailGraphic) {
      LessonThumbnailGraphic.BAKER ->
        R.drawable.lesson_thumbnail_graphic_baker
      LessonThumbnailGraphic.CHILD_WITH_BOOK ->
        R.drawable.lesson_thumbnail_graphic_child_with_book
      LessonThumbnailGraphic.CHILD_WITH_CUPCAKES ->
        R.drawable.lesson_thumbnail_graphic_child_with_cupcakes
      LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK ->
        R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework
      LessonThumbnailGraphic.DUCK_AND_CHICKEN ->
        R.drawable.lesson_thumbnail_graphic_duck_and_chicken
      LessonThumbnailGraphic.PERSON_WITH_PIE_CHART ->
        R.drawable.lesson_thumbnail_graphic_person_with_pie_chart
      LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION ->
        R.drawable.topic_fractions_01
      LessonThumbnailGraphic.WRITING_FRACTIONS ->
        R.drawable.topic_fractions_02
      LessonThumbnailGraphic.EQUIVALENT_FRACTIONS ->
        R.drawable.topic_fractions_03
      LessonThumbnailGraphic.MIXED_NUMBERS_AND_IMPROPER_FRACTIONS ->
        R.drawable.topic_fractions_04
      LessonThumbnailGraphic.COMPARING_FRACTIONS ->
        R.drawable.topic_fractions_05
      LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS ->
        R.drawable.topic_fractions_06
      LessonThumbnailGraphic.MULTIPLYING_FRACTIONS ->
        R.drawable.topic_fractions_07
      LessonThumbnailGraphic.DIVIDING_FRACTIONS ->
        R.drawable.topic_fractions_08
      LessonThumbnailGraphic.DERIVE_A_RATIO ->
        R.drawable.topic_ratios_01
      LessonThumbnailGraphic.WHAT_IS_A_FRACTION ->
        R.drawable.topic_fractions_01
      LessonThumbnailGraphic.FRACTION_OF_A_GROUP ->
        R.drawable.topic_fractions_02
      LessonThumbnailGraphic.ADDING_FRACTIONS ->
        R.drawable.topic_fractions_03
      LessonThumbnailGraphic.MIXED_NUMBERS ->
        R.drawable.topic_fractions_04
      else ->
        R.drawable.topic_fractions_01
    }
  }
}
