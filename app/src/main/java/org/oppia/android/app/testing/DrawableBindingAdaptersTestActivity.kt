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

  /** Dagger injector for [DrawableBindingAdaptersTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: DrawableBindingAdaptersTestActivity)
  }

  companion object {
    /** Returns an [Intent] for opening new instances of [DrawableBindingAdaptersTestActivity]. */
    fun createIntent(context: Context): Intent =
      Intent(context, DrawableBindingAdaptersTestActivity::class.java)
  }
}
