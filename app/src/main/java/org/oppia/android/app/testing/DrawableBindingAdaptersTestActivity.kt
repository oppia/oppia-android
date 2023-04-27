package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** Test activity for [org.oppia.android.app.databinding.DrawableBindingAdapters]. */
class DrawableBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    setContentView(R.layout.test_drawable_binding_adapter_activity)
  }

  interface Injector {
    fun inject(activity: DrawableBindingAdaptersTestActivity)
  }

  companion object {
    fun createIntent(context: Context): Intent =
      Intent(context, DrawableBindingAdaptersTestActivity::class.java)
  }
}
