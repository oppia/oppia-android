package org.oppia.android.scripts.gae.proto

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.oppia.android.scripts.gae.gcs.GcsService
import java.util.concurrent.ConcurrentHashMap

class ImageDownloader(
  private val gcsService: GcsService,
  private val coroutineDispatcher: CoroutineDispatcher
) {
  private val imageLengths = ConcurrentHashMap<ImageId, Long>()

  fun <T> retrieveImageLengthAsync(
    entityType: GcsService.EntityType,
    imageType: GcsService.ImageType,
    entityId: String,
    filename: String,
    transform: (Int) -> T
  ): Deferred<T> {
    return CoroutineScope(coroutineDispatcher).async {
      val length = imageLengths.getOrPut(ImageId(entityType, entityId, imageType, filename)) {
        gcsService.fetchImageContentLengthAsync(entityType, imageType, entityId, filename).await()
      }
      return@async transform(length.toInt())
    }
  }

  private data class ImageId(
    val entityType: GcsService.EntityType,
    val entityId: String,
    val imageType: GcsService.ImageType,
    val filename: String
  )
}
