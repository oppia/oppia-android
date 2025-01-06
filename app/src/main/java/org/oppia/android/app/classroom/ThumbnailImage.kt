package org.oppia.android.app.classroom

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.oppia.android.app.customview.LessonThumbnailImageView
import org.oppia.android.app.model.LessonThumbnail

@Composable
fun ThumbnailImage(
  entityId: String,
  entityType: String,
  lessonThumbnail: LessonThumbnail?,
  modifier: Modifier = Modifier,
) {
  // TODO(#5422): Migrate to jetpack compose.
  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { context ->
      LessonThumbnailImageView(context).apply {
        setLessonThumbnail(lessonThumbnail)
        setEntityId(entityId)
        setEntityType(entityType)
      }
    }
  )
}
