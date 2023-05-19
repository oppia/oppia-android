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

  /** Dagger injector for [HtmlParserTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: HtmlParserTestActivity)
  }

  companion object {
    /** Returns an [Intent] for opening new instances of [HtmlParserTestActivity]. */
    fun createIntent(context: Context): Intent = Intent(context, HtmlParserTestActivity::class.java)
  }
}
