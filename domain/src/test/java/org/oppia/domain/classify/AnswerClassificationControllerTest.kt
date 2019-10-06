package org.oppia.domain.classify

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.Outcome
import org.oppia.app.model.State
import org.oppia.app.model.SubtitledHtml
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AnswerClassificationController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AnswerClassificationControllerTest {
  private val TEST_STRING_ANSWER = InteractionObject.newBuilder().setNormalizedString("Some value").build()
  private val TEST_INT_2_ANSWER = InteractionObject.newBuilder().setNonNegativeInt(1).build()
  private val TEST_SIGNED_INT_121_ANSWER = InteractionObject.newBuilder().setSignedInt(121).build()

  private val OUTCOME_0 = Outcome.newBuilder()
    .setDestStateName("First state")
    .setFeedback(SubtitledHtml.newBuilder().setContentId("content_id_0").setHtml("Feedback 1"))
    .build()
  private val OUTCOME_1 = Outcome.newBuilder()
    .setDestStateName("Second state")
    .setFeedback(SubtitledHtml.newBuilder().setContentId("content_id_1").setHtml("Feedback 2"))
    .build()
  private val OUTCOME_2 = Outcome.newBuilder()
    .setDestStateName("Third state")
    .setFeedback(SubtitledHtml.newBuilder().setContentId("content_id_2").setHtml("Feedback 3"))
    .build()

  @Inject
  lateinit var answerClassificationController: AnswerClassificationController

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testClassify_testInteraction_withOnlyDefaultOutcome_returnsDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setDefaultOutcome(OUTCOME_0)
      .build()

    val state = createTestState("Things you can do", interaction)
    val outcome = answerClassificationController.classify(state, TEST_STRING_ANSWER)

    assertThat(outcome).isEqualTo(OUTCOME_0)
  }

  @Test
  fun testClassify_testInteraction_withMultipleOutcomes_wrongAnswer_returnsDefaultOutcome() {
    val interaction = Interaction.newBuilder()
      .setDefaultOutcome(OUTCOME_1)
      .addAnswerGroups(AnswerGroup.newBuilder().setOutcome(OUTCOME_2))
      .build()

    val state = createTestState("Welcome!", interaction)
    val outcome = answerClassificationController.classify(state, TEST_INT_2_ANSWER)

    assertThat(outcome).isEqualTo(OUTCOME_1)
  }

  @Test
  fun testClassify_afterPreviousInteraction_returnsDefaultOutcomeOfSecondInteraction() {
    val interaction1 = Interaction.newBuilder()
      .setDefaultOutcome(OUTCOME_1)
      .addAnswerGroups(AnswerGroup.newBuilder().setOutcome(OUTCOME_0))
      .build()
    val interaction2 = Interaction.newBuilder()
      .setDefaultOutcome(OUTCOME_2)
      .build()
    val state1 = createTestState("Numeric input", interaction1)
    answerClassificationController.classify(state1, TEST_SIGNED_INT_121_ANSWER)

    val state2 = createTestState("Things you can do", interaction2)
    val outcome = answerClassificationController.classify(state2, TEST_STRING_ANSWER)

    assertThat(outcome).isEqualTo(OUTCOME_2)
  }

  private fun createTestState(stateName: String, interaction: Interaction): State {
    return State.newBuilder()
      .setName(stateName)
      .setInteraction(interaction)
      .build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerAnswerClassificationControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class, TextInputRuleModule::class, InteractionsModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(answerClassificationControllerTest: AnswerClassificationControllerTest)
  }
}
