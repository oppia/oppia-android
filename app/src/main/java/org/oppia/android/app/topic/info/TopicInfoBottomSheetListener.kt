package org.oppia.android.app.topic.info

/** Listener to listen on topic download bottom sheet items. */
interface TopicInfoBottomSheetListener {
  /** Close the bottom sheet. */
  fun closeSheet()

  /** Resume the pause topic download. */
  fun resumeDownload()

  /** Pause the downloading topic. */
  fun pauseDownload()

  /** REmove the topic from downloading. */
  fun removeDownload()
}
