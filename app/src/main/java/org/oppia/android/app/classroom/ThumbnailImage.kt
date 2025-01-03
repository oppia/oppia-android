package org.oppia.android.app.classroom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import org.oppia.android.R
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.parser.image.ImageLoader
import org.oppia.android.util.parser.image.ImageTransformation

@Composable
fun ThumbnailImage(
  modifier: Modifier = Modifier,
  entityId: String,
  entityType: String,
  lessonThumbnail: LessonThumbnail?,
  isBlurred: Boolean,
  imageLoader: ImageLoader,
  resourceBucketName: String,
  thumbnailDownloadUrlTemplate: String,
  gcsPrefix: String,
  oppiaLogger: OppiaLogger,
  machineLocale: OppiaLocale.MachineLocale
) {
  val context = LocalContext.current

  // Determine the background color and scale type
  val backgroundColor = remember(lessonThumbnail) {
    lessonThumbnail?.backgroundColorRgb?.toLong()?.let {
      (0xff000000L or it).toInt()
    } ?: 0xff000000.toInt()
  }
  val transformations = if (isBlurred) listOf(ImageTransformation.BLUR) else listOf()

  Box(
    modifier = modifier
      .background(Color(backgroundColor))
      .fillMaxSize()
  ) {
    if (lessonThumbnail != null) {
      val filename = lessonThumbnail.thumbnailFilename
      if (filename.isNotEmpty()) {
        val imageUrl = remember(filename, entityId, entityType) {
          machineLocale.run {
            val formattedName = thumbnailDownloadUrlTemplate.formatForMachines(
              entityType,
              entityId,
              filename
            )
            "$gcsPrefix/$resourceBucketName/$formattedName"
          }
        }

        // Load image using ImageLoader
        /*ImageLoaderComposable(
          imageUrl = imageUrl,
          transformations = transformations,
          imageLoader = imageLoader,
          oppiaLogger = oppiaLogger*//*
        )*/
      } else {
        // Load drawable resource
        val drawableRes = getDrawableResource(lessonThumbnail)
        Image(
          painter = painterResource(id = drawableRes),
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Fit
        )
      }
    }
  }
}

/*@Composable
private fun ImageLoaderComposable(
  imageUrl: String,
  transformations: List<ImageTransformation>,
  imageLoader: ImageLoader,
  oppiaLogger: OppiaLogger
) {
  LaunchedEffect(imageUrl) {
    try {
      // Use the image loader to fetch the image
      imageLoader.loadBitmap(imageUrl, object : ImageViewTa {
        override fun onBitmapLoaded(bitmap: Bitmap?) {
          // Handle successful image loading
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
          // Handle image loading failure
          oppiaLogger.e("LessonThumbnailImage", "Image loading failed")
        }
      }, transformations)
    } catch (e: Exception) {
      oppiaLogger.e("LessonThumbnailImage", "Error loading image", e)
    }
  }
}*/

/** Retrieves the drawable resource ID for the lesson thumbnail based on its graphic type. */
private fun getDrawableResource(lessonThumbnail: LessonThumbnail): Int {
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
    LessonThumbnailGraphic.SCIENCE_CLASSROOM ->
      R.drawable.ic_science
    LessonThumbnailGraphic.MATHS_CLASSROOM ->
      R.drawable.ic_maths
    LessonThumbnailGraphic.ENGLISH_CLASSROOM ->
      R.drawable.ic_english
    else ->
      R.drawable.topic_fractions_01
  }
}
