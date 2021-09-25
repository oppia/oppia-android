package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** Test activity for ImageViewBindingAdapters. */
class ImageViewBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    setContentView(R.layout.test_image_view_bindable_adapter_activity)
  }
}