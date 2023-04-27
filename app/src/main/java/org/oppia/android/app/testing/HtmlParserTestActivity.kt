package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** This is a dummy activity to test Html parsing. */
class HtmlParserTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    setContentView(R.layout.test_html_parser_activity)
  }

  interface Injector {
    fun inject(activity: HtmlParserTestActivity)
  }

  companion object {
    fun createIntent(context: Context): Intent = Intent(context, HtmlParserTestActivity::class.java)
  }
}
