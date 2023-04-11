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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

private const val SKILL_ID_ARGUMENT_KEY = "ConceptCardFragment.skill_id"
private const val PROFILE_ID_ARGUMENT_KEY = "ConceptCardFragment.profile_id"

/* Fragment that displays a fullscreen dialog for concept cards */
class ConceptCardFragment : InjectableDialogFragment() {

  companion object {
    /** The fragment tag corresponding to the concept card dialog fragment. */
    private const val CONCEPT_CARD_DIALOG_FRAGMENT_TAG = "CONCEPT_CARD_FRAGMENT"

    /**
     * Creates a new fragment to show a concept card.
     *
     * @param skillId the skill ID for which a concept card should be loaded
     * @param profileId the profile in which the concept card will be shown
     * @return a new [ConceptCardFragment] to display the specified concept card
     */
    private fun newInstance(skillId: String, profileId: ProfileId): ConceptCardFragment {
      return ConceptCardFragment().apply {
        arguments = Bundle().apply {
          putString(SKILL_ID_ARGUMENT_KEY, skillId)
          putProto(PROFILE_ID_ARGUMENT_KEY, profileId)
        }
      }
    }

    fun bringToFrontOrCreateIfNew(
      skillId: String,
      profileId: ProfileId,
      fragmentManager: FragmentManager
    ) {
      // The UI is recreated when changing profile ids, so no need to include it in the tag name.
      val tag = "$CONCEPT_CARD_DIALOG_FRAGMENT_TAG:$skillId"
      val currentFragment = fragmentManager.findFragmentByTag(tag)
      if (currentFragment == null) {
        val newFragment = showNewInstance(skillId, profileId, fragmentManager, tag)
        val allConceptCards = fragmentManager.fragments.filterIsInstance<ConceptCardFragment>()
          .filter { fragment -> fragment != newFragment }
        if (allConceptCards.isNotEmpty()) {
          val transaction = fragmentManager.beginTransaction()
          for (toRemove in allConceptCards) {
            transaction.remove(toRemove)
          }
          transaction.commitNow()
        }
      }
    }

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
      tag: String
    ): ConceptCardFragment {
      val conceptCardFragment = newInstance(skillId, profileId)
      conceptCardFragment.showNow(fragmentManager, tag)
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
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to ConceptCardFragment"
    }
    val skillId =
      checkNotNull(args.getStringFromBundle(SKILL_ID_ARGUMENT_KEY)) {
        "Expected skillId to be passed to ConceptCardFragment"
      }
    val profileId = args.getProto(PROFILE_ID_ARGUMENT_KEY, ProfileId.getDefaultInstance())
    return conceptCardFragmentPresenter.handleCreateView(inflater, container, skillId, profileId)
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.FullScreenDialogStyle)
  }
}
