package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.TestFontScaleConfigurationUtilActivityParams
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject

/** Test activity used for testing font scale. */
class TestFontScaleConfigurationUtilActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var configUtilActivityPresenter: TestFontScaleConfigurationUtilActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    @Suppress("DEPRECATION") // TODO(#5405): Ensure the correct type is being retrieved.
    val readingTextSize = checkNotNull(
      intent.getProtoExtra(
        TEST_FONT_SCALE_CONFIGURATION_UTIL_ACTIVITY_PARAMS_KEY,
        TestFontScaleConfigurationUtilActivityParams.getDefaultInstance()
      ).readingTextSize
    ) { "Expected $FONT_SCALE_EXTRA_KEY to be in intent extras." }
    configUtilActivityPresenter.handleOnCreate(readingTextSize)
  }

  companion object {
    /** Params key for TestFontScaleConfigurationUtilActivity. */
    const val TEST_FONT_SCALE_CONFIGURATION_UTIL_ACTIVITY_PARAMS_KEY =
      "TestFontScaleConfigurationUtilActivity.params"

    private const val FONT_SCALE_EXTRA_KEY = "TestFontScaleConfigurationUtilActivity.font_scale"

    /** Returns a new [TestFontScaleConfigurationUtilActivity] for context and reading text size. */
    fun createFontScaleTestActivity(context: Context, readingTextSize: ReadingTextSize): Intent {
      val args = TestFontScaleConfigurationUtilActivityParams.newBuilder()
        .setReadingTextSize(readingTextSize).build()
      val intent = Intent(context, TestFontScaleConfigurationUtilActivity::class.java)
      intent.putProtoExtra(TEST_FONT_SCALE_CONFIGURATION_UTIL_ACTIVITY_PARAMS_KEY, args)
      return intent
    }
  }
}
