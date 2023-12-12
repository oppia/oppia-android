package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ReadingTextSize
import javax.inject.Inject
import org.oppia.android.app.model.TestFontScaleConfigurationUtilActivityArguments
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra

/** Test activity used for testing font scale. */
class TestFontScaleConfigurationUtilActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var configUtilActivityPresenter: TestFontScaleConfigurationUtilActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val readingTextSize = checkNotNull(
      intent.getProtoExtra(
        TEST_FONT_SCALE_CONFIGURATION_UTIL_ACTIVITY_ARGUMENTS_KEY,
        TestFontScaleConfigurationUtilActivityArguments.getDefaultInstance()
      ).readingTextSize
    ) { "Expected $FONT_SCALE_EXTRA_KEY to be in intent extras." }
    configUtilActivityPresenter.handleOnCreate(readingTextSize)
  }

  companion object {
    /** Arguments key for TestFontScaleConfigurationUtilActivity. */
    const val TEST_FONT_SCALE_CONFIGURATION_UTIL_ACTIVITY_ARGUMENTS_KEY =
      "TestFontScaleConfigurationUtilActivity.arguments"

    private const val FONT_SCALE_EXTRA_KEY = "TestFontScaleConfigurationUtilActivity.font_scale"

    /** Returns a new [TestFontScaleConfigurationUtilActivity] for context and reading text size. */
    fun createFontScaleTestActivity(context: Context, readingTextSize: ReadingTextSize): Intent {
      val args = TestFontScaleConfigurationUtilActivityArguments.newBuilder()
        .setReadingTextSize(readingTextSize).build()
      val intent = Intent(context, TestFontScaleConfigurationUtilActivity::class.java)
      intent.putProtoExtra(TEST_FONT_SCALE_CONFIGURATION_UTIL_ACTIVITY_ARGUMENTS_KEY, args)
      return intent
    }
  }
}
