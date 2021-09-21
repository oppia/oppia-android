package org.oppia.android.app.topic.info

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Topic
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType

/** [ViewModel] for showing topic info details. */
@FragmentScope
class TopicInfoViewModel @Inject constructor(
  private val context: Context,
  @TopicHtmlParserEntityType val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {

  val topic = ObservableField(DEFAULT_TOPIC)
  val storyCountText: ObservableField<String> =
    ObservableField(computeStoryCountText(DEFAULT_TOPIC))
  val topicSizeText: ObservableField<String> = ObservableField("")
  val topicDescription = ObservableField<CharSequence>("")
  var downloadStatusIndicatorDrawableResourceId =
    ObservableField(R.drawable.ic_available_offline_primary_24dp)
  val isDescriptionExpanded = ObservableField(true)
  val isSeeMoreVisible = ObservableField(true)

  fun setTopic(topic: Topic) {
    this.topic.set(topic)
    storyCountText.set(computeStoryCountText(topic))
  }

  fun calculateTopicSizeWithUnit() {
    // TODO: file an issue to combine these strings into one.
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
    } ?: resourceHandler.getStringInLocale(R.string.unknown_size)
    topicSizeText.set(
      resourceHandler.getStringInLocaleWithWrapping(R.string.topic_download_text, sizeWithUnit)
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
