package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.utility.ClickableAreasImage
import org.oppia.android.app.utility.activity.ActivityComponentImpl
import org.oppia.android.app.utility.activity.InjectableAppCompatActivity

/** Test Activity used for testing [ClickableAreasImage] functionality */
class ImageRegionSelectionTestActivity : InjectableAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    setContentView(R.layout.test_activity)
    supportFragmentManager.beginTransaction()
      .add(
        R.id.test_fragment_placeholder,
        ImageRegionSelectionTestFragment(),
        IMAGE_REGION_SELECTION_TEST_FRAGMENT_TAG
      )
      .commitNow()
  }
}
