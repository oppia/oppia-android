package org.oppia.android.app.application.dev

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.appcompat.content.res.AppCompatResources
import org.oppia.android.app.views.R
import java.io.File
import java.io.FileOutputStream

/**
 * A developer-only [ContentProvider] that serves local drawable resources as thumbnail images.
 *
 * In developer builds, thumbnail image loading is redirected from Google Cloud Storage to this
 * content provider via the `content://org.oppia.android.provider` URI scheme. When a thumbnail
 * filename (e.g., `baker.img`) is requested, this provider maps it to the corresponding local
 * drawable resource (e.g., `lesson_thumbnail_graphic_baker.xml`) and returns the rendered image.
 *
 * This allows proto lessons with `thumbnail_filename` placeholders to successfully load thumbnails
 * without needing access to GCS, which is unavailable in dev builds.
 */
class ThumbnailContentProvider : ContentProvider() {

  companion object {
    private const val AUTHORITY = "org.oppia.android.provider"
    private const val THUMBNAIL_MATCH = 1
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
      // Match any path under the authority — the thumbnail filename is extracted from the
      // last path segment of the URI.
      addURI(AUTHORITY, "*/*/*/*/#", THUMBNAIL_MATCH)
      addURI(AUTHORITY, "*/*/*/*", THUMBNAIL_MATCH)
      addURI(AUTHORITY, "thumbnail/*", THUMBNAIL_MATCH)
      addURI(AUTHORITY, "#", THUMBNAIL_MATCH)
    }

    /**
     * Maps thumbnail filenames (e.g., "baker.img") to their corresponding drawable resource IDs.
     *
     * This mapping must be kept in sync with the `thumbnail_filename` values defined in the
     * textproto asset files.
     */
    private val THUMBNAIL_FILENAME_TO_DRAWABLE_MAP = mapOf(
      "baker.img" to R.drawable.lesson_thumbnail_graphic_baker,
      "child_with_book.img" to R.drawable.lesson_thumbnail_graphic_child_with_book,
      "child_with_cupcakes.img" to R.drawable.lesson_thumbnail_graphic_child_with_cupcakes,
      "child_with_fractions_homework.img" to
        R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework,
      "duck_and_chicken.img" to R.drawable.lesson_thumbnail_graphic_duck_and_chicken,
      "person_with_pie_chart.img" to R.drawable.lesson_thumbnail_graphic_person_with_pie_chart
    )

    /** Default drawable used when a filename doesn't match any known thumbnail. */
    private val DEFAULT_THUMBNAIL_DRAWABLE = R.drawable.lesson_thumbnail_graphic_baker
  }

  override fun onCreate(): Boolean = true

  override fun query(
    uri: Uri,
    projection: Array<String>?,
    selection: String?,
    selectionArgs: Array<String>?,
    sortOrder: String?
  ): Cursor? = null

  override fun getType(uri: Uri): String = "image/png"

  override fun insert(uri: Uri, values: ContentValues?): Uri? = null

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

  override fun update(
    uri: Uri,
    values: ContentValues?,
    selection: String?,
    selectionArgs: Array<String>?
  ): Int = 0

  override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
    val context = context ?: return null
    val thumbnailFilename = uri.lastPathSegment ?: return null

    val drawableResId = THUMBNAIL_FILENAME_TO_DRAWABLE_MAP[thumbnailFilename]
      ?: DEFAULT_THUMBNAIL_DRAWABLE

    val drawable = AppCompatResources.getDrawable(context, drawableResId)
      ?: return null

    // Render the drawable (which may be a vector/XML drawable) to a bitmap.
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 192
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 192
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    // Write the bitmap to a temporary file and return a file descriptor.
    val cacheFile = File(context.cacheDir, "thumbnail_${thumbnailFilename.hashCode()}.png")
    FileOutputStream(cacheFile).use { outputStream ->
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }
    bitmap.recycle()

    return ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
  }
}
