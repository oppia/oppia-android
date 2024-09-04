package org.oppia.android.app.topic.revisioncard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.RevisionCardFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/* Fragment that displays revision card */
class RevisionCardFragment : InjectableDialogFragment() {
  companion object {
    /** Arguments key for RevisionCardFragment. */
    const val REVISION_CARD_FRAGMENT_ARGUMENTS_KEY = "RevisionCardFragment.arguments"

    /**
     * Returns a new [RevisionCardFragment] to display the specific subtopic for the given topic &
     * profile.
     */
    fun newInstance(
      topicId: String,
      subtopicId: Int,
      profileId: ProfileId,
      subtopicListSize: Int,
      readingTextSize: ReadingTextSize
    ):
      RevisionCardFragment {
        val args = RevisionCardFragmentArguments.newBuilder().apply {
          this.topicId = topicId
          this.subtopicId = subtopicId
          this.subtopicListSize = subtopicListSize
          this.readingTextSize = readingTextSize
        }.build()
        return RevisionCardFragment().apply {
          arguments = Bundle().apply {
            putProto(REVISION_CARD_FRAGMENT_ARGUMENTS_KEY, args)
            decorateWithUserProfileId(profileId)
          }
        }
      }
  }

  @Inject
  lateinit var revisionCardFragmentPresenter: RevisionCardFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
    revisionCardFragmentPresenter.handleAttach(context)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    val arguments = checkNotNull(arguments) {
      "Expected arguments to be passed to StoryFragment"
    }
    val args = arguments.getProto(
      REVISION_CARD_FRAGMENT_ARGUMENTS_KEY,
      RevisionCardFragmentArguments.getDefaultInstance()
    )

    val topicId =
      checkNotNull(args?.topicId) {
        "Expected topicId to be passed to RevisionCardFragment"
      }
    val subtopicId = args?.subtopicId ?: -1
    val profileId = arguments.extractCurrentUserProfileId()
    val subtopicListSize = args?.subtopicListSize ?: -1
    return revisionCardFragmentPresenter.handleCreateView(
      inflater, container, topicId, subtopicId, profileId, subtopicListSize
    )
  }

  /** Dismisses the concept card fragment if it's currently active in this fragment. */
  fun dismissConceptCard() = revisionCardFragmentPresenter.dismissConceptCard()
}
