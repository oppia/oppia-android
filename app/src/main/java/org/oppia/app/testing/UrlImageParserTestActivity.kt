package org.oppia.app.testing

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Spannable
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.util.parser.HtmlParser
import org.oppia.util.parser.ImageLoader
import org.oppia.util.parser.UrlImageParser_Factory_Factory
import javax.inject.Inject

/** This is a dummy activity to test [UrlImageParser]. */
class UrlImageParserTestActivity : InjectableAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.test_url_parser_activity)
  }
}
