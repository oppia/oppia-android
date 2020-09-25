package org.oppia.util.statusbar

import android.os.Build
import android.view.View
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
    fun statusBarColorUpdate(colorId: Int, activity: AppCompatActivity, statusBarLight: Boolean) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        if (statusBarLight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        activity.window.statusBarColor = ContextCompat.getColor(activity, colorId)
      }
    }
  }
}
