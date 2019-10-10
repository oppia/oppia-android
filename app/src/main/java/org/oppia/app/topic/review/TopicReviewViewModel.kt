package org.oppia.app.topic.review

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.domain.topic.TopicController
import javax.inject.Inject

/**
 * [ViewModel] for showing a list of topic summaries. Note that this can only be hosted in fragments that implement
 * [ReviewSkillSelector].
 */
@FragmentScope
class TopicReviewViewModel @Inject constructor(fragment: Fragment) : ViewModel() {}
