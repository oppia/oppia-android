package org.oppia.android.app.home.topiclist

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.EphemeralTopicSummary
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.translation.TranslationController
import java.util.Objects

/** The view model corresponding to individual topic summaries in the topic summary RecyclerView. */
class TopicSummaryViewModel(
  private val activity: AppCompatActivity,
  ephemeralTopicSummary: EphemeralTopicSummary,
  val entityType: String,
  private val topicSummaryClickListener: TopicSummaryClickListener,
  private val position: Int,
  private val resourceHandler: AppLanguageResourceHandler,
  translationController: TranslationController
) : HomeItemViewModel() {
  val topicSummary = ephemeralTopicSummary.topicSummary

  val title: String by lazy {
    translationController.extractString(
      topicSummary.title, ephemeralTopicSummary.writtenTranslationContext
    )
  }

  private val outerMargin by lazy {
    activity.resources.getDimensionPixelSize(R.dimen.home_outer_margin)
  }
  private val innerMargin by lazy {
    activity.resources.getDimensionPixelSize(R.dimen.home_inner_margin)
  }
  private val spanCount by lazy {
    activity.resources.getInteger(R.integer.home_span_count)
  }

  val thumbnailResourceId: Int by lazy {
    when (topicSummary.topicThumbnail.thumbnailGraphic) {
      LessonThumbnailGraphic.BAKER ->
        R.drawable.lesson_thumbnail_graphic_baker
      LessonThumbnailGraphic.CHILD_WITH_BOOK ->
        R.drawable.lesson_thumbnail_graphic_child_with_book
      LessonThumbnailGraphic.CHILD_WITH_CUPCAKES ->
        R.drawable.lesson_thumbnail_graphic_child_with_cupcakes
      LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK ->
        R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework
      LessonThumbnailGraphic.DUCK_AND_CHICKEN ->
        R.drawable.lesson_thumbnail_graphic_duck_and_chicken
      LessonThumbnailGraphic.PERSON_WITH_PIE_CHART ->
        R.drawable.lesson_thumbnail_graphic_person_with_pie_chart
      LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION ->
        R.drawable.topic_fractions_01
      LessonThumbnailGraphic.WRITING_FRACTIONS ->
        R.drawable.topic_fractions_02
      LessonThumbnailGraphic.EQUIVALENT_FRACTIONS ->
        R.drawable.topic_fractions_03
      LessonThumbnailGraphic.MIXED_NUMBERS_AND_IMPROPER_FRACTIONS ->
        R.drawable.topic_fractions_04
      LessonThumbnailGraphic.COMPARING_FRACTIONS ->
        R.drawable.topic_fractions_05
      LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS ->
        R.drawable.topic_fractions_06
      LessonThumbnailGraphic.MULTIPLYING_FRACTIONS ->
        R.drawable.topic_fractions_07
      LessonThumbnailGraphic.DIVIDING_FRACTIONS ->
        R.drawable.topic_fractions_08
      LessonThumbnailGraphic.DERIVE_A_RATIO ->
        R.drawable.topic_ratios_01
      LessonThumbnailGraphic.WHAT_IS_A_FRACTION ->
        R.drawable.topic_fractions_01
      LessonThumbnailGraphic.FRACTION_OF_A_GROUP ->
        R.drawable.topic_fractions_02
      LessonThumbnailGraphic.ADDING_FRACTIONS ->
        R.drawable.topic_fractions_03
      LessonThumbnailGraphic.MIXED_NUMBERS ->
        R.drawable.topic_fractions_04
      else ->
        R.drawable.topic_fractions_01
    }
  }

  /** Callback from data-binding for when the summary tile is clicked. */
  fun clickOnSummaryTile() {
    topicSummaryClickListener.onTopicSummaryClicked(topicSummary)
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

  fun computeLessonCountText(): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.lesson_count,
      topicSummary.totalChapterCount,
      topicSummary.totalChapterCount.toString()
    )
  }

  // Overriding equals is needed so that DataProvider combine functions used in the HomeViewModel
  // will only rebind when the actual data in the data list changes, rather than when the ViewModel
  // object changes.
  override fun equals(other: Any?): Boolean {
    return other is TopicSummaryViewModel &&
      other.topicSummary == this.topicSummary &&
      other.entityType == this.entityType &&
      other.position == this.position
  }

  override fun hashCode() = Objects.hash(topicSummary, entityType, position)
}
