package org.oppia.android.app.topic.revisioncard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/* Fragment that displays revision card */
class RevisionCardFragment : InjectableDialogFragment() {
  companion object {
    private const val TOPIC_ID_ARGUMENT_KEY = "RevisionCardFragment.topic_id"
    private const val SUBTOPIC_ID_ARGUMENT_KEY = "RevisionCardFragment.subtopic_id"
    private const val SUBTOPIC_LIST_SIZE_ARGUMENT_KEY = "RevisionCardFragment.subtopic_list_size"
    private const val PROFILE_ID_ARGUMENT_KEY = "RevisionCardFragment.profile_id"

    /**
     * Returns a new [RevisionCardFragment] to display the specific subtopic for the given topic &
     * profile.
     */
    fun newInstance(topicId: String, subtopicId: Int, profileId: ProfileId, subtopicListSize: Int):
      RevisionCardFragment {
        return RevisionCardFragment().apply {
          arguments = Bundle().apply {
            putString(TOPIC_ID_ARGUMENT_KEY, topicId)
            putInt(SUBTOPIC_ID_ARGUMENT_KEY, subtopicId)
            putProto(PROFILE_ID_ARGUMENT_KEY, profileId)
            putInt(SUBTOPIC_LIST_SIZE_ARGUMENT_KEY, subtopicListSize)
          }
        }
      }
  }

  @Inject
  lateinit var revisionCardFragmentPresenter: RevisionCardFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to StoryFragment"
    }
    val topicId =
      checkNotNull(args.getStringFromBundle(TOPIC_ID_ARGUMENT_KEY)) {
        "Expected topicId to be passed to RevisionCardFragment"
      }
    val subtopicId = args.getInt(SUBTOPIC_ID_ARGUMENT_KEY, -1)
    val profileId = args.getProto(PROFILE_ID_ARGUMENT_KEY, ProfileId.getDefaultInstance())
    val subtopicListSize = args.getInt(SUBTOPIC_LIST_SIZE_ARGUMENT_KEY, -1)
    return revisionCardFragmentPresenter.handleCreateView(
      inflater, container, topicId, subtopicId, profileId, subtopicListSize
    )
  }

  /** Dismisses the concept card fragment if it's currently active in this fragment. */
  fun dismissConceptCard() = revisionCardFragmentPresenter.dismissConceptCard()
}
