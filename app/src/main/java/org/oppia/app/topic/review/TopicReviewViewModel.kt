package org.oppia.app.topic.review

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.domain.topic.TopicController
import javax.inject.Inject

/**
 * [ViewModel] for showing a list of topic review-skills.
 */
@FragmentScope
class TopicReviewViewModel @Inject constructor(fragment: Fragment) : ViewModel() {}
