package org.oppia.android.app.translation

import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class ActivityRecreatorImpl @Inject constructor(): ActivityRecreator {
  override fun recreate(activity: AppCompatActivity) {
    activity.recreate()
  }
}
