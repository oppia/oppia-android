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
    val readingTextSize = intent.getStringExtra(FONT_SCALE_EXTRA_KEY)
    configUtilActivityPresenter.handleOnCreate(readingTextSize)
  }

  companion object {
    private const val FONT_SCALE_EXTRA_KEY = 
      "TestFontScaleConfigurationUtilActivity.font_scale"

    /** Returns a new [TestFontScaleConfigurationUtilActivity] for context and reading text size. */
    fun createFontScaleTestActivity(context: Context, readingTextSize: String): Intent {
      val intent = Intent(context, TestFontScaleConfigurationUtilActivity::class.java)
      intent.putExtra(FONT_SCALE_EXTRA_KEY, readingTextSize)
      return intent
    }
  }
}
