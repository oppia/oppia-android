package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** This is a dummy activity to test Html parsing. */
class HtmlParserTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    setContentView(R.layout.test_html_parser_activity)
  }
}
