package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.utility.FontScaleConfigurationUtil
import javax.inject.Inject

/** The presenter for [TestFontScaleConfigurationUtilActivity] */
@ActivityScope
class TestFontScaleConfigurationUtilActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil
) {
  fun handleOnCreate(readingTextSize: String) {
    fontScaleConfigurationUtil.adjustFontScale(activity, readingTextSize)
    activity.setContentView(R.layout.font_scale_test_activity)
  }
}
