package org.oppia.android.domain.exploration.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FakeExplorationRetriever]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FakeExplorationRetrieverTest.TestApplication::class)
class FakeExplorationRetrieverTest {
  @field:[Rule JvmField] val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var fakeExplorationRetriever: FakeExplorationRetriever

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLoadExploration_noProxySet_realExpId_returnsLoadedExploration() {
    val exp = runBlocking { fakeExplorationRetriever.loadExploration(TEST_EXPLORATION_ID_2) }

    // The fake should behave like a production implementation when no proxy overrides are set.
    assertThat(exp.id).isEqualTo(TEST_EXPLORATION_ID_2)
  }

  @Test
  fun testLoadExploration_noProxySet_fakeExpId_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      runBlocking { fakeExplorationRetriever.loadExploration("fake_id") }
    }

    assertThat(exception).hasMessageThat().contains("Asset doesn't exist: fake_id")
  }

  @Test
  fun testLoadExploration_realId_withProxyOverrideToValidId_returnsProxyExploration() {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = TEST_EXPLORATION_ID_2, expIdToLoadInstead = TEST_EXPLORATION_ID_5
    )

    val exp = runBlocking { fakeExplorationRetriever.loadExploration(TEST_EXPLORATION_ID_2) }

    // The fake should cause a different exploration to load, instead.
    assertThat(exp.id).isEqualTo(TEST_EXPLORATION_ID_5)
  }

  @Test
  fun testLoadExploration_realId_withProxyOverrideToInvalidId_throwsException() {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = TEST_EXPLORATION_ID_2, expIdToLoadInstead = "fake_id"
    )

    val exception = assertThrows(IllegalStateException::class) {
      runBlocking { fakeExplorationRetriever.loadExploration(TEST_EXPLORATION_ID_2) }
    }

    assertThat(exception).hasMessageThat().contains("Asset doesn't exist: fake_id")
  }

  @Test
  fun testLoadExploration_realId_withUnrelatedProxyOverride_returnsOriginalExploration() {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = TEST_EXPLORATION_ID_5, expIdToLoadInstead = "fake_id"
    )

    val exp = runBlocking { fakeExplorationRetriever.loadExploration(TEST_EXPLORATION_ID_2) }

    // The proxy binding is unrelated to the exploration being loaded, so nothing should change.
    assertThat(exp.id).isEqualTo(TEST_EXPLORATION_ID_2)
  }

  @Test
  fun testLoadExploration_fakeId_withProxyOverrideToValidId_returnsProxyExploration() {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = "fake_id", expIdToLoadInstead = TEST_EXPLORATION_ID_2
    )

    val exp = runBlocking { fakeExplorationRetriever.loadExploration("fake_id") }

    // A fake ID can load properly if it points to a valid exploration.
    assertThat(exp.id).isEqualTo(TEST_EXPLORATION_ID_2)
  }

  @Test
  fun testLoadExploration_fakeId_withProxyOverrideToInvalidId_throwsException() {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = "fake_id", expIdToLoadInstead = "other_fake_id"
    )

    val exception = assertThrows(IllegalStateException::class) {
      runBlocking { fakeExplorationRetriever.loadExploration("fake_id") }
    }

    assertThat(exception).hasMessageThat().contains("Asset doesn't exist: other_fake_id")
  }

  @Test
  fun testLoadExploration_fakeId_withUnrelatedProxyOverride_throwsException() {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = TEST_EXPLORATION_ID_2, expIdToLoadInstead = TEST_EXPLORATION_ID_5
    )

    val exception = assertThrows(IllegalStateException::class) {
      runBlocking { fakeExplorationRetriever.loadExploration("fake_id") }
    }

    assertThat(exception).hasMessageThat().contains("Asset doesn't exist: fake_id")
  }

  @Test
  fun testLoadExploration_fakeId_withProxyOverrideToValidId_setTwice_returnsLatestProxyExp() {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = "fake_id", expIdToLoadInstead = TEST_EXPLORATION_ID_2
    )
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = "fake_id", expIdToLoadInstead = TEST_EXPLORATION_ID_5
    )

    val exp = runBlocking { fakeExplorationRetriever.loadExploration("fake_id") }

    // The latest proxy should take precedence.
    assertThat(exp.id).isEqualTo(TEST_EXPLORATION_ID_5)
  }

  @Test
  fun testLoadExploration_realId_afterClearingValidProxy_returnsOriginalExploration() {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = TEST_EXPLORATION_ID_2, expIdToLoadInstead = TEST_EXPLORATION_ID_5
    )

    fakeExplorationRetriever.clearExplorationProxy(TEST_EXPLORATION_ID_2)
    val exp = runBlocking { fakeExplorationRetriever.loadExploration(TEST_EXPLORATION_ID_2) }

    // Clearing the proxy should cause the original exploration to load.
    assertThat(exp.id).isEqualTo(TEST_EXPLORATION_ID_2)
  }

  @Test
  fun testLoadExploration_fakeId_afterClearingValidProxy_throwsException() {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = "fake_id", expIdToLoadInstead = TEST_EXPLORATION_ID_2
    )

    fakeExplorationRetriever.clearExplorationProxy("fake_id")
    val exception = assertThrows(IllegalStateException::class) {
      runBlocking { fakeExplorationRetriever.loadExploration("fake_id") }
    }

    // Clearing the proxy reverts to the normal state (which will lead to a crash).
    assertThat(exception).hasMessageThat().contains("Asset doesn't exist: fake_id")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, LocaleTestModule::class, FakeOppiaClockModule::class, AssetModule::class,
      LoggerModule::class, TestDispatcherModule::class, RobolectricModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: FakeExplorationRetrieverTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerFakeExplorationRetrieverTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: FakeExplorationRetrieverTest) {
      component.inject(test)
    }
  }
}
