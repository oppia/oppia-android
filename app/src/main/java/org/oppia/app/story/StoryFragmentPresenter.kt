package org.oppia.app.story

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.StoryChapterViewBinding
import org.oppia.app.databinding.StoryFragmentBinding
import org.oppia.app.databinding.StoryHeaderViewBinding
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.model.EventLog
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.story.storyitemviewmodel.StoryChapterSummaryViewModel
import org.oppia.app.story.storyitemviewmodel.StoryHeaderViewModel
import org.oppia.app.story.storyitemviewmodel.StoryItemViewModel
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.analytics.AnalyticsController
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** The presenter for [StoryFragment]. */
class StoryFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val analyticsController: AnalyticsController,
  private val oppiaClock: OppiaClock,
  private val viewModelProvider: ViewModelProvider<StoryViewModel>
) {
  private val routeToExplorationListener = activity as RouteToExplorationListener

  private lateinit var binding: StoryFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var linearSmoothScroller: RecyclerView.SmoothScroller

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): View? {
    val viewModel = getStoryViewModel()
    binding = StoryFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    viewModel.setInternalProfileId(internalProfileId)
    viewModel.setTopicId(topicId)
    viewModel.setStoryId(storyId)
    logStoryAcivityEvent(topicId, storyId)

    binding.storyToolbar.setNavigationOnClickListener {
      (activity as StoryActivity).finish()
    }

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)
    linearSmoothScroller = createSmoothScroller()

    binding.storyChapterList.apply {
      layoutManager = linearLayoutManager
      adapter = createRecyclerViewAdapter()
    }

    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  fun handleSelectExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  ) {
    routeToExplorationListener.routeToExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen
    )
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StoryItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<StoryItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is StoryHeaderViewModel -> ViewType.VIEW_TYPE_HEADER
          is StoryChapterSummaryViewModel -> ViewType.VIEW_TYPE_CHAPTER
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_HEADER,
        inflateDataBinding = StoryHeaderViewBinding::inflate,
        setViewModel = StoryHeaderViewBinding::setViewModel,
        transformViewModel = { it as StoryHeaderViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_CHAPTER,
        inflateDataBinding = StoryChapterViewBinding::inflate,
        setViewModel = StoryChapterViewBinding::setViewModel,
        transformViewModel = { it as StoryChapterSummaryViewModel }
      )
      .build()
  }

  private fun getStoryViewModel(): StoryViewModel {
    return viewModelProvider.getForFragment(fragment, StoryViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_CHAPTER
  }

  fun smoothScrollToPosition(position: Int) {
    linearSmoothScroller.targetPosition = position
    linearLayoutManager.startSmoothScroll(linearSmoothScroller)
    binding.storyChapterList.layoutManager = linearLayoutManager
  }

  private fun createSmoothScroller(): RecyclerView.SmoothScroller {
    val milliSecondsPerInch = 100f

    return object : LinearSmoothScroller(activity) {
      override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
      }

      override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        return milliSecondsPerInch / displayMetrics!!.densityDpi
      }

      override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
        return super.calculateDyToMakeVisible(view, snapPreference) + dipToPixels(48)
      }
    }
  }

  private fun dipToPixels(dipValue: Int): Int {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dipValue.toFloat(),
      Resources.getSystem().displayMetrics
    ).toInt()
  }

  private fun logStoryAcivityEvent(topicId: String, storyId: String){
    analyticsController.logTransitionEvent(
      fragment.requireActivity().applicationContext,
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_PRACTICE_TAB,
      analyticsController.createStoryContext(topicId, storyId)
    )
  }
}
