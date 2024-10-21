package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity

/** Test activity for [TextInputLayoutBindingAdapters]. */
class TextInputLayoutBindingAdaptersTestActivity : InjectableAutoLocalizedAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.text_input_layout_binding_adapters_test_activity)

    supportFragmentManager.beginTransaction().add(
      R.id.background,
      TextInputLayoutBindingAdaptersTestFragment()
    ).commitNow()
  }

  companion object {
    /** Intent to open this activity. */
    fun createIntent(context: Context): Intent =
      Intent(context, TextInputLayoutBindingAdaptersTestActivity::class.java)
  }
}
