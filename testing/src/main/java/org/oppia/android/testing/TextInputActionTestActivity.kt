package org.oppia.android.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/** Test Activity for TextInputActionTest. */
class TextInputActionTestActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge)
  }

  companion object {
    /** Returns a new [Intent] for the given [Context] to launch new [TextInputActionTestActivity]s. */
    fun createIntent(context: Context): Intent =
      Intent(context, TextInputActionTestActivity::class.java)
  }
}
