package org.oppia.android.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.oppia.android.util.R
import javax.inject.Inject
import kotlin.math.max

// TODO(#169): Replace this with exploration asset downloader.
// TODO(#277): Add test cases for loading image.

// TODO: redo docs. Move to existing UrlImageParser (though might be a good time to rename this
// class in general).
class DeferredUrlImageParser private constructor(
  private val context: Context,
  private val gcsPrefix: String,
  private val gcsResourceName: String,
  private val imageDownloadUrlTemplate: String,
  private val entityType: String,
  private val entityId: String,
  private val imageLoader: ImageLoader
) : Html.ImageGetter {
  override fun getDrawable(urlString: String): Drawable {
    val imageUrl = String.format(imageDownloadUrlTemplate, entityType, entityId, urlString)
    // TODO(#1039): Introduce custom type OppiaImage for rendering Bitmap and Svg.
    return if (imageUrl.endsWith("svg", ignoreCase = true)) {
      val imageDrawable = DeferredImageDrawable.createDeferredImageDrawableForPicture()
      imageLoader.loadSvg("$gcsPrefix/$gcsResourceName/$imageUrl", imageDrawable.getImageTarget())
      return imageDrawable
    } else {
      val imageDrawable =
        DeferredImageDrawable.createDeferredImageDrawableForBitmap(context.resources)
      imageLoader.loadBitmap(
        "$gcsPrefix/$gcsResourceName/$imageUrl", imageDrawable.getImageTarget()
      )
      imageDrawable
    }
  }

  class Factory @Inject constructor(
    private val context: Context,
    @DefaultGcsPrefix private val gcsPrefix: String,
    @ImageDownloadUrlTemplate private val imageDownloadUrlTemplate: String,
    private val imageLoader: ImageLoader
  ) {
    fun create(
      gcsResourceName: String,
      entityType: String,
      entityId: String
    ): DeferredUrlImageParser {
      return DeferredUrlImageParser(
        context,
        gcsPrefix,
        gcsResourceName,
        imageDownloadUrlTemplate,
        entityType,
        entityId,
        imageLoader
      )
    }
  }
}
