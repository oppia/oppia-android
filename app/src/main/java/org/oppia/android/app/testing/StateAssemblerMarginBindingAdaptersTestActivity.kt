package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.utility.activity.ActivityComponentImpl
import org.oppia.android.app.utility.activity.InjectableAppCompatActivity

/** Test activity for StateAssemblerMarginBindingAdapters. */
class StateAssemblerMarginBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    setContentView(R.layout.test_margin_bindable_adapter_activity)
  }
}
