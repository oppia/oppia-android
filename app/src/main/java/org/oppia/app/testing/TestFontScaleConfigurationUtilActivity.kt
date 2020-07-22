package org.oppia.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Test activity used for testing font scale. */
class TestFontScaleConfigurationUtilActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var configUtilActivityPresenter: TestFontScaleConfigurationUtilActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val storyTextSize = intent.getStringExtra(FONT_SCALE_EXTRA_KEY)
    configUtilActivityPresenter.handleOnCreate(storyTextSize)
  }

  companion object {
    private const val FONT_SCALE_EXTRA_KEY = "FONT_SCALE_EXTRA_KEY"

    /** Returns a new [TestFontScaleConfigurationUtilActivity] for context and story text size. */
    fun createFontScaleTestActivity(context: Context, storyTextSize: String): Intent {
      val intent = Intent(context, TestFontScaleConfigurationUtilActivity::class.java)
      intent.putExtra(FONT_SCALE_EXTRA_KEY, storyTextSize)
      return intent
    }
  }
}
