package org.oppia.android.app.topic.info

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.Topic
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** [ViewModel] for showing topic info details. */
@FragmentScope
class TopicInfoViewModel @Inject constructor(
  @TopicHtmlParserEntityType val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) : ObservableViewModel() {

  val topic = ObservableField(DEFAULT_TOPIC)
  val storyCountText: ObservableField<String> =
    ObservableField(computeStoryCountText(DEFAULT_TOPIC))
  val topicSizeText: ObservableField<String> = ObservableField("")
  val topicTitle = ObservableField<CharSequence>(DEFAULT_TOPIC.title.html)
  val topicDescription = ObservableField<CharSequence>(DEFAULT_TOPIC.description.html)
  var downloadStatusIndicatorDrawableResourceId =
    ObservableField(R.drawable.ic_available_offline_primary_24dp)
  val isDescriptionExpanded = ObservableField(true)
  val isSeeMoreVisible = ObservableField(true)

  fun setTopic(ephemeralTopic: EphemeralTopic) {
    this.topic.set(ephemeralTopic.topic)
    topicTitle.set(
      translationController.extractString(
        ephemeralTopic.topic.title, ephemeralTopic.writtenTranslationContext
      )
    )
    topicDescription.set(
      translationController.extractString(
        ephemeralTopic.topic.description, ephemeralTopic.writtenTranslationContext
      )
    )
    storyCountText.set(computeStoryCountText(ephemeralTopic.topic))
  }

  fun calculateTopicSizeWithUnit() {
    val sizeWithUnit = topic.get()?.let { topic ->
      val sizeInBytes: Int = topic.diskSizeBytes.toInt()
      val sizeInKb = sizeInBytes / 1024
      val sizeInMb = sizeInKb / 1024
      val sizeInGb = sizeInMb / 1024
      return@let when {
        sizeInGb >= 1 -> {
          resourceHandler.getStringInLocaleWithWrapping(
            R.string.size_gb, roundUpToHundredsString(sizeInGb)
          )
        }
        sizeInMb >= 1 -> {
          resourceHandler.getStringInLocaleWithWrapping(
            R.string.size_mb, roundUpToHundredsString(sizeInMb)
          )
        }
        sizeInKb >= 1 -> {
          resourceHandler.getStringInLocaleWithWrapping(
            R.string.size_kb, roundUpToHundredsString(sizeInKb)
          )
        }
        else -> {
          resourceHandler.getStringInLocaleWithWrapping(
            R.string.size_bytes, roundUpToHundredsString(sizeInBytes)
          )
        }
      }
    } ?: resourceHandler.getStringInLocale(R.string.topic_info_activity_unknown_size)
    topicSizeText.set(
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.topic_info_activity_topic_download_text, sizeWithUnit
      )
    )
  }

  private fun computeStoryCountText(topic: Topic): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.story_count, topic.storyCount, topic.storyCount.toString()
    )
  }

  private fun roundUpToHundredsString(intValue: Int): String =
    (((intValue + 9) / 10) * 10).toString()

  fun clickSeeMore() {
    isDescriptionExpanded.set(!isDescriptionExpanded.get()!!)
  }

  private companion object {
    private val DEFAULT_TOPIC = Topic.getDefaultInstance()
  }
}
