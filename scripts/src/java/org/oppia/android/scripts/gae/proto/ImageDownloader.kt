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
    imageContainerType: GcsService.ImageContainerType,
    imageType: GcsService.ImageType,
    entityId: String,
    filename: String,
    transform: (Int) -> T
  ): Deferred<T> {
    return CoroutineScope(coroutineDispatcher).async {
      val length = imageLengths.getOrPut(ImageId(imageContainerType, entityId, imageType, filename)) {
        gcsService.fetchImageContentLengthAsync(imageContainerType, imageType, entityId, filename).await()
      }
      return@async transform(length.toInt())
    }
  }

  fun retrieveImageContentAsync(
    imageContainerType: GcsService.ImageContainerType,
    imageType: GcsService.ImageType,
    entityId: String,
    filename: String
  ): Deferred<ByteArray?> =
    gcsService.fetchImageContentDataAsync(imageContainerType, imageType, entityId, filename)

  fun computeImageUrl(
    imageContainerType: GcsService.ImageContainerType,
    imageType: GcsService.ImageType,
    entityId: String,
    filename: String
  ): String = gcsService.computeImageUrl(imageContainerType, imageType, entityId, filename)

  private data class ImageId(
    val imageContainerType: GcsService.ImageContainerType,
    val entityId: String,
    val imageType: GcsService.ImageType,
    val filename: String
  )
}
