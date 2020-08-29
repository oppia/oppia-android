package org.oppia.app.screenshots

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import java.io.File
import java.io.FileOutputStream

/**A class that is used to take screenshots from some activity.*/
class ScreenshotManager {

  /**
   * A function that takes a screenshot of a given activity and stores it in the
   * application's files.
   * @param activity The target activity to take a screenshot of.
   */
  fun takeScreenshot(activity: AppCompatActivity) {
    if (checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
      throw SecurityException("WRITE_EXTERNAL_STORAGE permission denied")
    }
    val view = activity.window.decorView.rootView
    val bitmap = Bitmap.createBitmap(
      view.width,
      view.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    val fileName = "${activity::class.java.name}.jpg"
    val imageFile = File(activity.getExternalFilesDir(null), fileName)
    val outputStream = FileOutputStream(imageFile)
    val quality = 100
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    outputStream.flush()
    outputStream.close()
  }
}
