package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** This is a dummy activity to test StateAssemblerMarginBindingAdapters. */
class StateAssemblerMarginBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    setContentView(R.layout.test_margin_bindable_adapter_activity)
  }
}
