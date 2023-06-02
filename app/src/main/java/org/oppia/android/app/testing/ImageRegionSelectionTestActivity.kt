package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.utility.ClickableAreasImage
import org.oppia.android.app.utility.OnClickableAreaClickedListener

/** Test Activity used for testing [ClickableAreasImage] functionality. */
class ImageRegionSelectionTestActivity : InjectableAutoLocalizedAppCompatActivity() {

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

  /**
   * Sets a test [OnClickableAreaClickedListener] that will be called when the image region
   * maintained by this test activity is clicked.
   */
  fun setMockOnClickableAreaClickedListener(listener: OnClickableAreaClickedListener) {
    val fragment =
      supportFragmentManager.findFragmentById(R.id.test_fragment_placeholder)
        as? ImageRegionSelectionTestFragment
        ?: throw AssertionError("Expected fragment to be present.")
    fragment.mockOnClickableAreaClickedListener = listener
  }
}
