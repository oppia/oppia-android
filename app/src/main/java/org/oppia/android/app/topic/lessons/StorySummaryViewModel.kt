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
  private val translationController: TranslationController
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

  fun computeStoryNameChapterCountContainerContentDescription(): String {
    // TODO(#3844): Combine these strings together.
    val chapterCountText =
      resourceHandler.getQuantityStringInLocaleWithWrapping(
        R.plurals.chapter_count, storySummary.chapterCount, storySummary.chapterCount.toString()
      )
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.chapter_count_with_story_name, chapterCountText, storyTitle
    )
  }

  fun computeChapterCountText(): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.chapter_count, storySummary.chapterCount, storySummary.chapterCount.toString()
    )
  }

  private fun computeStoryProgressPercentageText(storyPercentage: Int): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.topic_story_progress_percentage, storyPercentage.toString()
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
        storyId = storySummary.storyId,
        index = index,
        chapterSummarySelector = chapterSummarySelector,
        resourceHandler = resourceHandler
      )
    }
  }
}
