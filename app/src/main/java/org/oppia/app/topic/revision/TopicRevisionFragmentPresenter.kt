package org.oppia.app.topic.revision

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.app.databinding.TopicRevisionFragmentBinding
import org.oppia.app.databinding.TopicRevisionSummaryViewBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Subtopic
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.app.topic.RouteToRevisionCardListener
import org.oppia.app.topic.TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.topic.revision.revisionitemviewmodel.TopicRevisionItemViewModel
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [TopicRevisionFragment]. */
@FragmentScope
class TopicRevisionFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicRevisionViewModel>
) : RevisionSubtopicSelector {
  private lateinit var binding: TopicRevisionFragmentBinding
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private val routeToReviewListener = activity as RouteToRevisionCardListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {

    val viewModel = getTopicRevisionViewModel()

    internalProfileId = fragment.arguments?.getInt(PROFILE_ID_ARGUMENT_KEY, -1)!!
    topicId = checkNotNull(fragment.arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicRevisionFragment."
    }
    binding = TopicRevisionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    viewModel.setTopicId(topicId)
    viewModel.setInternalProfileId(internalProfileId)

    binding.revisionRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
      // https://stackoverflow.com/a/50075019/3689782
      val spanCount =
        if (fragment.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2
      layoutManager = GridLayoutManager(context, spanCount)
    }
    binding.apply {
      this.viewModel = viewModel
      lifecycleOwner = fragment
    }
    return binding.root
  }

  override fun onTopicRevisionSummaryClicked(subtopic: Subtopic) {
    routeToReviewListener.routeToRevisionCard(topicId, subtopic.subtopicId)
  }

  private fun getTopicRevisionViewModel(): TopicRevisionViewModel {
    return viewModelProvider.getForFragment(fragment, TopicRevisionViewModel::class.java)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TopicRevisionItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<TopicRevisionItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TopicRevisionSummaryViewBinding::inflate,
        setViewModel = TopicRevisionSummaryViewBinding::setViewModel
      ).build()
  }
}
