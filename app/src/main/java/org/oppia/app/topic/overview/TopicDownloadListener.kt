package org.oppia.app.topic.overview

/** Interface to check the preference regarding alert for [TopicDownloadDialogFragment]. */
interface TopicDownloadListener {
  /**
   * If saveUserChoice is true, show audio-player and save preference do not show dialog again.
   * If saveUserChoice is false, show audio-player and do not save preference and show this dialog next time too.
   */
  fun downloadTopicWhileOnCellular(saveUserChoice: Boolean)

  /**
   * If saveUserChoice is true, do not show audio-player on cellular network and save preference.
   * If saveUserChoice is false, do not show audio-player and do not save preference.
   */
  fun doNotDownloadTopicWhileOnCellular(saveUserChoice: Boolean)
}
