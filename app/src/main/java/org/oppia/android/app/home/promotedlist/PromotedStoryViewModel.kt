package org.oppia.android.app.home.promotedlist

import android.content.res.Configuration
import android.content.res.Resources
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.translation.TranslationController
import java.util.Objects

// TODO(#283): Add download status information to promoted-story-card.

/** [ViewModel] for displaying a promoted story. */
class PromotedStoryViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  private val totalStoryCount: Int,
  val entityType: String,
  val promotedStory: PromotedStory,
  translationController: TranslationController,
  val index: Int
) : ObservableViewModel() {
  val storyTitle by lazy {
    translationController.extractString(
      promotedStory.storyTitle, promotedStory.storyWrittenTranslationContext
    )
  }
  val topicTitle by lazy {
    translationController.extractString(
      promotedStory.topicTitle, promotedStory.topicWrittenTranslationContext
    )
  }
  val nextChapterTitle by lazy {
    translationController.extractString(
      promotedStory.nextChapterTitle, promotedStory.nextChapterWrittenTranslationContext
    )
  }
  val classroomTitle by lazy {
    translationController.extractString(
      promotedStory.classroomTitle, promotedStory.topicWrittenTranslationContext
    )
  }

  val thumbnailResourceId: Int by lazy {
    when (promotedStory.lessonThumbnail.thumbnailGraphic) {
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

  private val routeToTopicPlayStoryListener = activity as RouteToTopicPlayStoryListener

  /**
   * Returns an [Int] for the width of the card layout of this promoted story, based on the device's orientation
   * and the number of promoted stories displayed the home activity.
   */
  fun computeLayoutWidth(): Int {
    val orientation = Resources.getSystem().configuration.orientation
    return if (orientation != Configuration.ORIENTATION_PORTRAIT && totalStoryCount > 1) {
      activity.resources.getDimensionPixelSize(R.dimen.promoted_story_card_width)
    } else {
      ViewGroup.LayoutParams.MATCH_PARENT
    }
  }

  fun clickOnStoryTile() {
    routeToTopicPlayStoryListener.routeToTopicPlayStory(
      internalProfileId,
      promotedStory.topicId,
      promotedStory.storyId
    )
  }

  // Overriding equals is needed so that DataProvider combine functions used in the HomeViewModel
  // will only rebind when the actual data in the data list changes, rather than when the ViewModel
  // object changes.
  override fun equals(other: Any?): Boolean {
    return other is PromotedStoryViewModel &&
      other.internalProfileId == this.internalProfileId &&
      other.totalStoryCount == this.totalStoryCount &&
      other.entityType == this.entityType &&
      other.promotedStory == this.promotedStory
  }

  override fun hashCode() = Objects.hash(
    internalProfileId,
    totalStoryCount,
    entityType,
    promotedStory
  )
}
