package org.oppia.android.app.devoptions.markchapterscompleted

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.ModifyLessonProgressController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/**
 * [ViewModel] for [MarkChaptersCompletedFragment]. It populates the recyclerview with a list of
 * [StorySummaryViewModel] which in turn display the story.
 */
@FragmentScope
class MarkChaptersCompletedViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val modifyLessonProgressController: ModifyLessonProgressController
) : ObservableViewModel() {

  private var internalProfileId: Int = -1

  /**
   * List of [MarkChaptersCompletedItemViewModel] used in [MarkChaptersCompletedFragmentPresenter]
   * to implement the selection logic of chapters.
   */
  val itemList = mutableListOf<MarkChaptersCompletedItemViewModel>()

  /**
   * List of [MarkChaptersCompletedItemViewModel] used to populate recyclerview of
   * [MarkChaptersCompletedFragment] to display stories and chapters.
   */
  val storySummaryLiveData: LiveData<List<MarkChaptersCompletedItemViewModel>> by lazy {
    Transformations.map(storyMapLiveData, ::processStoryMap)
  }

  private val storyMapLiveData: LiveData<Map<String, List<StorySummary>>> by lazy { getStoryMap() }

  private fun getStoryMap(): LiveData<Map<String, List<StorySummary>>> {
    return Transformations.map(storyMapResultLiveData, ::processStoryMapResult)
  }

  private val storyMapResultLiveData:
    LiveData<AsyncResult<Map<String, List<StorySummary>>>> by lazy {
      modifyLessonProgressController
        .getStoryMapWithProgress(ProfileId.newBuilder().setInternalId(internalProfileId).build())
        .toLiveData()
    }

  private fun processStoryMapResult(
    storyMap: AsyncResult<Map<String, List<StorySummary>>>
  ): Map<String, List<StorySummary>> {
    if (storyMap.isFailure()) {
      oppiaLogger.e(
        "MarkChaptersCompletedFragment",
        "Failed to retrieve storyList",
        storyMap.getErrorOrNull()!!
      )
    }
    return storyMap.getOrDefault(mapOf())
  }

  private fun processStoryMap(
    storyMap: Map<String, List<StorySummary>>
  ): List<MarkChaptersCompletedItemViewModel> {
    itemList.clear()
    var nextStoryIndex = 0
    var chapterIndex = 0
    storyMap.forEach { storyMapItem ->
      storyMapItem.value.forEach { storySummary ->
        itemList.add(StorySummaryViewModel(storyName = storySummary.storyName))
        chapterIndex++
        nextStoryIndex = chapterIndex + storySummary.chapterCount
        storySummary.chapterList.forEach { chapterSummary ->
          itemList.add(
            ChapterSummaryViewModel(
              chapterIndex = chapterIndex,
              chapterSummary = chapterSummary,
              nextStoryIndex = nextStoryIndex,
              storyId = storySummary.storyId,
              topicId = storyMapItem.key
            )
          )
          chapterIndex++
        }
      }
    }
    return itemList
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }
}
