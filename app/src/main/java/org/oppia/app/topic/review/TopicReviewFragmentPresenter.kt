package org.oppia.app.topic.review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.app.databinding.TopicReviewFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.SkillSummary
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.topic.RouteToReviewListener
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [TopicReviewFragment]. */
@FragmentScope
class TopicReviewFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val viewModelProvider: ViewModelProvider<TopicReviewViewModel>
) {
  private val routeToReviewListener = activity as RouteToReviewListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TopicReviewFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.topicSkillRecyclerView.apply {
      //adapter = createRecyclerViewAdapter()
      layoutManager = GridLayoutManager(context, /* spanCount= */ 2)
    }
    binding.let {
      it.viewModel = getTopicReviewViewModel()
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  fun onTopicSkillSummaryClicked(skillSummary: SkillSummary) {
    routeToReviewListener.routeToReview(skillSummary.skillId)
  }

//  private fun createRecyclerViewAdapter(): BindableAdapter<TopicReviewSummaryViewModel> {
//    return BindableAdapter.Builder
//      .newBuilder<TopicReviewSummaryViewModel>()
//      .registerViewDataBinder(
//        inflateDataBinding = TopicReviewSummaryViewModel::inflate,
//        setViewModel = TopicReviewSummaryViewModel::setViewModel)
//      .build()
//  }

  private fun getTopicReviewViewModel(): TopicReviewViewModel {
    return viewModelProvider.getForFragment(fragment, TopicReviewViewModel::class.java)
  }
}
