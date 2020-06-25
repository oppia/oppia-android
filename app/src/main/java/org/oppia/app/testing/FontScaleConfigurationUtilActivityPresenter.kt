package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.utility.FontScaleConfigurationUtil
import javax.inject.Inject

/** The presenter for [FontScaleConfigurationUtilActivity] */
@ActivityScope
class FontScaleConfigurationUtilActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil
) {
  fun handleOnCreate(storyTextSize: String) {
    fontScaleConfigurationUtil.adjustFontScale(activity, storyTextSize)
    activity.setContentView(R.layout.font_scale_test_activity)
  }
}
