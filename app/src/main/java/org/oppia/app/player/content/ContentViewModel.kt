package org.oppia.app.player.content

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for content-card state. */
@FragmentScope
class ContentViewModel @Inject constructor() : ViewModel() {

  val contentId = ObservableField<String>("content_id")
  val htmlContent = ObservableField<String>("html")

  fun setContentId(content_id: String) {
    contentId.set(content_id)
  }

  fun setHtmlContent(html: String) {
    htmlContent.set(html)
  }
}
