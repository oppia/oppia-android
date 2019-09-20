package org.oppia.app.topic.conceptcard

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class ConceptCardViewModel @Inject constructor() : ViewModel() {
  fun getDummyText() = "hello world"
}