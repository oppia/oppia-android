package org.oppia.android.app.topic.lessons

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.translation.AppLanguageResourceHandler

private const val DEFAULT_STORY_PERCENTAGE = 0

/** [ViewModel] for displaying a story summary. */
class StorySummaryViewModel(
  val storySummary: StorySummary,
  private val storySummarySelector: StorySummarySelector,
  private val chapterSummarySelector: ChapterSummarySelector,
  private val resourceHandler: AppLanguageResourceHandler
) : TopicLessonsItemViewModel() {
  val storyPercentage: ObservableField<Int> = ObservableField(DEFAULT_STORY_PERCENTAGE)
  val storyProgressPercentageText: ObservableField<String> =
    ObservableField(computeStoryProgressPercentageText(DEFAULT_STORY_PERCENTAGE))

  val chapterSummaryItemList: List<ChapterSummaryViewModel> by lazy {
    computeChapterSummaryItemList()
  }

  fun clickOnStorySummaryTitle() {
    storySummarySelector.selectStorySummary(storySummary)
  }

  fun setStoryPercentage(storyPercentage: Int) {
    this.storyPercentage.set(storyPercentage)
    storyProgressPercentageText.set(computeStoryProgressPercentageText(storyPercentage))
  }

  fun computeStoryNameChapterCountContainerContentDescription(): String {
    // TODO(#3844): Combine these strings together.
    val chapterCountText =
      resourceHandler.getQuantityStringInLocaleWithWrapping(
        R.plurals.chapter_count, storySummary.chapterCount, storySummary.chapterCount.toString()
      )
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.chapter_count_with_story_name, chapterCountText, storySummary.storyName
    )
  }

  fun computeChapterCountText(): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.chapter_count, storySummary.chapterCount, storySummary.chapterCount.toString()
    )
  }

  fun computeProgressContainerContentDescription(): String {
    return if (storyPercentage.get()!! < 100) {
      "${storyProgressPercentageText.get()} " +
        resourceHandler.getStringInLocale(R.string.status_in_progress)
    } else {
      "${storyProgressPercentageText.get()} " +
        resourceHandler.getStringInLocale(R.string.status_completed)
    }
  }

  private fun computeStoryProgressPercentageText(storyPercentage: Int): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.topic_story_progress_percentage, storyPercentage.toString()
    )
  }

  private fun computeChapterSummaryItemList(): List<ChapterSummaryViewModel> {
    return storySummary.chapterList.mapIndexed { index, chapterSummary ->
      ChapterSummaryViewModel(
        chapterPlayState = chapterSummary.chapterPlayState,
        explorationId = chapterSummary.explorationId,
        chapterName = chapterSummary.name,
        storyId = storySummary.storyId,
        index = index,
        chapterSummarySelector = chapterSummarySelector,
        resourceHandler = resourceHandler
      )
    }
  }
}
