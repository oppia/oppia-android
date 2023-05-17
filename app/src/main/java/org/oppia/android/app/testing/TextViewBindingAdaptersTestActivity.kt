package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** Test activity for [org.oppia.android.app.databinding.TextViewBindingAdapters]. */
class TextViewBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    setContentView(R.layout.test_text_view_bindable_adapter_activity)
  }

  /** Dagger injector for [TextViewBindingAdaptersTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: TextViewBindingAdaptersTestActivity)
  }

  companion object {
    /** Returns an [Intent] for opening new instances of [TextViewBindingAdaptersTestActivity]. */
    fun createIntent(context: Context): Intent =
      Intent(context, TextViewBindingAdaptersTestActivity::class.java)
  }
}
