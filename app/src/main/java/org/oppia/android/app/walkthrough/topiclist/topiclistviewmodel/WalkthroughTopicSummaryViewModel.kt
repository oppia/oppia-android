package org.oppia.android.app.walkthrough.topiclist.topiclistviewmodel

import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.model.EphemeralTopicSummary
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.walkthrough.topiclist.WalkthroughTopicItemViewModel
import org.oppia.android.domain.translation.TranslationController

/** [ViewModel] corresponding to topic summaries in [WalkthroughTopicListFragment] RecyclerView.. */
class WalkthroughTopicSummaryViewModel(
  val topicEntityType: String,
  ephemeralTopicSummary: EphemeralTopicSummary,
  private val topicSummaryClickListener: TopicSummaryClickListener,
  private val resourceHandler: AppLanguageResourceHandler,
  translationController: TranslationController
) : WalkthroughTopicItemViewModel() {
  val topicSummary = ephemeralTopicSummary.topicSummary

  val name: String by lazy {
    translationController.extractString(
      topicSummary.title, ephemeralTopicSummary.writtenTranslationContext
    )
  }

  @ColorInt
  val backgroundColor: Int = retrieveBackgroundColor()

  /** Callback from data-binding for when the summary tile is clicked. */
  fun clickOnSummaryTile() {
    topicSummaryClickListener.onTopicSummaryClicked(topicSummary)
  }

  fun computeWalkthroughLessonCountText(): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.lesson_count,
      topicSummary.totalChapterCount,
      topicSummary.totalChapterCount.toString()
    )
  }

  @ColorInt
  private fun retrieveBackgroundColor(): Int {
    return (0xff000000L or topicSummary.topicThumbnail.backgroundColorRgb.toLong()).toInt()
  }
}
