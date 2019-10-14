package org.oppia.app.player.content

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.SubtitledHtml
import javax.inject.Inject

/** [ViewModel] for content-card state. */
@FragmentScope
class ContentViewModel @Inject constructor(
) : ViewModel() {
  val explorationId = ObservableField<String>("exploration_id")

  fun getExplorationId(): String?{
    return explorationId.get()
  }

  val contentId = ObservableField<String>("content_id")

  fun setContentId(content_id: String) {
    contentId.set(content_id)
  }
  val html_content = ObservableField<String>("html")

  fun setHtmlContent(html: String) {
    html_content.set(html)
  }
}
