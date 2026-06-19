package org.oppia.android.app.topic.studyguide

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.StudyGuideFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that displays a study guide as a sequence of sections. */
class StudyGuideFragment : InjectableDialogFragment() {
  companion object {
    /** Arguments key for StudyGuideFragment. */
    const val STUDY_GUIDE_FRAGMENT_ARGUMENTS_KEY = "StudyGuideFragment.arguments"

    /**
     * Returns a new [StudyGuideFragment] to display the specific subtopic for the given topic &
     * profile.
     */
    fun newInstance(
      topicId: String,
      subtopicId: Int,
      profileId: LegacyProfileId,
      subtopicListSize: Int,
      readingTextSize: ReadingTextSize
    ): StudyGuideFragment {
      val args = StudyGuideFragmentArguments.newBuilder().apply {
        this.topicId = topicId
        this.subtopicId = subtopicId
        this.subtopicListSize = subtopicListSize
        this.readingTextSize = readingTextSize
      }.build()
      return StudyGuideFragment().apply {
        arguments = Bundle().apply {
          putProto(STUDY_GUIDE_FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
    }
  }

  @Inject
  lateinit var studyGuideFragmentPresenter: StudyGuideFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
    studyGuideFragmentPresenter.handleAttach(context)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    val arguments = checkNotNull(arguments) {
      "Expected arguments to be passed to StudyGuideFragment"
    }
    val args = arguments.getProto(
      STUDY_GUIDE_FRAGMENT_ARGUMENTS_KEY,
      StudyGuideFragmentArguments.getDefaultInstance()
    )

    val topicId =
      checkNotNull(args?.topicId) {
        "Expected topicId to be passed to StudyGuideFragment"
      }
    val subtopicId = args?.subtopicId ?: -1
    val profileId = arguments.extractCurrentUserProfileId()
    val subtopicListSize = args?.subtopicListSize ?: -1
    return studyGuideFragmentPresenter.handleCreateView(
      inflater, container, topicId, subtopicId, profileId, subtopicListSize
    )
  }

  /** Dismisses the concept card fragment if it's currently active in this fragment. */
  fun dismissConceptCard() = studyGuideFragmentPresenter.dismissConceptCard()
}
