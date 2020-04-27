package org.oppia.util.statusbar

import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class StatusBarColor{
  companion object {
    fun statusBarColorUpdate(colorId: Int, activity: AppCompatActivity, statusBarLight: Boolean) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if(statusBarLight){
          activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        activity.window.statusBarColor = ContextCompat.getColor(activity, colorId)
      }
    }
  }
}
