package org.oppia.domain.exploration

import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.Exploration
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.Outcome
import org.oppia.app.model.RuleSpec
import org.oppia.app.model.State
import org.oppia.app.model.SubtitledHtml
import javax.inject.Inject

const val TEST_EXPLORATION_ID_5 = "test_exp_id_5"

// TODO(#59): Make this class inaccessible outside of the domain package. UI code should not depend on it.

/** Internal class for actually retrieving an exploration object for uses in domain controllers. */
class ExplorationRetriever @Inject constructor() {
  /** Loads and returns an exploration for the specified exploration ID, or fails. */
  @Suppress("RedundantSuspendModifier") // Force callers to call this on a background thread.
  internal suspend fun loadExploration(explorationId: String): Exploration {
    // TODO(#193): Load this exploration from a file, instead.
    check(explorationId == TEST_EXPLORATION_ID_5) { "Invalid exploration ID: $explorationId" }
    return createTestExploration0()
  }

  private fun createTestExploration0(): Exploration {
    return Exploration.newBuilder()
      .setId(TEST_EXPLORATION_ID_5)
      .putStates(TEST_INIT_STATE_NAME, createInitState())
      .putStates(TEST_MIDDLE_STATE_NAME, createStateWithTwoOutcomes())
      .putStates(TEST_END_STATE_NAME, createTerminalState())
      .setInitStateName(TEST_INIT_STATE_NAME)
      .setObjective("To provide a stub for the UI to reasonably interact with ExplorationProgressController.")
      .setTitle("Stub Exploration")
      .setLanguageCode("en")
      .build()
  }

  private fun createInitState(): State {
    return State.newBuilder()
      .setName(TEST_INIT_STATE_NAME)
      .setContent(SubtitledHtml.newBuilder().setContentId("state_0_content").setHtml("First State"))
      .setInteraction(
        Interaction.newBuilder()
          .setId("Continue")
          .addAnswerGroups(
            AnswerGroup.newBuilder()
              .setOutcome(
                Outcome.newBuilder()
                  .setDestStateName(TEST_MIDDLE_STATE_NAME)
                  .setFeedback(SubtitledHtml.newBuilder().setContentId("state_0_feedback").setHtml("Let's continue."))
              )
          )
      )
      .build()
  }

  private fun createStateWithTwoOutcomes(): State {
    return State.newBuilder()
      .setName(TEST_MIDDLE_STATE_NAME)
      .setContent(SubtitledHtml.newBuilder().setContentId("state_1_content").setHtml("What language is 'Oppia' from?"))
      .setInteraction(
        Interaction.newBuilder()
          .setId("TextInput")
          .addAnswerGroups(
            AnswerGroup.newBuilder()
              .addRuleSpecs(
                RuleSpec.newBuilder()
                  .putInputs("x", InteractionObject.newBuilder().setNormalizedString("Finnish").build())
                  .setRuleType("CaseSensitiveEquals")
              )
              .setOutcome(
                Outcome.newBuilder()
                  .setDestStateName(TEST_END_STATE_NAME)
                  .setFeedback(SubtitledHtml.newBuilder().setContentId("state_1_pos_feedback").setHtml("Correct!"))
              )
          )
          .setDefaultOutcome(
            Outcome.newBuilder()
              .setDestStateName(TEST_MIDDLE_STATE_NAME)
              .setFeedback(SubtitledHtml.newBuilder().setContentId("state_1_neg_feedback").setHtml("Not quite right."))
          )
      )
      .build()
  }

  private fun createTerminalState(): State {
    return State.newBuilder()
      .setName(TEST_END_STATE_NAME)
      .setContent(SubtitledHtml.newBuilder().setContentId("state_2_content").setHtml("Thanks for playing"))
      .setInteraction(Interaction.newBuilder().setId("EndExploration"))
      .build()
  }
}
