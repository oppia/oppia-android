package org.oppia.app.testing.options

import android.content.Intent
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.options.APP_LANGUAGE
import org.oppia.app.options.AppLanguageActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
class AppLanguageFragmentTest {

  @Test
  @LooperMode(LooperMode.Mode.PAUSED)
  fun changeAppLanguageToFrench_changeConfiguration_selectedLanguageIsFrench() {
    launch<AppLanguageActivity>(createAppLanguageActivityIntent("English")).use {
      onView(
        atPositionOnView(
          R.id.language_recycler_view,
          1, R.id.language_radio_button
        )
      ).perform(
        click()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.language_recycler_view,
          1, R.id.language_radio_button
        )
      ).check(matches(isChecked()))
    }
  }

  private fun createAppLanguageActivityIntent(summaryValue: String): Intent {
    return AppLanguageActivity.createAppLanguageActivityIntent(
      ApplicationProvider.getApplicationContext(),
      APP_LANGUAGE,
      summaryValue
    )
  }
}
