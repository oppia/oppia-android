package org.oppia.android.app.topic.lessons

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.model.EphemeralStorySummary
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.translation.TranslationController

private const val DEFAULT_STORY_PERCENTAGE = 0

/** [ViewModel] for displaying a story summary. */
class StorySummaryViewModel(
  private val ephemeralStorySummary: EphemeralStorySummary,
  private val storySummarySelector: StorySummarySelector,
  private val chapterSummarySelector: ChapterSummarySelector,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  private val storyIndex: Int
) : TopicLessonsItemViewModel() {
  val storySummary = ephemeralStorySummary.storySummary
  val storyTitle by lazy {
    translationController.extractString(
      storySummary.storyTitle, ephemeralStorySummary.writtenTranslationContext
    )
  }
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

  fun computeChapterCountText(): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.chapter_count, storySummary.chapterCount, storySummary.chapterCount.toString()
    )
  }

  /*
  * Returns content description of progress container based on story percentage.
  *
  * @return a [String] representing content description for progress container
  */
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
      R.string.story_summary_activity_topic_story_progress_percentage, storyPercentage.toString()
    )
  }

  private fun computeChapterSummaryItemList(): List<ChapterSummaryViewModel> {
    return ephemeralStorySummary.chaptersList.mapIndexed { index, ephemeralChapterSummary ->
      ChapterSummaryViewModel(
        chapterPlayState = ephemeralChapterSummary.chapterSummary.chapterPlayState,
        explorationId = ephemeralChapterSummary.chapterSummary.explorationId,
        chapterTitle = translationController.extractString(
          ephemeralChapterSummary.chapterSummary.title,
          ephemeralChapterSummary.writtenTranslationContext
        ),
        previousChapterTitle = if (index > 0) {
          translationController.extractString(
            ephemeralStorySummary.chaptersList[index - 1].chapterSummary.title,
            ephemeralStorySummary.chaptersList[index - 1].writtenTranslationContext
          )
        } else null,
        storyId = storySummary.storyId,
        index = index,
        chapterSummarySelector = chapterSummarySelector,
        resourceHandler = resourceHandler,
        storyIndex = storyIndex
      )
    }
  }
}
