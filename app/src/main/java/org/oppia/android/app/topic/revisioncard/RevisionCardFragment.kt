package org.oppia.app.topic.revisioncard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/* Fragment that displays revision card */
class RevisionCardFragment : InjectableDialogFragment() {
  companion object {
    internal const val TOPIC_ID_ARGUMENT_KEY = "TOPIC_ID_ARGUMENT_KEY"
    internal const val SUBTOPIC_ID_ARGUMENT_KEY = "SUBOPIC_ID_ARGUMENT_KEY"

    /** Returns a new [RevisionCardFragment] to display the subtopic content.. */
    fun newInstance(topicId: String, subtopicId: Int): RevisionCardFragment {
      val revisionCardFragment = RevisionCardFragment()
      val args = Bundle()
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      args.putInt(SUBTOPIC_ID_ARGUMENT_KEY, subtopicId)
      revisionCardFragment.arguments = args
      return revisionCardFragment
    }
  }

  @Inject
  lateinit var revisionCardFragmentPresenter: RevisionCardFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
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
      checkNotNull(args.getString(TOPIC_ID_ARGUMENT_KEY)) {
        "Expected topicId to be passed to RevisionCardFragment"
      }
    val subtopicId = args.getInt(SUBTOPIC_ID_ARGUMENT_KEY, -1)
    return revisionCardFragmentPresenter.handleCreateView(inflater, container, topicId, subtopicId)
  }
}
