package org.oppia.app.topic.questionplayer

interface QuestionNavigationButtonListener {
  fun onSubmitButtonClicked()
  fun onContinueButtonClicked()
  fun onReturnToTopicButtonClicked()
  fun onReplayButtonClicked()
}