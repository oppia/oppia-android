package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity

/** Test activity for [org.oppia.android.app.databinding.DrawableBindingAdapters]. */
class DrawableBindingAdaptersTestActivity : InjectableAutoLocalizedAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    setContentView(R.layout.test_drawable_binding_adapter_activity)
  }
}
