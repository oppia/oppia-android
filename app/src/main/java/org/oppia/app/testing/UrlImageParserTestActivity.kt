package org.oppia.app.testing

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity

/** This is a dummy activity to test [UrlImageParser]. */
class UrlImageParserTestActivity : InjectableAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.test_url_parser_activity)
    val testUrlImageParserImageview: ImageView = findViewById(R.id.test_url_parser_image_view)
    testUrlImageParserImageview.setImageResource(R.drawable.dummy_place_holder)
  }
}
