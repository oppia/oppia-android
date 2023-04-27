package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** Test activity for MarginBindableAdapters. */
class MarginBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    setContentView(R.layout.test_margin_bindable_adapter_activity)
  }

  interface Injector {
    fun inject(activity: MarginBindingAdaptersTestActivity)
  }

  companion object {
    fun createIntent(context: Context): Intent =
      Intent(context, MarginBindingAdaptersTestActivity::class.java)
  }
}
