package org.oppia.android.app.devoptions.markstoriescompleted

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
 * [ViewModel] for [MarkStoriesCompletedFragment]. It populates the recyclerview with a list of
 * [StorySummaryViewModel] which in turn display the story.
 */
@FragmentScope
class MarkStoriesCompletedViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val modifyLessonProgressController: ModifyLessonProgressController,
  private val translationController: TranslationController
) : ObservableViewModel() {

  private lateinit var profileId: ProfileId

  private val itemList = mutableMapOf<String, StorySummaryViewModel>()

  /**
   * List of [StorySummaryViewModel] used to populate recyclerview of [MarkStoriesCompletedFragment]
   * to display stories.
   */
  val storySummaryLiveData: LiveData<List<StorySummaryViewModel>> by lazy {
    Transformations.map(storyMapLiveData, ::processStoryMap)
  }

  private val storyMapLiveData: LiveData<Map<String, List<EphemeralStorySummary>>> by lazy {
    getStoryMap()
  }

  private val storyMapResultLiveData:
    LiveData<AsyncResult<Map<String, List<EphemeralStorySummary>>>> by lazy {
      modifyLessonProgressController.getStoryMapWithProgress(profileId).toLiveData()
    }

  private fun getStoryMap(): LiveData<Map<String, List<EphemeralStorySummary>>> {
    return Transformations.map(storyMapResultLiveData, ::processStoryMapResult)
  }

  private fun processStoryMapResult(
    storyMap: AsyncResult<Map<String, List<EphemeralStorySummary>>>
  ): Map<String, List<EphemeralStorySummary>> {
    return when (storyMap) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "MarkStoriesCompletedFragment", "Failed to retrieve storyList", storyMap.error
        )
        mapOf()
      }
      is AsyncResult.Pending -> mapOf()
      is AsyncResult.Success -> storyMap.value
    }
  }

  private fun processStoryMap(
    storyMap: Map<String, List<EphemeralStorySummary>>
  ): List<StorySummaryViewModel> {
    itemList.clear()
    storyMap.forEach {
      it.value.forEach { ephemeralStorySummary ->
        val isCompleted =
          modifyLessonProgressController.checkIfStoryIsCompleted(ephemeralStorySummary)
        itemList[ephemeralStorySummary.storySummary.storyId] =
          StorySummaryViewModel(
            ephemeralStorySummary, isCompleted, topicId = it.key, translationController
          )
      }
    }
    return itemList.values.toList()
  }

  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  /**
   * Returns a list of [StorySummaryViewModel]s mapped to corresponding story IDs whose progress
   * can be modified.
   */
  fun getStorySummaryMap(): Map<String, StorySummaryViewModel> = itemList.toMap()
}
