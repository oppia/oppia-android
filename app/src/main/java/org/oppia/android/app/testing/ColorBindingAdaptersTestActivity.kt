package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity

/** Test activity for ColorBindingAdapters. */
class ColorBindingAdaptersTestActivity : InjectableAutoLocalizedAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_color_binding_adapters_test)

    supportFragmentManager.beginTransaction().add(
      R.id.background,
      ColorBindingAdaptersTestFragment()
    ).commitNow()
  }

  companion object {
    /** Intent to open this activity. */
    fun createIntent(context: Context): Intent =
      Intent(context, ColorBindingAdaptersTestActivity::class.java)
  }
}
