package org.oppia.android.util.logging

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [EventTypeToHumanReadableNameConverter].
 *
 * Note that this suite has special change detector tests to ensure that the converter conforms to
 * event logger restrictions (such as not having an event name longer than a certain number), which
 * means some of these tests don't necessarily follow best practices when it comes to minimizing
 * control flow--that's fine for these. The actual domain correctness of these names are verified
 * elsewhere (e.g. in [EventBundleCreatorTest]).
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = EventTypeToHumanReadableNameConverterTest.TestApplication::class)
class EventTypeToHumanReadableNameConverterTest {
  @Inject lateinit var converter: EventTypeToHumanReadableNameConverter

  private companion object {
    private val FAILURE_TYPES = setOf(
      ActivityContextCase.INSTALL_ID_FOR_FAILED_ANALYTICS_LOG,
      ActivityContextCase.ACTIVITYCONTEXT_NOT_SET
    )
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testConvertToHumanReadableName_nonErrorTypes_returnsUniqueNameForEach() {
    val nonErrorContexts = ActivityContextCase.values().toSet() - FAILURE_TYPES

    val nonErrorNames = nonErrorContexts.map(converter::convertToHumanReadableName)

    assertThat(nonErrorNames).containsNoDuplicates()
  }

  @Test
  fun testConvertToHumanReadableName_errorTypes_shareTheSameName() {
    val errorContexts = FAILURE_TYPES

    val errorNames = errorContexts.map(converter::convertToHumanReadableName)

    assertThat(errorNames.toSet()).hasSize(1)
  }

  @Test
  fun testConvertToHumanReadableName_errorTypes_doNotShareNamesWithNonErrorTypes() {
    val errorContexts = FAILURE_TYPES
    val nonErrorContexts = ActivityContextCase.values().toSet() - errorContexts

    val nonErrorNames = nonErrorContexts.map(converter::convertToHumanReadableName).toSet()
    val errorNames = errorContexts.map(converter::convertToHumanReadableName).toSet()

    assertThat(errorNames).containsNoneIn(nonErrorNames)
  }

  @Test
  fun testConvertToHumanReadableName_allNamesLessThan40Chars() {
    val allContexts = ActivityContextCase.values()
    val allNames = allContexts.map(converter::convertToHumanReadableName)

    val namesLongerThan40 = allNames.filter { it.length > 40 }

    assertWithMessage("Expected no names longer than 40 chars").that(namesLongerThan40).isEmpty()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: EventTypeToHumanReadableNameConverterTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerEventTypeToHumanReadableNameConverterTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: EventTypeToHumanReadableNameConverterTest) {
      component.inject(test)
    }
  }
}
