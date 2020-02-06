package org.oppia.app.topic.practice.practiceitemviewmodel

import org.oppia.app.topic.RouteToQuestionPlayerListener
import org.oppia.app.topic.practice.TopicPracticeFragment

/** Footer view model for the recycler view in [TopicPracticeFragment]. */
class TopicPracticeFooterViewModel(private val routeToQuestionPlayerListener: RouteToQuestionPlayerListener) :
  TopicPracticeItemViewModel() {

  fun onStartButtonClicked() {
    routeToQuestionPlayerListener.routeToQuestionPlayer(ArrayList())
  }

}
