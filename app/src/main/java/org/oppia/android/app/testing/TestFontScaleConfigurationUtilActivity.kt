package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ReadingTextSize
import javax.inject.Inject

/** Test activity used for testing font scale. */
class TestFontScaleConfigurationUtilActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var configUtilActivityPresenter: TestFontScaleConfigurationUtilActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val readingTextSize = checkNotNull(
      intent.getSerializableExtra(FONT_SCALE_EXTRA_KEY) as? ReadingTextSize
    ) { "Expected $FONT_SCALE_EXTRA_KEY to be in intent extras." }
    configUtilActivityPresenter.handleOnCreate(readingTextSize)
  }

  companion object {
    private const val FONT_SCALE_EXTRA_KEY = "TestFontScaleConfigurationUtilActivity.font_scale"

    /** Returns a new [TestFontScaleConfigurationUtilActivity] for context and reading text size. */
    fun createFontScaleTestActivity(context: Context, readingTextSize: ReadingTextSize): Intent {
      val intent = Intent(context, TestFontScaleConfigurationUtilActivity::class.java)
      intent.putExtra(FONT_SCALE_EXTRA_KEY, readingTextSize)
      return intent
    }
  }
}
