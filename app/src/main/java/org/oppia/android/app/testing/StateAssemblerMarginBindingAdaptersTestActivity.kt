package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** Test activity for StateAssemblerMarginBindingAdapters. */
class StateAssemblerMarginBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    setContentView(R.layout.test_margin_bindable_adapter_activity)
  }

  /** Dagger injector for [StateAssemblerMarginBindingAdaptersTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: StateAssemblerMarginBindingAdaptersTestActivity)
  }

  companion object {
    /**
     * Returns an [Intent] for opening new instances of
     * [StateAssemblerMarginBindingAdaptersTestActivity].
     */
    fun createIntent(context: Context): Intent =
      Intent(context, StateAssemblerMarginBindingAdaptersTestActivity::class.java)
  }
}
