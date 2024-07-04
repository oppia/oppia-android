package org.oppia.android.app.topic.conceptcard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.ConceptCardFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

private const val SKILL_ID_ARGUMENT_KEY = "ConceptCardFragment.skill_id"
private const val PROFILE_ID_ARGUMENT_KEY = "ConceptCardFragment.profile_id"

/* Fragment that displays a fullscreen dialog for concept cards */
class ConceptCardFragment : InjectableDialogFragment() {

  companion object {

    const val CONCEPT_CARD_FRAGMENT_ARGUMENTS_KEY = "ConceptCardFragment.arguments"

    /** The fragment tag corresponding to the concept card dialog fragment. */
    private const val CONCEPT_CARD_DIALOG_FRAGMENT_TAG = "CONCEPT_CARD_FRAGMENT"

    private fun newInstance(skillId: String, profileId: ProfileId): ConceptCardFragment {
      val args = ConceptCardFragmentArguments.newBuilder().apply {
        this.skillId = skillId
      }.build()
      return ConceptCardFragment().apply {
        arguments = Bundle().apply {
          putProto(CONCEPT_CARD_FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
    }

    /**
     * Removes any [ConceptCardFragment] in the given FragmentManager that is not of the given
     * skill Id. If no [ConceptCardFragment] remains, creates a new fragment to show a concept card
     * for the given skill id.
     *
     * @param skillId the skill ID for which a concept card should be loaded
     * @param profileId the profile in which the concept card will be shown
     * @param fragmentManager the [FragmentManager] where to show the concept card
     */
    fun bringToFrontOrCreateIfNew(
      skillId: String,
      profileId: ProfileId,
      fragmentManager: FragmentManager
    ) {
      // Concept cards are keyed by profileId and skillId. However, in this method we are only
      // using the skillId for equality checks. The reason is that when the user switches profiles
      // the UI is recreated, so that it is not possible to have concept cards from different
      // profiles in the same fragment manager.
      val allConceptCards = fragmentManager.fragments.filterIsInstance<ConceptCardFragment>()
      val conceptCardsWithDifferentSkillId = allConceptCards.filter { skillId != it.getSkillId() }
      if (conceptCardsWithDifferentSkillId.isNotEmpty()) {
        val transaction = fragmentManager.beginTransaction()
        for (toRemove in conceptCardsWithDifferentSkillId) {
          transaction.remove(toRemove)
        }
        transaction.commitNow()
      }
      if (allConceptCards.size <= conceptCardsWithDifferentSkillId.size) {
        showNewInstance(skillId, profileId, fragmentManager)
      }
    }

    /**
     * Removes all [ConceptCardFragment] in the given FragmentManager.
     *
     * @param fragmentManager the [FragmentManager] from where to remove all concept cards.
     */
    fun dismissAll(fragmentManager: FragmentManager) {
      val toDismiss = fragmentManager.fragments.filterIsInstance<ConceptCardFragment>()
      if (toDismiss.isNotEmpty()) {
        val transaction = fragmentManager.beginTransaction()
        for (fragment in toDismiss) {
          transaction.remove(fragment)
        }
        transaction.commitNow()
      }
    }

    private fun showNewInstance(
      skillId: String,
      profileId: ProfileId,
      fragmentManager: FragmentManager,
    ): ConceptCardFragment {
      val conceptCardFragment = newInstance(skillId, profileId)
      conceptCardFragment.showNow(fragmentManager, CONCEPT_CARD_DIALOG_FRAGMENT_TAG)
      return conceptCardFragment
    }
  }

  @Inject
  lateinit var conceptCardFragmentPresenter: ConceptCardFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    val arguments = checkNotNull(arguments) {
      "Expected arguments to be passed to ConceptCardFragment"
    }
    val args = arguments.getProto(
      CONCEPT_CARD_FRAGMENT_ARGUMENTS_KEY,
      ConceptCardFragmentArguments.getDefaultInstance()
    )

    val skillId =
      checkNotNull(args.skillId) {
        "Expected skillId to be passed to ConceptCardFragment"
      }
    val profileId = arguments.extractCurrentUserProfileId()
    return conceptCardFragmentPresenter.handleCreateView(inflater, container, skillId, profileId)
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.FullScreenDialogStyle)
  }

  private fun getSkillId(): String? {
    return arguments?.getProto(
      CONCEPT_CARD_FRAGMENT_ARGUMENTS_KEY,
      ConceptCardFragmentArguments.getDefaultInstance()
    )?.skillId
  }
}
