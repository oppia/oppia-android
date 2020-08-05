package org.oppia.app.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.LessonThumbnail
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.CustomImageTarget
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.ImageLoader
import org.oppia.util.parser.RevisionCardHtmlParserEntityType
import org.oppia.util.parser.ThumbnailDownloadUrlTemplate
import javax.inject.Inject

/** A custom [AppCompatImageView] used to show lesson thumbnails. */
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
    val urlDrawable = UrlDrawable()
    if (imageUrl.endsWith("svg", ignoreCase = true)) {
      val target = SvgTarget(urlDrawable)
      imageLoader.loadSvg(imageUrl, CustomImageTarget(target))
    } else {
      val target = BitmapTarget(urlDrawable)
      imageLoader.loadBitmap(imageUrl, CustomImageTarget(target))
    }
  }

  fun setEntityId(entityId: String) {
    this.entityId = entityId
  }

  private inner class BitmapTarget(urlDrawable: UrlDrawable) : CustomImageTarget<Bitmap>(
    urlDrawable, { resource -> BitmapDrawable(context.resources, resource) }
  )

  private inner class SvgTarget(urlDrawable: UrlDrawable) : CustomImageTarget<Picture>(
    urlDrawable, { resource -> PictureDrawable(resource) }
  )

  private open inner class CustomImageTarget<T>(
    private val urlDrawable: UrlDrawable,
    private val drawableFactory: (T) -> Drawable
  ) : CustomTarget<T>() {
    override fun onLoadCleared(placeholder: Drawable?) {
      // No resources to clear.
    }

    override fun onResourceReady(resource: T, transition: Transition<in T>?) {
      val drawable = drawableFactory(resource)
      imageView.post {
        urlDrawable.drawable = drawable
        Glide.with(context)
          .load(drawable)
          .into(imageView)
        imageView.setBackgroundColor(lessonThumbnail.backgroundColorRgb)
      }
    }
  }

  class UrlDrawable : BitmapDrawable() {
    var drawable: Drawable? = null
    override fun draw(canvas: Canvas) {
      val currentDrawable = drawable
      currentDrawable?.draw(canvas)
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    FragmentManager.findFragment<InjectableFragment>(this)
      .createViewComponent(this)
      .inject(this)
  }
}
