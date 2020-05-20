package org.oppia.domain.util

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.RuleSpec
import org.oppia.app.model.State
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.test.assertNotNull

const val TEST_EXPLORATION_NAME = "test_prototype_exploration.json"

/** Tests for [StateRetriever]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class StateRetrieverTest {

  @Inject
  lateinit var stateRetriever: StateRetriever

  @Inject
  lateinit var jsonAssetRetriever: JsonAssetRetriever

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithIsEqualToOrderingRuleSpec() {
    val state = createStateFromJson("DragDropSortInput")
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsEqualToOrdering")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXAtPositionYRuleSpec() {
    val state = createStateFromJson("DragDropSortInput")
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("HasElementXAtPositionY")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithIsEqualToOrderingWithOneItemAtIncorrectPositionRuleSpec() {
    val state = createStateFromJson("DragDropSortInput")
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("IsEqualToOrderingWithOneItemAtIncorrectPosition")
  }

  @Test
  fun testParseState_withDragAndDropInteraction_parsesRuleWithHasElementXBeforeElementYRuleSpec() {
    val state = createStateFromJson("DragDropSortInput")
    val ruleSpecMap = state.interaction.answerGroupsList
      .flatMap(AnswerGroup::getRuleSpecsList)
      .associateBy(RuleSpec::getRuleType)
    assertThat(ruleSpecMap).containsKey("HasElementXBeforeElementY")
  }

  private fun createStateFromJson(stateName: String): State {
    val json = jsonAssetRetriever.loadJsonFromAsset(TEST_EXPLORATION_NAME)
    return stateRetriever.createStateFromJson(
      stateName,
      json?.getJSONObject("states")?.getJSONObject(stateName)
    )
  }

  private fun setUpTestApplicationComponent() {
    DaggerStateRetrieverTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Qualifier
  annotation class TestDispatcher

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE

    @CacheAssetsLocally
    @Provides
    fun provideCacheAssetsLocally(): Boolean = false
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(stateRetrieverTest: StateRetrieverTest)
  }
}
