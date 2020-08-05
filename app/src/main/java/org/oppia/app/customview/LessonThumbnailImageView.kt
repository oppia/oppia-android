package org.oppia.app.customview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentManager
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.LessonThumbnail
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.ImageLoader
import org.oppia.util.parser.ImageViewTarget
import org.oppia.util.parser.ThumbnailDownloadUrlTemplate
import javax.inject.Inject

/** A custom [AppCompatImageView] used to show lesson thumbnails. */
class LessonThumbnailImageView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

  private lateinit var lessonThumbnail: LessonThumbnail
  private lateinit var entityId: String
  private lateinit var entityType: String

  @Inject
  lateinit var imageLoader: ImageLoader

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  @Inject
  @field:ThumbnailDownloadUrlTemplate
  lateinit var thumbnailDownloadUrlTemplate: String

  @Inject
  @field:DefaultGcsPrefix
  lateinit var gcsPrefix: String

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

  private fun checkIfLoadingIsPossible() {
    if (::entityId.isInitialized && ::entityType.isInitialized && ::lessonThumbnail.isInitialized) {
      loadLessonThumbnail()
    }
  }

  private fun loadLessonThumbnail() {
    if (lessonThumbnail.thumbnailFilename.isNotEmpty()) {
      loadImage(lessonThumbnail.thumbnailFilename)
    }
  }

  /** Loads an image using Glide from [filename]. */
  private fun loadImage(filename: String) {
    val imageName = String.format(
      thumbnailDownloadUrlTemplate,
      entityType,
      entityId,
      filename
    )
    val imageUrl = "$gcsPrefix/$resourceBucketName/$imageName"
    if (imageUrl.endsWith("svg", ignoreCase = true)) {
      imageLoader.loadSvg(imageUrl, ImageViewTarget(this))
    } else {
      imageLoader.loadBitmap(imageUrl, ImageViewTarget(this))
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    FragmentManager.findFragment<InjectableFragment>(this)
      .createViewComponent(this)
      .inject(this)
  }
}
