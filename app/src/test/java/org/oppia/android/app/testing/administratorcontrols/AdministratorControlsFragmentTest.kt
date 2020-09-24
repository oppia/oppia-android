package org.oppia.app.testing.administratorcontrols

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.app.administratorcontrols.appversion.AppVersionFragment
import org.oppia.app.settings.profile.ProfileListActivity
import org.oppia.app.settings.profile.ProfileListFragment
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class AdministratorControlsFragmentTest {

  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testAdministratorControlsFragment_clickEditProfile_checkSendingTheCorrectIntent() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        0
      )
    ).use {
      onView(withId(R.id.edit_profiles_text_view))
        .perform(click())
      intended(hasComponent(ProfileListActivity::class.java.name))
    }
  }

  @Test
  fun testAdministratorControlsFragment_clickAppVersion_checkSendingTheCorrectIntent() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        0
      )
    ).use {
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withId(R.id.app_version_text_view)).perform(click())
      intended(hasComponent(AppVersionActivity::class.java.name))
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControlsFragment_clickEditProfile_checkLoadingTheCorrectFragment() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        0
      )
    ).use {
      onView(withId(R.id.edit_profiles_text_view))
        .perform(click())
      it.onActivity { activity ->
        val fragment =
          activity.supportFragmentManager
            .findFragmentById(R.id.administrator_controls_fragment_multipane_placeholder)
        assertThat(fragment is ProfileListFragment).isTrue()
      }
    }
  }

  @Test
  @Config(qualifiers = "sw600dp")
  fun testAdministratorControlsFragment_clickAppVersion_checkLoadingTheCorrectFragment() {
    launch<AdministratorControlsActivity>(
      createAdministratorControlsActivityIntent(
        0
      )
    ).use {
      onView(withId(R.id.administrator_controls_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withId(R.id.app_version_text_view)).perform(click())
      it.onActivity { activity ->
        val fragment =
          activity.supportFragmentManager
            .findFragmentById(R.id.administrator_controls_fragment_multipane_placeholder)
        assertThat(fragment is AppVersionFragment).isTrue()
      }
    }
  }

  private fun createAdministratorControlsActivityIntent(profileId: Int): Intent {
    return AdministratorControlsActivity.createAdministratorControlsActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }
}
