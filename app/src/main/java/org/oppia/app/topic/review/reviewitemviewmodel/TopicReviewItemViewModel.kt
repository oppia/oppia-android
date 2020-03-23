package org.oppia.app.topic.review.reviewitemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.model.Subtopic
import org.oppia.app.topic.review.ReviewSubtopicSelector

class TopicReviewItemViewModel(val subtopic: Subtopic, val onReviewItemPressed: ReviewSubtopicSelector): ViewModel()