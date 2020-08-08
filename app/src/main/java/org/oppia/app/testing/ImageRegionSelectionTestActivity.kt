package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.ui.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.utility.ClickableAreasImage

/** Test Activity used for testing [ClickableAreasImage] functionality */
class ImageRegionSelectionTestActivity : InjectableAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
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
