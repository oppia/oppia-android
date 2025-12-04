package org.oppia.android.app.classroom

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.oppia.android.app.customview.LessonThumbnailImageView
import org.oppia.android.app.model.LessonThumbnail

/**
 * A composable function that displays a lesson thumbnail image using a custom Android view.
 *
 * This function integrates the [LessonThumbnailImageView] within a Compose layout, allowing it
 * to render a lesson thumbnail image based on the provided parameters. The implementation
 * currently relies on a traditional Android View approach due to compatibility issues with the
 * Glide Compose library.
 *
 * @param entityId the unique identifier for the entity associated with the thumbnail
 * @param entityType the type of the entity (e.g., classroom, topic, story)
 * @param lessonThumbnail the [LessonThumbnail] containing metadata required to load the image
 * @param modifier the [Modifier] to be applied to the layout, defaulting to [Modifier]
 */
@Composable
fun ThumbnailImage(
  entityId: String,
  entityType: String,
  lessonThumbnail: LessonThumbnail?,
  modifier: Modifier = Modifier,
) {
  // TODO(#5422): Migrate to Jetpack Compose once the Glide Compose library becomes compatible.
  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { context ->
      LessonThumbnailImageView(context).apply {
        setLessonThumbnail(lessonThumbnail)
        setEntityId(entityId)
        setEntityType(entityType)
      }
    },
    update = { view ->
      view.setLessonThumbnail(lessonThumbnail)
      view.setEntityId(entityId)
      view.setEntityType(entityType)
    }
  )
}
