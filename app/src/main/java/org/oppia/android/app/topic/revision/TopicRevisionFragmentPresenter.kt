package org.oppia.android.app.topic.revision

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Subtopic
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.topic.revision.revisionitemviewmodel.TopicRevisionItemViewModel
import org.oppia.android.databinding.TopicRevisionFragmentBinding
import org.oppia.android.databinding.TopicRevisionSummaryViewBinding
import javax.inject.Inject

/** The presenter for [TopicRevisionFragment]. */
@FragmentScope
class TopicRevisionFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModel: TopicRevisionViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) : RevisionSubtopicSelector {
  private lateinit var binding: TopicRevisionFragmentBinding
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private val routeToReviewListener = activity as RouteToRevisionCardListener
  private var subtopicListSize: Int? = null

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId,
    topicId: String
  ): View? {
    this.profileId = profileId
    this.topicId = topicId
    binding = TopicRevisionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    viewModel.setTopicId(this.topicId)
    viewModel.setProfileId(this.profileId)

    binding.revisionRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
      // https://stackoverflow.com/a/50075019/3689782
      val spanCount = fragment.resources.getInteger(R.integer.topic_revision_span_count)
      layoutManager = GridLayoutManager(context, spanCount)
    }
    binding.apply {
      this.viewModel = this@TopicRevisionFragmentPresenter.viewModel
      lifecycleOwner = fragment
    }

    viewModel.subtopicLiveData.observe(fragment) {
      this.subtopicListSize = it.size
    }
    return binding.root
  }

  override fun onTopicRevisionSummaryClicked(subtopic: Subtopic) {
    routeToReviewListener.routeToRevisionCard(
      profileId,
      topicId,
      subtopic.subtopicId,
      checkNotNull(subtopicListSize) { "Subtopic list size not found." }
    )
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<TopicRevisionItemViewModel> {
    return singleTypeBuilderFactory.create<TopicRevisionItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = TopicRevisionSummaryViewBinding::inflate,
        setViewModel = TopicRevisionSummaryViewBinding::setViewModel
      ).build()
  }
}
