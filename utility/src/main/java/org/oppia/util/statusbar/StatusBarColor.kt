package org.oppia.util.statusbar

import android.content.res.Resources
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/** Utility to change the color of Status Bar. */
class StatusBarColor {
  companion object {
    /**
     * This method updates the color of the Status Bar.
     * @param colorId color passed for the status bar
     * @param activity the reference of the activity from which this method is called.
     * @param statusBarLight passed Boolean true if the status bar theme is light, else passed Boolean false
     */
    fun statusBarColorUpdate(
      colorId: Int,
      activity: AppCompatActivity,
      statusBar: View?,
      statusBarLight: Boolean
    ) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        if (statusBarLight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        activity.window.statusBarColor = ContextCompat.getColor(activity, colorId)
      } else {
        activity.window.setFlags(
          WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
          WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )
        val statusBarHeight = getStatusBarHeight(activity.resources)
        statusBar!!.layoutParams.height = statusBarHeight
        statusBar.setBackgroundColor(colorId)
      }
    }

    private fun getStatusBarHeight(resources: Resources): Int {
      var statusBarHeight = 0
      val id = resources.getIdentifier("status_bar_height", "dimen", "android")
      if (id > 0) {
        statusBarHeight = resources.getDimensionPixelSize(id)
      }
      return statusBarHeight
    }
  }
}
