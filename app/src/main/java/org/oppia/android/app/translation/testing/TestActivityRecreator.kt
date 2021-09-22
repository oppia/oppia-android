package org.oppia.android.app.translation.testing

import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject
import org.oppia.android.app.translation.ActivityRecreator

class TestActivityRecreator @Inject constructor(): ActivityRecreator {
  override fun recreate(activity: AppCompatActivity) {
    // Do nothing since activity recreation doesn't work correctly in Robolectric.
  }
}
