package org.oppia.android.app.home.recentlyplayed

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.translation.TranslationController

// TODO(#297): Add download status information to promoted-story-card.

/** [ViewModel] for displaying a promoted story. */
class PromotedStoryViewModel(
  private val activity: AppCompatActivity,
  val promotedStory: PromotedStory,
  val entityType: String,
  private val promotedStoryClickListener: PromotedStoryClickListener,
  private val position: Int,
  private val resourceHandler: AppLanguageResourceHandler,
  translationController: TranslationController
) : RecentlyPlayedItemViewModel() {
  /** Sets the story title of the recently played story. */
  val storyTitle by lazy {
    translationController.extractString(
      promotedStory.storyTitle, promotedStory.storyWrittenTranslationContext
    )
  }
  /** Sets the topic of the recently played story. */
  val topicTitle by lazy {
    translationController.extractString(
      promotedStory.topicTitle, promotedStory.topicWrittenTranslationContext
    )
  }
  /** Sets the next chapter title of the recently played story. */
  val nextChapterTitle by lazy {
    translationController.extractString(
      promotedStory.nextChapterTitle, promotedStory.nextChapterWrittenTranslationContext
    )
  }
  /** Sets the classroom of the recently played story. */
  val classroomTitle by lazy {
    translationController.extractString(
      promotedStory.classroomTitle, promotedStory.classroomWrittenTranslationContext
    )
  }

  /**
   * Starts [ResumeLessonActivity] if a saved exploration is selected or [ExplorationActivity] if an
   * un-started recommended story is selected.
   */
  fun clickOnPromotedStoryTile(@Suppress("UNUSED_PARAMETER") v: View) {
    promotedStoryClickListener.promotedStoryClicked(promotedStory)
  }

  private val outerMargin by lazy {
    activity.resources.getDimensionPixelSize(R.dimen.recently_played_margin_max)
  }
  private val innerMargin by lazy {
    activity.resources.getDimensionPixelSize(R.dimen.recently_played_margin_min)
  }
  private val spanCount by lazy {
    activity.resources.getInteger(R.integer.recently_played_span_count)
  }

  /**
   * Determines the start margin for an individual TopicSummary relative to the grid columns laid out on the
   * HomeActivity. GridLayout columns are evenly spread out across the entire activity screen but the
   * Topic Summaries are positioned towards the center, so start margins are calculated to stagger inside each
   * fixed column but centered on the activity's layout, as shown below.
   *
   *  |        _____|      _____   |   _____      |_____        |
   *  |       |     |     |     |  |  |     |     |     |       |
   *  |       |     |     |     |  |  |     |     |     |       |
   *  |       |_____|     |_____|  |  |_____|     |_____|       |
   *  |             |              |              |             |
   *  |        _____       _____       _____       _____        |
   *  |       |     |     |     |     |     |     |     |       |
   *  |       |     |     |     |     |     |     |     |       |
   *  |       |_____|     |_____|     |_____|     |_____|       |
   *  |                                                         |
   */
  fun computeStartMargin(): Int {
    return when (spanCount) {
      2 -> when (position % spanCount) {
        0 -> outerMargin
        else -> innerMargin
      }
      3 -> when (position % spanCount) {
        0 -> outerMargin
        1 -> innerMargin
        2 -> 0
        else -> 0
      }
      4 -> when (position % spanCount) {
        0 -> outerMargin
        1 -> innerMargin
        2 -> innerMargin / 2
        3 -> 0
        else -> 0
      }
      else -> 0
    }
  }

  /**
   * Determines the end margin for an individual TopicSummary relative to the grid columns laid out on the
   * HomeActivity. The end margins are calculated to stagger inside each fixed column but centered on the
   * activity's layout (see [computeStartMargin]).
   */
  fun computeEndMargin(): Int {
    return when (spanCount) {
      2 -> when (position % spanCount) {
        0 -> innerMargin
        else -> outerMargin
      }
      3 -> when (position % spanCount) {
        0 -> 0
        1 -> innerMargin
        2 -> outerMargin
        else -> 0
      }
      4 -> when (position % spanCount) {
        0 -> 0
        1 -> innerMargin / 2
        2 -> innerMargin
        3 -> outerMargin
        else -> 0
      }
      else -> 0
    }
  }

  /**
   * Creates the content description of the story thumbnail by interpolating the thumbnail
   * description string resource and the next chapter title.
   */
  fun computeLessonThumbnailContentDescription(): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.lesson_thumbnail_content_description, nextChapterTitle
    )
  }
}
