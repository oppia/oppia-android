package org.oppia.android.app.devoptions.markchapterscompleted

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralStorySummary
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.devoptions.ModifyLessonProgressController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
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
  private val modifyLessonProgressController: ModifyLessonProgressController,
  private val translationController: TranslationController
) : ObservableViewModel() {

  private lateinit var profileId: ProfileId

  private val itemList = mutableListOf<MarkChaptersCompletedItemViewModel>()

  /**
   * List of [MarkChaptersCompletedItemViewModel] used to populate recyclerview of
   * [MarkChaptersCompletedFragment] to display stories and chapters.
   */
  val storySummaryLiveData: LiveData<List<MarkChaptersCompletedItemViewModel>> by lazy {
    Transformations.map(storyMapLiveData, ::processStoryMap)
  }

  private val storyMapLiveData: LiveData<Map<String, List<EphemeralStorySummary>>> by lazy {
    getStoryMap()
  }

  private fun getStoryMap(): LiveData<Map<String, List<EphemeralStorySummary>>> {
    return Transformations.map(storyMapResultLiveData, ::processStoryMapResult)
  }

  private val storyMapResultLiveData:
    LiveData<AsyncResult<Map<String, List<EphemeralStorySummary>>>> by lazy {
      modifyLessonProgressController.getStoryMapWithProgress(profileId).toLiveData()
    }

  private fun processStoryMapResult(
    storyMap: AsyncResult<Map<String, List<EphemeralStorySummary>>>
  ): Map<String, List<EphemeralStorySummary>> {
    return when (storyMap) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "MarkChaptersCompletedFragment", "Failed to retrieve storyList", storyMap.error
        )
        mapOf()
      }
      is AsyncResult.Pending -> mapOf()
      is AsyncResult.Success -> storyMap.value
    }
  }

  private fun processStoryMap(
    storyMap: Map<String, List<EphemeralStorySummary>>
  ): List<MarkChaptersCompletedItemViewModel> {
    itemList.clear()
    var nextStoryIndex: Int
    var chapterIndex = 0
    storyMap.forEach { storyMapItem ->
      storyMapItem.value.forEach { ephemeralStorySummary ->
        val storySummary = ephemeralStorySummary.storySummary
        val storyTitle =
          translationController.extractString(
            storySummary.storyTitle, ephemeralStorySummary.writtenTranslationContext
          )
        itemList.add(StorySummaryViewModel(storyTitle))
        chapterIndex++
        nextStoryIndex = chapterIndex + storySummary.chapterCount
        ephemeralStorySummary.chaptersList.forEach { ephemeralChapterSummary ->
          itemList.add(
            ChapterSummaryViewModel(
              chapterIndex = chapterIndex,
              ephemeralChapterSummary = ephemeralChapterSummary,
              nextStoryIndex = nextStoryIndex,
              storyId = storySummary.storyId,
              topicId = storyMapItem.key,
              translationController
            )
          )
          chapterIndex++
        }
      }
    }
    return itemList
  }

  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  /** Returns a list of [MarkChaptersCompletedItemViewModel]s. */
  fun getItemList(): List<MarkChaptersCompletedItemViewModel> = itemList.toList()

  /** Returns a list of [ChapterSummaryViewModel]s mapped to corresponding exploration IDs. */
  fun getChapterMap(): Map<String, Pair<String, String>> =
    itemList.filterIsInstance<ChapterSummaryViewModel>().associateBy(
      { it.chapterSummary.explorationId },
      { Pair(it.storyId, it.topicId) }
    )
}
