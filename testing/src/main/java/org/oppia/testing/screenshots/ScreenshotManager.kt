package org.oppia.testing.screenshots

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.view.View
import java.io.File
import java.io.FileOutputStream

/** A class that is used to take screenshots for a UI. */
class ScreenshotManager {

  companion object {
    private const val SCREENSHOT_TEST_RESULT_DIRECTORY_NAME = "oppiascreenshots"
    /**
     * @return the path of the directory containing the output screenshots
     */
    fun getOutputPath(): String {
      val path = Environment.getExternalStorageDirectory().path
      return "$path/$SCREENSHOT_TEST_RESULT_DIRECTORY_NAME"
    }
  }

  /**
   * A function that takes a screenshot of a given activity and stores it in the
   * application's files.
   * @param activity The target activity to take a screenshot of.
   * @return a bitmap representing the screenshot of the activity.
   */
  fun takeScreenshot(activity: Activity): Bitmap {
    val view = activity.window.decorView.rootView
    val bitmap = getViewBitmap(view)
    val fileName = activity::class.java.name
    exportImageFile(fileName, bitmap)
    return bitmap
  }

  /**
   * A function that takes a screenshot of a given view and stores it in the
   * application's files.
   * @param view The target view to take a screenshot of.
   * @return a bitmap representing the screenshot of the activity.
   */
  fun takeScreenshot(view: View): Bitmap {
    val bitmap = getViewBitmap(view)
    val viewStringId = view.context.resources.getResourceEntryName(view.id)
    val fileName = "${view.context::class.java.name}_$viewStringId"
    exportImageFile(fileName, bitmap)
    return bitmap
  }

  /**
   * Takes a view and returns a bitmap object representing it.
   * @param view the target view
   * @return the desired bitmap
   */
  private fun getViewBitmap(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(
      view.width,
      view.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
  }

  /**
   * Writes a png file to the external storage.
   * @param fileName the output file name without the extension
   * @param sourceBitmap the desired image to be exported
   */
  private fun exportImageFile(fileName: String, sourceBitmap: Bitmap) {
    val outputFolder = File(getOutputPath())
    val imageFile =
      File(outputFolder, "$fileName.png")
    val outputStream = FileOutputStream(imageFile)
    val quality = 100
    sourceBitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
    outputStream.flush()
    outputStream.close()
  }
}
