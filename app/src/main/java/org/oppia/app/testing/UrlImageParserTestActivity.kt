package org.oppia.app.testing

import android.os.Bundle
import android.text.Spannable
import android.widget.TextView
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.util.parser.HtmlParser
import org.oppia.util.parser.UrlImageParser_Factory_Factory
import javax.inject.Inject

/** This is a dummy activity to test [UrlImageParser]. */
class UrlImageParserTestActivity : InjectableAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.test_url_parser_activity)
  }
}
