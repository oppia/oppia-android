package org.oppia.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.utility.FontScaleConfigurationUtil

private const val FONT_SCALE_EXTRA_KEY = "FONT_SCALE_EXTRA_KEY"

/** Test activity used for testing font scale. */
class FontScaleTestActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val storyTextSize = intent.getStringExtra(FONT_SCALE_EXTRA_KEY)
    FontScaleConfigurationUtil().adjustFontScale(this, storyTextSize)
    setContentView(R.layout.font_scale_test_activity)
  }

  companion object {
    fun createFontScaleTestActivity(context: Context, storyTextSize: String): Intent {
      val intent = Intent(context, FontScaleTestActivity::class.java)
      intent.putExtra(FONT_SCALE_EXTRA_KEY, storyTextSize)
      return intent
    }
  }
}
