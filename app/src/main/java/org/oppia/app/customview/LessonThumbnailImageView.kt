package org.oppia.app.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.utility.ClickableAreasImage
import org.oppia.util.R
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.ImageLoader
import org.oppia.util.parser.ImageViewTarget
import org.oppia.util.parser.RevisionCardHtmlParserEntityType
import org.oppia.util.parser.ThumbnailDownloadUrlTemplate
import org.oppia.util.parser.UrlImageParser
import javax.inject.Inject

/**
 * A custom [AppCompatImageView] with a list of [LabeledRegion] to work with
 * [ClickableAreasImage].
 *
 * In order to correctly work with this interaction make sure you've called attached an listener
 * using setListener function.
 */
class LessonThumbnailImageView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

  val imageView: ImageView = this

  private lateinit var lessonThumbnail: LessonThumbnail

  @Inject
  lateinit var imageLoader: ImageLoader

  @Inject
  @field:RevisionCardHtmlParserEntityType
  lateinit var entityType: String

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  @Inject
  @field:ThumbnailDownloadUrlTemplate
  lateinit var thumbnailDownloadUrlTemplate: String

  @Inject
  @field:DefaultGcsPrefix
  lateinit var gcsPrefix: String

  private lateinit var entityId: String

  /**
   * Sets the URL for the image & initiates loading it. This is intended to be called via data-binding.
   */
  fun setLessonThumbnail(lessonThumbnail: LessonThumbnail) {
    this.lessonThumbnail = lessonThumbnail
    Log.d("TAG", "setLessonThumbnail: " + lessonThumbnail.thumbnailFilename)
    if (lessonThumbnail.thumbnailFilename.isNotEmpty()) {
      loadImage(lessonThumbnail.thumbnailFilename)
    }
  }

  /** Loads an image using Glide from [filename]. */
  private fun loadImage(filename: String) {
    val imageName = String.format(thumbnailDownloadUrlTemplate, entityType, entityId, filename)
    val imageUrl = "$gcsPrefix/$resourceBucketName/$imageName"
    Log.d("TAG", "loadImage: $imageUrl")
    if (imageUrl.endsWith("svg", ignoreCase = true)) {
      imageLoader.loadSvg(imageUrl, ImageViewTarget(this))
    } else {
      imageLoader.loadBitmap(imageUrl, ImageViewTarget(this))
    }
  }

  fun setEntityId(entityId: String) {
    this.entityId = entityId
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    FragmentManager.findFragment<InjectableFragment>(this).createViewComponent(this).inject(this)
  }
}
