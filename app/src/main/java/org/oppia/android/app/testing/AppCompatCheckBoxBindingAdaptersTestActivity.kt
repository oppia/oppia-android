package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** Test activity for [org.oppia.android.app.databinding.AppCompatCheckBoxBindingAdapters]. */
class AppCompatCheckBoxBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    setContentView(R.layout.test_app_compat_check_box_bindable_adapter_activity)
  }

  companion object {
    fun createIntent(context: Context): Intent =
      Intent(context, AppCompatCheckBoxBindingAdaptersTestActivity::class.java)
  }

  interface Injector {
    fun inject(activity: AppCompatCheckBoxBindingAdaptersTestActivity)
  }
}
