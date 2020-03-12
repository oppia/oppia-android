package org.oppia.app.administratorcontrols

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.BuildConfig
import org.oppia.app.R
import org.oppia.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AppVersionActivity]. */
@RunWith(AndroidJUnit4::class)
class AppVersionActivityTest {

  @Inject lateinit var context: Context
  private lateinit var lastUpdateDate: String

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()

    val lastUpdateDateTime = context.packageManager.getPackageInfo(context.packageName, /* flags= */ 0).lastUpdateTime
    lastUpdateDate = getDateTime(lastUpdateDateTime)!!
  }

  private fun setUpTestApplicationComponent() {
    DaggerAppVersionActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testAppVersionActivity_loadFragment_displaysAppVersion() {
    launchAppVersionActivityIntent().use {
      onView(
        withText(
          String.format(
            context.resources.getString(R.string.app_version_name),
            BuildConfig.VERSION_NAME
          )
        )
      ).check(matches(isDisplayed()))
      onView(withText(String.format(context.resources.getString(R.string.app_last_update_date), lastUpdateDate))).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testAppVersionActivity_loadFragment_onBackPressed_displaysAdministratorControlsActivity() {
    ActivityScenario.launch<AdministratorControlsActivity>(launchAdministratorControlsActivityIntent(0)).use {
      onView(withId(R.id.administrator_controls_list)).perform(scrollToPosition<RecyclerView.ViewHolder>(3))
      onView(withText(R.string.administrator_controls_app_version)).perform(click())
      intended(hasComponent(AppVersionActivity::class.java.name))
      onView(isRoot()).perform(pressBack())
      onView(withId(R.id.administrator_controls_list)).check(matches(isDisplayed()))
    }
  }

  // TODO(#555): Create one central utility file from where we should access date format or even convert date timestamp to string from that file.
  private fun getDateTime(l: Long): String? {
    return try {
      val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.US)
      val netDate = Date(l)
      sdf.format(netDate)
    } catch (e: Exception) {
      e.toString()
    }
  }

  private fun launchAppVersionActivityIntent(): ActivityScenario<AppVersionActivity> {
    val intent = AppVersionActivity.createAppVersionActivityIntent(
      ApplicationProvider.getApplicationContext()
    )
    return ActivityScenario.launch(intent)
  }

  private fun launchAdministratorControlsActivityIntent(profileId: Int): Intent {
    return AdministratorControlsActivity.createAdministratorControlsActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#89): Introduce a proper IdlingResource for background dispatchers to ensure they all complete before
    //  proceeding in an Espresso test. This solution should also be interoperative with Robolectric contexts by using a
    //  test coroutine dispatcher.

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@BlockingDispatcher blockingDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return blockingDispatcher
    }
  }

  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(appVersionActivityTest: AppVersionActivityTest)
  }
}
