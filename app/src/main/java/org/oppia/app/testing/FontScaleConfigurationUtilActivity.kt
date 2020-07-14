package org.oppia.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Test activity used for testing font scale. */
class FontScaleConfigurationUtilActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var fontScaleConfigurationUtilActivityPresenter: FontScaleConfigurationUtilActivityPresenter // ktlint-disable max-line-length

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val storyTextSize = intent.getStringExtra(FONT_SCALE_EXTRA_KEY)
    fontScaleConfigurationUtilActivityPresenter.handleOnCreate(storyTextSize)
  }

  companion object {
    private const val FONT_SCALE_EXTRA_KEY = "FONT_SCALE_EXTRA_KEY"

    /** Returns a new [FontScaleConfigurationUtilActivity] for context and story text size. */
    fun createFontScaleTestActivity(context: Context, storyTextSize: String): Intent {
      val intent = Intent(context, FontScaleConfigurationUtilActivity::class.java)
      intent.putExtra(FONT_SCALE_EXTRA_KEY, storyTextSize)
      return intent
    }
  }
}
