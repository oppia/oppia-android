package org.oppia.android.app.devoptions.markchapterscompleted

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.MarkChaptersCompletedChapterSummaryViewBinding
import org.oppia.android.databinding.MarkChaptersCompletedFragmentBinding
import org.oppia.android.databinding.MarkChaptersCompletedStorySummaryViewBinding
import org.oppia.android.domain.devoptions.ModifyLessonProgressController
import javax.inject.Inject

/** The presenter for [MarkChaptersCompletedFragment]. */
@FragmentScope
class MarkChaptersCompletedFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<MarkChaptersCompletedViewModel>,
  private val modifyLessonProgressController: ModifyLessonProgressController
) : ChapterSelector {
  private lateinit var binding: MarkChaptersCompletedFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<MarkChaptersCompletedItemViewModel>
  lateinit var selectedExplorationIdList: ArrayList<String>
  private lateinit var profileId: ProfileId

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    selectedExplorationIdList: ArrayList<String>
  ): View? {
    binding = MarkChaptersCompletedFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.markChaptersCompletedToolbar.setNavigationOnClickListener {
      (activity as MarkChaptersCompletedActivity).finish()
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = getMarkChaptersCompletedViewModel()
    }

    this.selectedExplorationIdList = selectedExplorationIdList

    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    getMarkChaptersCompletedViewModel().setProfileId(profileId)

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.markChaptersCompletedRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    binding.markChaptersCompletedAllCheckBoxContainer.setOnClickListener {
      if (binding.isAllChecked == null || binding.isAllChecked == false) {
        binding.isAllChecked = true
        getMarkChaptersCompletedViewModel().getItemList().forEach { viewModel ->
          if (viewModel is ChapterSummaryViewModel) {
            if (!viewModel.checkIfChapterIsCompleted())
              chapterSelected(
                viewModel.chapterIndex,
                viewModel.nextStoryIndex,
                viewModel.chapterSummary.explorationId
              )
          }
        }
      }
    }

    binding.markChaptersCompletedMarkCompletedTextView.setOnClickListener {
      modifyLessonProgressController.markMultipleChaptersCompleted(
        profileId = profileId,
        chapterMap = getMarkChaptersCompletedViewModel().getChapterMap()
          .filterKeys { selectedExplorationIdList.contains(it) }
      )
      activity.finish()
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<MarkChaptersCompletedItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<MarkChaptersCompletedItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is StorySummaryViewModel -> ViewType.VIEW_TYPE_STORY
          is ChapterSummaryViewModel -> ViewType.VIEW_TYPE_CHAPTER
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_STORY,
        inflateDataBinding = MarkChaptersCompletedStorySummaryViewBinding::inflate,
        setViewModel = MarkChaptersCompletedStorySummaryViewBinding::setViewModel,
        transformViewModel = { it as StorySummaryViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_CHAPTER,
        inflateDataBinding = MarkChaptersCompletedChapterSummaryViewBinding::inflate,
        setViewModel = this::bindChapterSummaryView,
        transformViewModel = { it as ChapterSummaryViewModel }
      )
      .build()
  }

  private fun bindChapterSummaryView(
    binding: MarkChaptersCompletedChapterSummaryViewBinding,
    model: ChapterSummaryViewModel
  ) {
    binding.viewModel = model
    val notCompletedChapterCount = getMarkChaptersCompletedViewModel().getItemList().count {
      it is ChapterSummaryViewModel && !it.checkIfChapterIsCompleted()
    }
    if (notCompletedChapterCount == 0) {
      this.binding.isAllChecked = true
    }
    if (model.checkIfChapterIsCompleted()) {
      binding.isChapterChecked = true
      binding.isChapterCheckboxEnabled = false
    } else {
      binding.isChapterChecked =
        selectedExplorationIdList.contains(model.chapterSummary.explorationId)

      binding.isChapterCheckboxEnabled = !model.chapterSummary.hasMissingPrerequisiteChapter() ||
        model.chapterSummary.hasMissingPrerequisiteChapter() &&
        selectedExplorationIdList.contains(
          model.chapterSummary.missingPrerequisiteChapter.explorationId
        )

      binding.markChaptersCompletedChapterCheckBox.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
          chapterSelected(
            model.chapterIndex,
            model.nextStoryIndex,
            model.chapterSummary.explorationId
          )
        } else {
          chapterUnselected(model.chapterIndex, model.nextStoryIndex)
        }
      }
    }
  }

  private fun getMarkChaptersCompletedViewModel(): MarkChaptersCompletedViewModel {
    return viewModelProvider.getForFragment(fragment, MarkChaptersCompletedViewModel::class.java)
  }

  override fun chapterSelected(chapterIndex: Int, nextStoryIndex: Int, explorationId: String) {
    if (!selectedExplorationIdList.contains(explorationId)) {
      selectedExplorationIdList.add(explorationId)
    }
    if (selectedExplorationIdList.size ==
      getMarkChaptersCompletedViewModel().getItemList().count {
        it is ChapterSummaryViewModel && !it.checkIfChapterIsCompleted()
      }
    ) {
      binding.isAllChecked = true
    }
    if (!binding.markChaptersCompletedRecyclerView.isComputingLayout &&
      binding.markChaptersCompletedRecyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE
    ) {
      bindingAdapter.notifyItemChanged(chapterIndex)
      if (chapterIndex + 1 < nextStoryIndex)
        bindingAdapter.notifyItemChanged(chapterIndex + 1)
    }
  }

  override fun chapterUnselected(chapterIndex: Int, nextStoryIndex: Int) {
    for (index in chapterIndex until nextStoryIndex) {
      val explorationId =
        (getMarkChaptersCompletedViewModel().getItemList()[index] as ChapterSummaryViewModel)
          .chapterSummary.explorationId
      if (selectedExplorationIdList.contains(explorationId)) {
        selectedExplorationIdList.remove(explorationId)
      }
    }
    if (selectedExplorationIdList.size !=
      getMarkChaptersCompletedViewModel().getItemList().count {
        it is ChapterSummaryViewModel && !it.checkIfChapterIsCompleted()
      }
    ) {
      binding.isAllChecked = false
    }
    if (!binding.markChaptersCompletedRecyclerView.isComputingLayout &&
      binding.markChaptersCompletedRecyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE
    ) {
      bindingAdapter.notifyItemRangeChanged(
        chapterIndex,
        /*itemCount = */ nextStoryIndex - chapterIndex
      )
    }
  }

  private enum class ViewType {
    VIEW_TYPE_STORY,
    VIEW_TYPE_CHAPTER
  }
}
