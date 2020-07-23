package org.oppia.app.topic.revisioncard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.RevisionCardFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.EventLog
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** Presenter for [RevisionCardFragment], sets up bindings from ViewModel. */
@FragmentScope
class RevisionCardFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val analyticsController: AnalyticsController,
  private val oppiaClock: OppiaClock,
  private val viewModelProvider: ViewModelProvider<RevisionCardViewModel>
) {

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    topicId: String,
    subtopicId: Int
  ): View? {
    val binding =
      RevisionCardFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    val viewModel = getReviewCardViewModel()

    viewModel.setSubtopicIdAndBinding(topicId, subtopicId, binding)
    logRevisionCardEvent(topicId, subtopicId)

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getReviewCardViewModel(): RevisionCardViewModel {
    return viewModelProvider.getForFragment(fragment, RevisionCardViewModel::class.java)
  }

  private fun logRevisionCardEvent(topicId: String, subTopicId: Int) {
    analyticsController.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_REVISION_CARD,
      analyticsController.createRevisionCardContext(topicId, subTopicId.toString())
    )
  }
}
