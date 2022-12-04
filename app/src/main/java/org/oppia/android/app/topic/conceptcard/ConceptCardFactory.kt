package org.oppia.android.app.topic.conceptcard

import android.os.Bundle
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

class ConceptCardFactory @Inject constructor(
  private val conceptCardStackManager: ConceptCardBackStackManager
) : ConceptCardFragment.DestroyObserver {

  companion object {
    private const val SKILL_ID_ARGUMENT_KEY = "ConceptCardFragment.skill_id"
    private const val PROFILE_ID_ARGUMENT_KEY = "ConceptCardFragment.profile_id"
  }

  /**
   * Creates a new fragment object to show a concept card.
   *
   * @param skillId the skill ID for which a concept card should be loaded
   * @param profileId the profile in which the concept card will be shown
   * @return a new [ConceptCardFragment] to display the specified concept card
   */
  fun createCard(skillId: String, profileId: ProfileId): ConceptCardFragment? {
    if (conceptCardStackManager.getSize() != 0) {
      val currSkillId = conceptCardStackManager.peek()
      if (skillId == currSkillId) {
        return null
      } else {
        handleStackManagerWhenCardCreated(false, skillId)
        return card(skillId, profileId)
      }
    } else {
      handleStackManagerWhenCardCreated(true, skillId)
      return card(skillId, profileId)
    }
  }

  override fun onCardDestroyed() {
    handleStackWhenCardDestroy()
  }

  private fun handleStackWhenCardDestroy() {
    conceptCardStackManager.remove()
    if (conceptCardStackManager.getSize() ==
      ConceptCardBackStackManager.DEFAULT_STACK_SIZE
    ) {
      conceptCardStackManager.destroyBackStack()
    }
  }

  private fun card(skillId: String, profileId: ProfileId): ConceptCardFragment {
    return ConceptCardFragment(this).apply {
      arguments = Bundle().apply {
        putString(SKILL_ID_ARGUMENT_KEY, skillId)
        putProto(PROFILE_ID_ARGUMENT_KEY, profileId)
      }
    }
  }

  private fun handleStackManagerWhenCardCreated(isStackEmpty: Boolean, skillId: String) {
    if (isStackEmpty) {
      conceptCardStackManager.initBackStack()
    }
    conceptCardStackManager.addToStack(skillId)
  }
}
