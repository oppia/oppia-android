package org.oppia.app.topic.reviewcard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.R
import org.oppia.app.fragment.InjectableDialogFragment
import javax.inject.Inject

private const val KEY_TOPIC_ID = "TOPIC_NAME"
private const val KEY_SUBTOPIC_ID = "SUBTOPIC_ID"

/* Fragment that displays a fullscreen dialog for review cards */
class ReviewCardFragment : InjectableDialogFragment() {

  companion object {
    /**
     * Creates a new instance of a DialogFragment to display content
     * @param subtopicId Used in TopicController to get correct review card data.
     * @return [ReviewCardFragment]: DialogFragment
     */
    fun newInstance(topicName: String, subtopicId: String): ReviewCardFragment {
      val reviewCardFrag = ReviewCardFragment()
      val args = Bundle()
      args.putString(KEY_SUBTOPIC_ID, subtopicId)
      args.putString(KEY_TOPIC_ID, topicName)
      reviewCardFrag.arguments = args
      return reviewCardFrag
    }
  }

  @Inject lateinit var reviewCardFragmentPresenter: ReviewCardFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    val args = checkNotNull(arguments) { "Expected arguments to be passed to ReviewCardFragment" }
    val subtopicId = checkNotNull(args.getString(KEY_SUBTOPIC_ID)) { "Expected subtopicId to be passed to ReviewCardFragment" }
    val topicId = checkNotNull(args.getString(KEY_TOPIC_ID)) { "Expected topicId to be passed to ReviewCardFragment" }
    return reviewCardFragmentPresenter.handleCreateView(inflater, container,topicId, subtopicId)
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.FullScreenDialogStyle)
  }
}
