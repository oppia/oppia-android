package org.oppia.app.testing

import android.os.Bundle
import android.widget.Button
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.utility.ClickableAreasImage
import org.oppia.app.utility.OnClickableAreaClickedListener

/** Test Activity used for testing [ClickableAreasImage] functionality */
class ImageRegionSelectionTestActivity :
  InjectableAppCompatActivity(),
  OnClickableAreaClickedListener {

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

  override fun onClickableAreaTouched(region: String) {
    findViewById<Button>(R.id.submit_button).isEnabled = region.isNotEmpty()
  }
}
