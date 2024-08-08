package org.oppia.android.app.devoptions.markchapterscompleted

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.translation.AppLanguageResourceHandler
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
  private val viewModel: MarkChaptersCompletedViewModel,
  private val modifyLessonProgressController: ModifyLessonProgressController,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var binding: MarkChaptersCompletedFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<MarkChaptersCompletedItemViewModel>
  private lateinit var profileId: ProfileId
  private lateinit var alertDialog: AlertDialog
  private val selectedExplorationIds = mutableListOf<String>()
  private val selectedExplorationTitles = mutableListOf<String>()

  val serializableSelectedExplorationIds: ArrayList<String>
    get() = ArrayList(selectedExplorationIds)
  val serializableSelectedExplorationTitles: ArrayList<String>
    get() = ArrayList(selectedExplorationTitles)

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    showConfirmationNotice: Boolean,
    selectedExplorationIds: List<String>,
    selectedExplorationTitles: List<String>
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
      this.viewModel = this@MarkChaptersCompletedFragmentPresenter.viewModel
    }

    this.selectedExplorationIds += selectedExplorationIds
    this.selectedExplorationTitles += selectedExplorationTitles

    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    viewModel.setProfileId(profileId)

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.markChaptersCompletedRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    binding.markChaptersCompletedAllCheckBoxContainer.setOnClickListener {
      if (binding.isAllChecked == null || binding.isAllChecked == false) {
        binding.isAllChecked = true
        viewModel.getItemList().forEach { viewModel ->
          if (viewModel is ChapterSummaryViewModel) {
            if (!viewModel.checkIfChapterIsCompleted())
              chapterSelected(
                viewModel.chapterIndex,
                viewModel.nextStoryIndex,
                viewModel.chapterSummary.explorationId,
                viewModel.chapterTitle
              )
          }
        }
      } else if (binding.isAllChecked == true) {
        binding.isAllChecked = false
        viewModel.getItemList().forEach { viewModel ->
          if (viewModel is ChapterSummaryViewModel) {
            if (!viewModel.checkIfChapterIsCompleted()) {
              chapterUnselected(viewModel.chapterIndex, viewModel.nextStoryIndex)
            }
          }
        }
      }
    }

    binding.markChaptersCompletedMarkCompletedTextView.setOnClickListener {
      if (showConfirmationNotice && this.selectedExplorationIds.isNotEmpty()) {
        showConfirmationDialog()
      } else markChaptersAsCompleted()
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<MarkChaptersCompletedItemViewModel> {
    return multiTypeBuilderFactory.create<MarkChaptersCompletedItemViewModel,
      ViewType> { viewModel ->
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
    val notCompletedChapterCount = viewModel.getItemList().count {
      it is ChapterSummaryViewModel && !it.checkIfChapterIsCompleted()
    }
    if (notCompletedChapterCount == 0) {
      this.binding.isAllChecked = true
    }
    if (model.checkIfChapterIsCompleted()) {
      binding.isChapterChecked = true
      binding.isChapterCheckboxEnabled = false
    } else {
      binding.isChapterChecked = model.chapterSummary.explorationId in selectedExplorationIds

      binding.isChapterCheckboxEnabled = !model.chapterSummary.hasMissingPrerequisiteChapter() ||
        model.chapterSummary.hasMissingPrerequisiteChapter() &&
        model.chapterSummary.missingPrerequisiteChapter.explorationId in selectedExplorationIds

      binding.markChaptersCompletedChapterCheckBox.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
          chapterSelected(
            model.chapterIndex,
            model.nextStoryIndex,
            model.chapterSummary.explorationId,
            model.chapterTitle
          )
        } else {
          chapterUnselected(model.chapterIndex, model.nextStoryIndex)
        }
      }
    }
  }

  private fun chapterSelected(chapterIdx: Int, nextStoryIdx: Int, expId: String, expTitle: String) {
    if (expId !in selectedExplorationIds) {
      selectedExplorationIds += expId
      selectedExplorationTitles += expTitle
    }
    if (selectedExplorationIds.size == viewModel.getItemList().countIncompleteChapters()) {
      binding.isAllChecked = true
    }
    if (!binding.markChaptersCompletedRecyclerView.isComputingLayout &&
      binding.markChaptersCompletedRecyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE
    ) {
      bindingAdapter.notifyItemChanged(chapterIdx)
      if (chapterIdx + 1 < nextStoryIdx)
        bindingAdapter.notifyItemChanged(chapterIdx + 1)
    }
  }

  private fun chapterUnselected(chapterIndex: Int, nextStoryIndex: Int) {
    for (index in chapterIndex until nextStoryIndex) {
      val explorationId =
        (viewModel.getItemList()[index] as ChapterSummaryViewModel).chapterSummary.explorationId
      val expIndex = selectedExplorationIds.indexOf(explorationId)
      if (expIndex != -1) {
        selectedExplorationIds.removeAt(expIndex)
        selectedExplorationTitles.removeAt(expIndex)
      }
    }
    if (selectedExplorationIds.size != viewModel.getItemList().countIncompleteChapters()) {
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

  private fun showConfirmationDialog() {
    alertDialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme).apply {
      setTitle(R.string.mark_chapters_completed_confirm_setting_dialog_title)
      setMessage(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.mark_chapters_completed_confirm_setting_dialog_message,
          selectedExplorationTitles.joinToReadableString()
        )
      )
      setNegativeButton(
        R.string.mark_chapters_completed_confirm_setting_dialog_cancel_button_text
      ) { dialog, _ -> dialog.dismiss() }
      setPositiveButton(
        R.string.mark_chapters_completed_confirm_setting_dialog_confirm_button_text
      ) { dialog, _ ->
        dialog.dismiss()
        markChaptersAsCompleted()
      }
    }.create().also {
      it.setCanceledOnTouchOutside(true)
      it.show()
    }
  }

  private fun markChaptersAsCompleted() {
    modifyLessonProgressController.markMultipleChaptersCompleted(
      profileId = profileId,
      chapterMap = viewModel.getChapterMap().filterKeys { it in selectedExplorationIds }
    )
    activity.finish()
  }

  private enum class ViewType {
    VIEW_TYPE_STORY,
    VIEW_TYPE_CHAPTER
  }

  private companion object {
    private fun List<String>.joinToReadableString(): String {
      return when (size) {
        0 -> ""
        1 -> single()
        2 -> "${this[0]} and ${this[1]}"
        else -> "${asSequence().take(size - 1).joinToString()}, and ${last()}"
      }
    }

    private fun List<MarkChaptersCompletedItemViewModel>.countIncompleteChapters() =
      filterIsInstance<ChapterSummaryViewModel>().count { !it.checkIfChapterIsCompleted() }
  }
}
